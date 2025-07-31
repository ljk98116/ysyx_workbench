package cpu.core.backend

import chisel3._
import chisel3.util._

import cpu.config._
import cpu.core.utils._

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
        // var cdb_i = Input(new CDB)
        val prf_valid_vec = Input(Vec(1 << base.PREG_WIDTH, Bool()))
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

    /* 更新寄存器ready状态 */
    for(j <- 0 until base.FETCH_WIDTH){
        rob_item_reg(j).rdy1 := rob_item_reg(j).rdy1 | (
            io.prf_valid_vec(rob_item_reg(j).ps1) &
            rob_item_reg(j).HasRs1
        )
        rob_item_reg(j).rdy2 := rob_item_reg(j).rdy2 | (
            io.prf_valid_vec(rob_item_reg(j).ps2) &
            rob_item_reg(j).HasRs2
        )
    }

    /* 检查队列前2个位置 */
    var issue_able0 = WireInit(false.B)
    issue_able0 := ~(
        (
            rob_item_reg(head).HasRs1 & 
            ~(rob_item_reg(head).rdy1 | io.prf_valid_vec(rob_item_reg(head).ps1))
        ) |
        (
            rob_item_reg(head).HasRs2 & 
            ~(rob_item_reg(head).rdy2 | io.prf_valid_vec(rob_item_reg(head).ps2))
        )
    ) & rob_item_reg(head).valid & (head =/= tail)
    rob_item_o(0) := Mux(issue_able0, rob_item_reg(head), (0.U).asTypeOf(new ROBItem))

    var issue_able1 = WireInit(false.B)
    issue_able1 := ~(
        (
            rob_item_reg(head + 1.U).HasRs1 & 
            ~(rob_item_reg(head + 1.U).rdy1 | io.prf_valid_vec(rob_item_reg(head + 1.U).ps1))
        ) |
        (
            rob_item_reg(head + 1.U).HasRs2 & 
            ~(rob_item_reg(head + 1.U).rdy2 | io.prf_valid_vec(rob_item_reg(head + 1.U).ps2))
        )
    ) & rob_item_reg(head + 1.U).valid & ((head + 1.U) =/= tail) & 
    ~(rob_item_reg(head + 1.U).isLoad & rob_item_reg(head).isStore) &
    ~(rob_item_reg(head + 1.U).isStore & rob_item_reg(head).isLoad)
    rob_item_o(1) := Mux(issue_able1, rob_item_reg(head + 1.U), (0.U).asTypeOf(new ROBItem))

    /* 更新 */
    /* 不是写入位置或者发射位置，不更新 */
    /* 写入位置更新rdy1, rdy2 */
    /* 发射位置将该位置置为0 */
    for(j <- 0 until size){
        /* 是否是新的item写入的位置 */
        var is_write_loc = WireInit(VecInit(
            Seq.fill(base.FETCH_WIDTH)(false.B)
        ))
        for(k <- 0 until base.FETCH_WIDTH){
            is_write_loc(k) := io.rob_item_i(k).valid & ((tail + k.U) === j.U)
        }
        /* 计算写入位置 */
        var write_loc = WireInit((0.U)((log2Ceil(base.FETCH_WIDTH) + 1).W))
        val prio_enc = Module(new PriorityEncoder(base.FETCH_WIDTH))
        prio_enc.io.val_i := is_write_loc.asUInt
        write_loc := Mux(is_write_loc.asUInt.orR, prio_enc.io.idx_o, base.FETCH_WIDTH.U)

        /* 是否是发射位置 */
        var is_issue_loc = WireInit(false.B)
        is_issue_loc := (issue_able0 & (j.U === head)) | (issue_able1 & (j.U === (head + 1.U)))

        rob_item_reg(j) := Mux(
            (is_issue_loc & io.read_able) | io.rat_flush_en,
            0.U.asTypeOf(new ROBItem),
            Mux(
                is_write_loc.asUInt.orR,
                io.rob_item_i(write_loc(log2Ceil(base.FETCH_WIDTH) - 1, 0)),
                rob_item_reg(j)
            )
        )

        /* 更新寄存器状态位 */
        rob_item_reg(j).rdy1 := Mux(
            (is_issue_loc & io.read_able) | io.rat_flush_en,
            false.B,
            Mux(
                is_write_loc.asUInt.orR,
                io.prf_valid_vec(io.rob_item_i(write_loc(log2Ceil(base.FETCH_WIDTH) - 1, 0)).ps1),
                rob_item_reg(j).rdy1 | 
                io.prf_valid_vec(rob_item_reg(j).ps1)            
            )
        )

        rob_item_reg(j).rdy2 := Mux(
            (is_issue_loc & io.read_able) | io.rat_flush_en,
            false.B,
            Mux(
                is_write_loc.asUInt.orR,
                io.prf_valid_vec(io.rob_item_i(write_loc(log2Ceil(base.FETCH_WIDTH) - 1, 0)).ps2),
                rob_item_reg(j).rdy2 | 
                io.prf_valid_vec(rob_item_reg(j).ps2)            
            )
        )
    }

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