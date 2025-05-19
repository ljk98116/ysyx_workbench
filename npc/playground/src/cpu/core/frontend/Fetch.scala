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
        val rat_flush_en = Input(Bool())
        val inst_valid_mask_i = Input(UInt(base.FETCH_WIDTH.W))
        val inst_valid_cnt_i = Input(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))

        val pc_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val inst_valid_mask_o = Output(UInt(base.FETCH_WIDTH.W))
        val inst_valid_cnt_o = Output(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))
    })

    /* pipeline */
    var inst_valid_mask = RegInit((0.U)(base.FETCH_WIDTH.W))
    inst_valid_mask := io.inst_valid_mask_i

    var pc = RegInit((0.U)(base.ADDR_WIDTH.W))
    pc := io.pc_i

    var inst_valid_cnt = RegInit((0.U)(log2Ceil(base.FETCH_WIDTH + 1).W))
    inst_valid_cnt := io.inst_valid_cnt_i

    var inst_valid_mask_o = WireInit((0.U)(base.FETCH_WIDTH.W))
    var pc_vec_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.ADDR_WIDTH.W))
    ))
    var inst_valid_cnt_o = WireInit((0.U)(log2Ceil(base.FETCH_WIDTH + 1).W))
    inst_valid_mask_o := Mux(~io.rat_flush_en, inst_valid_mask, 0.U)
    pc_vec_o(0) := Mux(~io.rat_flush_en, pc, 0.U)
    pc_vec_o(1) := Mux(~io.rat_flush_en, pc + 4.U, 0.U)
    pc_vec_o(2) := Mux(~io.rat_flush_en, pc + 8.U, 0.U)
    pc_vec_o(3) := Mux(~io.rat_flush_en, pc + 12.U, 0.U)
    inst_valid_cnt_o := Mux(~io.rat_flush_en, inst_valid_cnt, 0.U)
    /* connect */
    io.inst_valid_mask_o := inst_valid_mask_o
    io.pc_vec_o := pc_vec_o
    io.inst_valid_cnt_o := inst_valid_cnt_o
}