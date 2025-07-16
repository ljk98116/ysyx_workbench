package cpu.core.frontend

import chisel3._
import chisel3.util._
import cpu.config._
import cpu.memory._

/* Sram取指令, 16对齐, 4通道输出*/
/* 16对齐看位数是否变化 */
class Fetch extends Module
{
    val io = IO(new Bundle{
        val pc_i = Input(UInt(base.ADDR_WIDTH.W))
        val freereg_rd_able = Input(Vec(base.FETCH_WIDTH, Bool()))
        val rat_flush_en = Input(Bool())
        val rob_state = Input(Bool())
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
        val global_pht_idx_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val local_pht_idx_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val bht_idx_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.BHTID_WIDTH.W)))        
    })

    /* pipeline */
    var inst_valid_mask = RegInit((0.U)(base.FETCH_WIDTH.W))
    inst_valid_mask := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state & io.freereg_rd_able.asUInt.andR, io.inst_valid_mask_i, inst_valid_mask), 
        0.U(base.FETCH_WIDTH.W)
    )

    var pc = RegInit((0.U)(base.ADDR_WIDTH.W))
    pc := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state & io.freereg_rd_able.asUInt.andR, io.pc_i, pc),
        (0.U)(base.ADDR_WIDTH.W)
    )

    var inst_valid_cnt = RegInit((0.U)(log2Ceil(base.FETCH_WIDTH + 1).W))
    inst_valid_cnt := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state & io.freereg_rd_able.asUInt.andR, io.inst_valid_cnt_i, inst_valid_cnt),
        (0.U)(log2Ceil(base.FETCH_WIDTH + 1).W)
    )

    var inst_valid_mask_o = WireInit((0.U)(base.FETCH_WIDTH.W))
    var pc_vec_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.ADDR_WIDTH.W))
    ))
    var inst_valid_cnt_o = WireInit((0.U)(log2Ceil(base.FETCH_WIDTH + 1).W))
    inst_valid_mask_o := Mux(~io.rat_flush_en & ~io.rob_state, inst_valid_mask, 0.U)

    var global_pht_idx_vec_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PHTID_WIDTH.W))
    ))
    global_pht_idx_vec_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state & io.freereg_rd_able.asUInt.andR, io.global_pht_idx_vec_i, global_pht_idx_vec_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.PHTID_WIDTH.W)))        
    )

    var local_pht_idx_vec_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PHTID_WIDTH.W))
    ))
    local_pht_idx_vec_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state & io.freereg_rd_able.asUInt.andR, io.local_pht_idx_vec_i, local_pht_idx_vec_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.PHTID_WIDTH.W)))        
    )

    var bht_idx_vec_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.BHTID_WIDTH.W))
    ))
    bht_idx_vec_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state & io.freereg_rd_able.asUInt.andR, io.bht_idx_vec_i, bht_idx_vec_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.BHTID_WIDTH.W)))        
    )

    pc_vec_o(0) := Mux(~io.rat_flush_en, pc, 0.U)
    pc_vec_o(1) := Mux(~io.rat_flush_en & inst_valid_mask(1), pc + 4.U, 0.U)
    pc_vec_o(2) := Mux(~io.rat_flush_en & inst_valid_mask(2), pc + 8.U, 0.U)
    pc_vec_o(3) := Mux(~io.rat_flush_en & inst_valid_mask(3), pc + 12.U, 0.U)
    inst_valid_cnt_o := Mux(~io.rat_flush_en, inst_valid_cnt, 0.U)
    /* connect */
    io.inst_valid_mask_o := inst_valid_mask_o
    io.pc_vec_o := pc_vec_o
    io.inst_valid_cnt_o := inst_valid_cnt_o
    io.bht_idx_vec_o := bht_idx_vec_reg
    io.global_pht_idx_vec_o := global_pht_idx_vec_reg
    io.local_pht_idx_vec_o := local_pht_idx_vec_reg
}