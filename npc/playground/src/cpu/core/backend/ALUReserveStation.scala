package cpu.core.backend

import chisel3._
import chisel3.util._

import cpu.config._
import cpu.core.utils._
import chisel3.util.SparseVec.Lookup.OneHot
import cpu.config.base.ALU_NUM

/* 每周期分配固定数量的ID */
class ReserveFreeIdBuffer(size : Int) extends Module
{
    var width = log2Ceil(size)
    val io = IO(new Bundle{
        /* issue stage */
        val free_id_ren = Input(Bool())
        val rat_flush_en = Input(Bool())
        val issued_i = Input(Bool())
        val issued_id_i = Input(UInt(width.W))
        /* output */
        val free_id_o = Output(UInt(width.W)) 
        val rd_able = Output(Bool())
        val wr_able = Output(Bool())       
    })

    var IDRegFile = RegInit(VecInit(
        Seq.tabulate(size)((i) => {i.U})
    ))

    var head = RegInit((0.U)(width.W))
    var tail = RegInit(((size - 1).U)(width.W))

    io.rd_able := (head =/= tail)
    io.wr_able := (tail + 1.U) =/= head

    for(i <- 0 until size){
        when(~io.rat_flush_en){
            IDRegFile(i) := Mux(io.issued_i & (i.U === tail), io.issued_id_i, IDRegFile(i)) 
        }.otherwise{
            IDRegFile(i) := i.U
        }
    }

    var free_id_o = WireInit((0.U)(width.W))
    free_id_o := IDRegFile(head)

    head := Mux(io.rd_able, Mux(~io.rat_flush_en, head + io.free_id_ren, 0.U), head)
    tail := Mux(io.wr_able & io.issued_i & ~io.rat_flush_en, tail + io.issued_i.asUInt, Mux(io.rat_flush_en, (size - 1).U, tail))

    /* connect */
    io.free_id_o := free_id_o
}

/* 指定step长度和队列大小 */
/* 维护一个Reservestation空闲位置的队列 + 每一个位置对应的年龄矩阵 */
/* 时序逻辑接收发射队列内rdy状态的更新，时序逻辑 */
/* 组合逻辑判断当前每一项是否是最老的能发射的 */
class ALUReserveStation(size: Int) extends Module {
    val io = IO(new Bundle {
        val rat_flush_en = Input(Bool())
        val rob_state = Input(UInt(2.W))
        /* PRF 读使能 */
        val prf_rs1_data_ren = Output(Bool())
        val prf_rs2_data_ren = Output(Bool())
        val prf_rs1_data_raddr = Output(UInt(base.PREG_WIDTH.W))
        val prf_rs2_data_raddr = Output(UInt(base.PREG_WIDTH.W))
        val prf_rs1_data_rdata = Input(UInt(base.DATA_WIDTH.W))
        val prf_rs2_data_rdata = Input(UInt(base.DATA_WIDTH.W))
        /* 输出对应channel的操作数 */
        val alu_channel_rs1_rdata = Output(UInt(base.DATA_WIDTH.W))
        val alu_channel_rs2_rdata = Output(UInt(base.DATA_WIDTH.W))
        var rob_item_i = Input(new ROBItem)
        /* 总线状态 */
        var cdb_i = Input(new CDB)
        var rob_item_o = Output(new ROBItem)
        var write_able = Bool()
        var read_able = Bool()
    })

    /* 寄存器读取 */

    /* free id阵列 */
    val freeIdBuffer = Module(new ReserveFreeIdBuffer(size))

    /* payload ram */
    var ps1_payLoad_RAM = RegInit(VecInit(
        Seq.fill(size)((0.U)(base.DATA_WIDTH.W))
    ))
    var ps2_payLoad_RAM = RegInit(VecInit(
        Seq.fill(size)((0.U)(base.DATA_WIDTH.W))
    ))

    /* connect */
    /* 写入发射队列的指令数目 */
    /* 读写使能与空闲队列保持一致 */
    io.read_able := freeIdBuffer.io.rd_able
    io.write_able := freeIdBuffer.io.wr_able

    /* age matrix */
    var age_mat = RegInit(VecInit(
        Seq.fill(size)(
            VecInit(Seq.fill(size)(false.B))
        )
    ))
    /* issue rob item reg */
    var rob_item_reg = RegInit(VecInit(
        Seq.fill(size)((0.U).asTypeOf(new ROBItem))
    ))

    /* search issue able insts */
    /* 需要rs1且rs1 ready，需要rs2且rs2 ready */
    var issue_able_vec = WireInit(VecInit(
        Seq.fill(size)(false.B)
    ))
    for(i <- 0 until size){
        issue_able_vec(i) := ~(
            (rob_item_reg(i).HasRs1 & ~rob_item_reg(i).rdy1) | 
            (rob_item_reg(i).HasRs2 & ~rob_item_reg(i).rdy2)) & rob_item_reg(i).valid
    }
    /* 能发射且比其他能发射的矩阵年长 */
    var issue_oh_vec = WireInit(VecInit(
        Seq.fill(size)(false.B)
    ))
    for(i <- 0 until size){
        var issue_loc_mask = WireInit(VecInit(
            Seq.fill(size)(true.B)
        ))
        /* 对于每个能发射的位置i必须比j年长或者j位置无效，要么不能发射 */
        /* 不能发射同样满足条件 */
        for(j <- 0 until size){
            if(j != i){
                issue_loc_mask(j) := ~issue_able_vec(j) | (issue_able_vec(j) & age_mat(i)(j))
            }
        }
        issue_loc_mask(i) := true.B
        issue_oh_vec(i) := issue_able_vec(i) & issue_loc_mask.asUInt.andR
    }

    var issue_idx = WireInit((size.U)((log2Ceil(size) + 1).W))
    issue_idx := Mux(~issue_oh_vec.asUInt.orR, size.U, OHToUInt(issue_oh_vec.asUInt))
    io.rob_item_o := Mux(issue_idx =/= size.U, rob_item_reg(issue_idx(log2Ceil(size) - 1, 0)), (0.U).asTypeOf(new ROBItem))
    /* 回收issue_idx */
    freeIdBuffer.io.rat_flush_en := io.rat_flush_en
    freeIdBuffer.io.issued_i := issue_oh_vec.asUInt.orR & ~io.rat_flush_en
    freeIdBuffer.io.issued_id_i := issue_idx(log2Ceil(size) - 1, 0)
    freeIdBuffer.io.free_id_ren := io.rob_item_i.valid
    /* 使用总线消息更新发射队列进入项 */
    var rob_item_i_update = WireInit((0.U).asTypeOf(new ROBItem))
    /* 2 stage logic */
    rob_item_i_update := io.rob_item_i

    var rdy1_vec = WireInit(VecInit(
        Seq.fill(base.ALU_NUM + base.AGU_NUM)(false.B)
    ))
    var rdy2_vec = WireInit(VecInit(
        Seq.fill(base.ALU_NUM + base.AGU_NUM)(false.B)
    ))
    for(i <- 0 until base.ALU_NUM){
        rdy1_vec(i) := 
            (io.cdb_i.alu_channel(i).phy_reg_id === io.rob_item_i.ps1) & 
            io.cdb_i.alu_channel(i).valid &
            (io.cdb_i.alu_channel(i).arch_reg_id === io.rob_item_i.rs1)
        rdy2_vec(i) := 
            (io.cdb_i.alu_channel(i).phy_reg_id === io.rob_item_i.ps2) & 
            io.cdb_i.alu_channel(i).valid &
            (io.cdb_i.alu_channel(i).arch_reg_id === io.rob_item_i.rs2)
    }

    for(i <- 0 until base.AGU_NUM){
        rdy1_vec(i + base.ALU_NUM) := 
            (io.cdb_i.agu_channel(i).phy_reg_id === io.rob_item_i.ps1) & 
            io.cdb_i.agu_channel(i).valid &
            (io.cdb_i.agu_channel(i).arch_reg_id === io.rob_item_i.rs1)
        rdy2_vec(i + base.ALU_NUM) := 
            (io.cdb_i.agu_channel(i).phy_reg_id === io.rob_item_i.ps2) & 
            io.cdb_i.agu_channel(i).valid &
            (io.cdb_i.agu_channel(i).arch_reg_id === io.rob_item_i.rs2)
    }    
    rob_item_i_update.rdy1 := rdy1_vec.asUInt.orR | io.rob_item_i.rdy1
    rob_item_i_update.rdy2 := rdy2_vec.asUInt.orR | io.rob_item_i.rdy2
    /* update issue rob regs */
    /* update age matrix, age[i]为0表示当前ID比其他位置都年轻*/
    /* 新分配的reg更新年龄矩阵 */
    for(i <- 0 until size){
        when(~io.rat_flush_en & (io.rob_state === 0.U) & (i.U === freeIdBuffer.io.free_id_o) & io.rob_item_i.valid){
            rob_item_reg(i) := rob_item_i_update
            /* 有效的项比新写入的项要老 */
            for(j <- 0 until size){
                if(i != j){
                    age_mat(j)(i) := rob_item_reg(j).valid
                }
                age_mat(i)(j) := false.B
            }
        }.elsewhen(~io.rat_flush_en & (i.U =/= freeIdBuffer.io.free_id_o) & rob_item_reg(i).valid & (i.U =/= issue_idx)){
            var rob_item = WireInit((0.U).asTypeOf(new ROBItem))
            var rdy1_vec = WireInit(VecInit(
                Seq.fill(base.ALU_NUM + base.AGU_NUM)(false.B)
            ))
            var rdy2_vec = WireInit(VecInit(
                Seq.fill(base.ALU_NUM + base.AGU_NUM)(false.B)
            ))
            for(j <- 0 until base.ALU_NUM){
                rdy1_vec(j) := 
                    (io.cdb_i.alu_channel(j).phy_reg_id === rob_item_reg(i).ps1) & 
                    io.cdb_i.alu_channel(j).valid &
                    (io.cdb_i.alu_channel(j).arch_reg_id === rob_item_reg(i).rs1)

                rdy2_vec(j) := 
                    (io.cdb_i.alu_channel(j).phy_reg_id === rob_item_reg(i).ps2) & 
                    io.cdb_i.alu_channel(j).valid &
                    (io.cdb_i.alu_channel(j).arch_reg_id === rob_item_reg(i).rs2)
            }
            for(j <- 0 until base.AGU_NUM){
                rdy1_vec(j + base.ALU_NUM) := 
                    (io.cdb_i.agu_channel(j).phy_reg_id === rob_item_reg(i).ps1) & 
                    io.cdb_i.agu_channel(j).valid &
                    (io.cdb_i.agu_channel(j).arch_reg_id === rob_item_reg(i).rs1)
                rdy2_vec(j + base.ALU_NUM) := 
                    (io.cdb_i.agu_channel(j).phy_reg_id === rob_item_reg(i).ps2) & 
                    io.cdb_i.agu_channel(j).valid &
                    (io.cdb_i.agu_channel(j).arch_reg_id === rob_item_reg(i).rs2)
            }
            rob_item_reg(i).rdy1 := rdy1_vec.asUInt.orR | rob_item_reg(i).rdy1
            rob_item_reg(i).rdy2 := rdy2_vec.asUInt.orR | rob_item_reg(i).rdy2
        }.elsewhen((i.U === issue_idx) & freeIdBuffer.io.issued_i & ~io.rat_flush_en){
            for(j <- 0 until size){
                age_mat(issue_idx(log2Ceil(size) - 1, 0))(j) := false.B
                age_mat(j)(issue_idx(log2Ceil(size) - 1, 0)) := rob_item_reg(j).valid
            }
            rob_item_reg(issue_idx(log2Ceil(size) - 1, 0)) := 0.U.asTypeOf(new ROBItem)
        }.elsewhen(io.rat_flush_en){
            rob_item_reg(i) := 0.U.asTypeOf(new ROBItem)
            for(j <- 0 until size){
                age_mat(i)(j) := false.B
            }
        }
    }
    io.prf_rs1_data_ren := io.rob_item_o.HasRs1 & (io.rob_item_o.rs1 =/= 0.U)
    io.prf_rs2_data_ren := io.rob_item_o.HasRs2 & (io.rob_item_o.rs2 =/= 0.U)
    io.prf_rs1_data_raddr := io.rob_item_o.ps1
    io.prf_rs2_data_raddr := io.rob_item_o.ps2
    io.alu_channel_rs1_rdata := io.prf_rs1_data_rdata
    io.alu_channel_rs2_rdata := io.prf_rs2_data_rdata
}