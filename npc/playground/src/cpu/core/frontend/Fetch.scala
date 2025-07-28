package cpu.core.frontend

import chisel3._
import chisel3.util._
import cpu.config._
import cpu.memory._

/* Sram取指令, 16对齐, 4通道输出*/
/* 16对齐看位数是否变化 */
/* 优先选择第一个BTB命中且预测为跳转的指令作为分支预测结果 */
class Fetch extends Module
{
    val io = IO(new Bundle{
        val pc_i = Input(UInt(base.ADDR_WIDTH.W))
        val freereg_rd_able = Input(Vec(base.FETCH_WIDTH, Bool()))
        val store_buffer_wr_able = Input(Bool())
        val rat_flush_en = Input(Bool())
        val rob_state = Input(UInt(2.W))
        val inst_valid_mask_i = Input(UInt(base.FETCH_WIDTH.W))
        val inst_valid_cnt_i = Input(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))

        /* 当前全局、局部历史PHT索引 */
        val global_pht_idx_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val local_pht_idx_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val bht_idx_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.BHTID_WIDTH.W)))

        val pc_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val inst_valid_mask_o = Output(UInt(base.FETCH_WIDTH.W))
        val inst_valid_cnt_o = Output(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))
        /* 分支预测结果 */
        /* 使用全局/局部历史预测 */
        /* 分支预测方向 */
        val branch_pre_res_i = Input(Vec(base.FETCH_WIDTH, Bool()))

        val btb_hit_vec_i = Input(Vec(base.FETCH_WIDTH, Bool()))
        val btb_pred_addr_i = Input(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))

        /* 分支预测使能 */
        val branch_en_pred = Output(Bool())
        val branch_addr_pred = Output(UInt(base.ADDR_WIDTH.W))

        val global_pht_idx_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val local_pht_idx_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val bht_idx_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.BHTID_WIDTH.W)))        
    })

    /* pipeline */
    var inst_valid_mask = RegInit((0.U)(base.FETCH_WIDTH.W))
    var stall = WireInit(false.B)
    stall := (io.rob_state === "b00".U) & io.freereg_rd_able.asUInt.andR & io.store_buffer_wr_able
    inst_valid_mask := Mux(
        ~io.rat_flush_en, 
        Mux(stall, io.inst_valid_mask_i, inst_valid_mask), 
        0.U(base.FETCH_WIDTH.W)
    )

    var pc = RegInit((0.U)(base.ADDR_WIDTH.W))
    pc := Mux(
        ~io.rat_flush_en, 
        Mux(stall, io.pc_i, pc),
        (0.U)(base.ADDR_WIDTH.W)
    )

    var inst_valid_cnt = RegInit((0.U)(log2Ceil(base.FETCH_WIDTH + 1).W))
    inst_valid_cnt := Mux(
        ~io.rat_flush_en, 
        Mux(stall, io.inst_valid_cnt_i, inst_valid_cnt),
        (0.U)(log2Ceil(base.FETCH_WIDTH + 1).W)
    )

    var inst_valid_mask_o = WireInit((0.U)(base.FETCH_WIDTH.W))
    var pc_vec_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.ADDR_WIDTH.W))
    ))
    var inst_valid_cnt_o = WireInit((0.U)(log2Ceil(base.FETCH_WIDTH + 1).W))
    inst_valid_mask_o := Mux(~io.rat_flush_en & (io.rob_state === "b00".U), inst_valid_mask, 0.U)

    var global_pht_idx_vec_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PHTID_WIDTH.W))
    ))
    global_pht_idx_vec_reg := Mux(
        ~io.rat_flush_en, 
        Mux(stall, io.global_pht_idx_vec_i, global_pht_idx_vec_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.PHTID_WIDTH.W)))        
    )

    var local_pht_idx_vec_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PHTID_WIDTH.W))
    ))
    local_pht_idx_vec_reg := Mux(
        ~io.rat_flush_en, 
        Mux(stall, io.local_pht_idx_vec_i, local_pht_idx_vec_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.PHTID_WIDTH.W)))        
    )

    var bht_idx_vec_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.BHTID_WIDTH.W))
    ))
    bht_idx_vec_reg := Mux(
        ~io.rat_flush_en, 
        Mux(stall, io.bht_idx_vec_i, bht_idx_vec_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.BHTID_WIDTH.W)))        
    )

    pc_vec_o(0) := Mux(~io.rat_flush_en, pc, 0.U)
    pc_vec_o(1) := Mux(~io.rat_flush_en & inst_valid_mask(1), pc + 4.U, 0.U)
    pc_vec_o(2) := Mux(~io.rat_flush_en & inst_valid_mask(2), pc + 8.U, 0.U)
    pc_vec_o(3) := Mux(~io.rat_flush_en & inst_valid_mask(3), pc + 12.U, 0.U)
    inst_valid_cnt_o := Mux(~io.rat_flush_en, inst_valid_cnt, 0.U)

    /* 跳转到第一个预测为跳转的分支指令 */
    var branch_en_pred = WireInit(false.B)
    branch_en_pred := io.branch_pre_res_i.asUInt.orR & io.btb_hit_vec_i.asUInt.orR

    var branch_pred_addr = WireInit((0.U)(base.ADDR_WIDTH.W))
    var branch_pred_addr_mid = WireInit(VecInit(
        Seq.fill(2)((0.U)(base.ADDR_WIDTH.W))
    ))
    branch_pred_addr_mid(0) := Mux(
        io.branch_pre_res_i(0) & io.btb_hit_vec_i(0),
        io.btb_pred_addr_i(0),
        Mux(
            io.branch_pre_res_i(1) & io.btb_hit_vec_i(1),
            io.btb_pred_addr_i(1),
            0.U
        )
    )
    branch_pred_addr_mid(1) := Mux(
        io.branch_pre_res_i(2) & io.btb_hit_vec_i(2),
        io.btb_pred_addr_i(2),
        Mux(
            io.branch_pre_res_i(3) & io.btb_hit_vec_i(3),
            io.btb_pred_addr_i(3),
            0.U
        )
    )
    branch_pred_addr := Mux(
        branch_pred_addr_mid(0) =/= 0.U,
        branch_pred_addr_mid(0),
        branch_pred_addr_mid(1)
    )

    io.inst_valid_mask_o := Cat(
        Mux(
            (
                (io.branch_pre_res_i(0) & io.btb_hit_vec_i(0)) | 
                (io.branch_pre_res_i(1) & io.btb_hit_vec_i(1)) |
                (io.branch_pre_res_i(2) & io.btb_hit_vec_i(2))
            ),
            false.B,
            inst_valid_mask_o(3)
        ),
        Mux(
            ((io.branch_pre_res_i(0) & io.btb_hit_vec_i(0)) | (io.branch_pre_res_i(1) & io.btb_hit_vec_i(1))),
            false.B,
            inst_valid_mask_o(2)
        ),
        Mux(io.branch_pre_res_i(0) & io.btb_hit_vec_i(0), false.B, inst_valid_mask_o(1)),
        inst_valid_mask_o(0)
    )
    /* connect */
    // io.inst_valid_mask_o := inst_valid_mask_o
    io.pc_vec_o := pc_vec_o
    io.inst_valid_cnt_o := io.inst_valid_mask_o(0).asTypeOf(UInt(3.W)) +
        io.inst_valid_mask_o(1).asTypeOf(UInt(3.W)) +
        io.inst_valid_mask_o(2).asTypeOf(UInt(3.W)) +
        io.inst_valid_mask_o(3).asTypeOf(UInt(3.W))
    io.bht_idx_vec_o := bht_idx_vec_reg
    io.global_pht_idx_vec_o := global_pht_idx_vec_reg
    io.local_pht_idx_vec_o := local_pht_idx_vec_reg
    io.branch_en_pred := branch_en_pred
    io.branch_addr_pred := branch_pred_addr
}