package cpu.core.backend

import chisel3._
import chisel3.util._

import cpu.config._

/* 顺序接收ROB项，顺序发射 */
/* 异常时清空队列 */
/* 如果是前面store后面load或者load后面store，且均能发射，只发射第一个指令, 避免load forwarding失效 */
class AGUReservestation(size : Int) extends Module
{
    val width = log2Ceil(size)
    val io = IO(new Bundle{
        val rat_flush_en = Input(Bool())
        val rob_state = Input(UInt(2.W))
        var rob_item_i = Input(Vec(base.FETCH_WIDTH, new ROBItem))
        var valid_cnt_i = Input(UInt((log2Ceil(base.FETCH_WIDTH) + 1).W))

        /* PRF 读使能 */
        val prf_rs1_data_ren = Output(Vec(base.AGU_NUM, Bool()))
        val prf_rs2_data_ren = Output(Vec(base.AGU_NUM, Bool()))
        val prf_rs1_data_raddr = Output(Vec(base.AGU_NUM, UInt(base.PREG_WIDTH.W)))
        val prf_rs2_data_raddr = Output(Vec(base.AGU_NUM, UInt(base.PREG_WIDTH.W)))
        val prf_rs1_data_rdata = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val prf_rs2_data_rdata = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        /* 输出对应channel的操作数 */
        val agu_channel_rs1_rdata = Output(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val agu_channel_rs2_rdata = Output(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        /* 总线状态 */
        var cdb_i = Input(new CDB)
        var rob_item_o = Output(Vec(base.AGU_NUM, new ROBItem))
        var read_able = Output(Bool())
        var write_able = Output(Bool())
    })

    var rob_item_reg = RegInit(VecInit(
        Seq.fill(size)((0.U).asTypeOf(new ROBItem))
    ))

    var head = RegInit((0.U)(width.W))
    var tail = RegInit((0.U)(width.W))

    io.read_able := head =/= tail
    io.write_able := 
        (tail + 1.U =/= head) & 
        (tail + 2.U =/= head) &
        (tail + 3.U =/= head) &
        (tail + 4.U =/= head)

    var rob_item_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)asTypeOf(new ROBItem))
    ))

    for(i <- 0 until base.FETCH_WIDTH){
        when(io.write_able & ~io.rat_flush_en & io.rob_item_i(i).valid & (io.rob_state === 0.U)){
            rob_item_reg(tail + i.U) := io.rob_item_i(i)
        }
    }

    /* 更新 */
    for(j <- 0 until size){
        when(~io.rat_flush_en & rob_item_reg(j).valid){
            var issue_able_rs1_vec = WireInit(VecInit(
                Seq.fill(base.AGU_NUM + base.ALU_NUM)(false.B)
            ))
            var issue_able_rs2_vec = WireInit(VecInit(
                Seq.fill(base.AGU_NUM + base.ALU_NUM)(false.B)
            ))
            for(i <- 0 until base.ALU_NUM){
                issue_able_rs1_vec(i) := 
                    (rob_item_reg(j).ps1 === io.cdb_i.alu_channel(i).phy_reg_id) &
                    io.cdb_i.alu_channel(i).valid &
                    (io.cdb_i.alu_channel(i).arch_reg_id === rob_item_reg(j).rs1)
                issue_able_rs2_vec(i) := 
                    (rob_item_reg(j).ps2 === io.cdb_i.alu_channel(i).phy_reg_id) &
                    io.cdb_i.alu_channel(i).valid &
                    (io.cdb_i.alu_channel(i).arch_reg_id === rob_item_reg(j).rs2)
            }
            for(i <- 0 until base.AGU_NUM){
                issue_able_rs1_vec(i + base.ALU_NUM) := 
                    (rob_item_reg(j).ps1 === io.cdb_i.agu_channel(i).phy_reg_id) &
                    io.cdb_i.agu_channel(i).valid &
                    (io.cdb_i.agu_channel(i).arch_reg_id === rob_item_reg(j).rs1)
                issue_able_rs2_vec(i + base.ALU_NUM) := 
                    (rob_item_reg(j).ps2 === io.cdb_i.agu_channel(i).phy_reg_id) &
                    io.cdb_i.agu_channel(i).valid &
                    (io.cdb_i.agu_channel(i).arch_reg_id === rob_item_reg(j).rs2)
            }
            rob_item_reg(j).rdy1 := issue_able_rs1_vec.asUInt.orR | rob_item_reg(j).rdy1 
            rob_item_reg(j).rdy2 := issue_able_rs2_vec.asUInt.orR | rob_item_reg(j).rdy2
        }.elsewhen(io.rat_flush_en){
            rob_item_reg(j) := 0.U.asTypeOf(new ROBItem)
        }
    }

    /* 检查队列前2个位置 */
    var issue_able0 = WireInit(false.B)
    issue_able0 := ~(
        (rob_item_reg(head).HasRs1 & ~rob_item_reg(head).rdy1) |
        (rob_item_reg(head).HasRs2 & ~rob_item_reg(head).rdy2)
    ) & rob_item_reg(head).valid & (head =/= tail)
    rob_item_o(0) := Mux(issue_able0, rob_item_reg(head), (0.U).asTypeOf(new ROBItem))

    var issue_able1 = WireInit(false.B)
    issue_able1 := ~(
        (rob_item_reg(head + 1.U).HasRs1 & ~rob_item_reg(head + 1.U).rdy1) |
        (rob_item_reg(head + 1.U).HasRs2 & ~rob_item_reg(head + 1.U).rdy2)
    ) & rob_item_reg(head + 1.U).valid & ((head + 1.U) =/= tail) & 
    ~(rob_item_reg(head + 1.U).isLoad & rob_item_reg(head).isStore) &
    ~(rob_item_reg(head + 1.U).isStore & rob_item_reg(head).isLoad)
    rob_item_o(1) := Mux(issue_able1, rob_item_reg(head + 1.U), (0.U).asTypeOf(new ROBItem))

    io.prf_rs1_data_ren(0) := Mux(issue_able0, rob_item_o(0).HasRs1 & (rob_item_o(0).rs1 =/= 0.U), false.B)
    io.prf_rs1_data_raddr(0) := Mux(issue_able0, rob_item_o(0).ps1, 0.U)
    io.prf_rs2_data_ren(0) := Mux(issue_able0, rob_item_o(0).HasRs2 & (rob_item_o(0).rs2 =/= 0.U), false.B)
    io.prf_rs2_data_raddr(0) := Mux(issue_able0, rob_item_o(0).ps2, 0.U)

    io.prf_rs1_data_ren(1) := Mux(issue_able0, rob_item_o(1).HasRs1 & (rob_item_o(1).rs1 =/= 0.U), false.B)
    io.prf_rs1_data_raddr(1) := Mux(issue_able0, rob_item_o(1).ps1, 0.U)
    io.prf_rs2_data_ren(1) := Mux(issue_able0, rob_item_o(1).HasRs2 & (rob_item_o(1).rs2 =/= 0.U), false.B)
    io.prf_rs2_data_raddr(1) := Mux(issue_able0, rob_item_o(1).ps2, 0.U)

    io.agu_channel_rs1_rdata := io.prf_rs1_data_rdata
    io.agu_channel_rs2_rdata := io.prf_rs2_data_rdata

    head := Mux(
        io.rat_flush_en,
        0.U,
        Mux(
            issue_able0 & issue_able1, 
            head + 2.U,
            Mux(issue_able0, head + 1.U, head)
        )
    )
    tail := Mux(io.write_able & ~io.rat_flush_en & (io.rob_state === 0.U), tail + io.valid_cnt_i, Mux(~io.rat_flush_en, tail, 0.U))

    /* connect */
    io.rob_item_o := rob_item_o
}