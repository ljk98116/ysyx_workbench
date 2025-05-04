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
        val inst_valid_mask_i = Input(UInt(base.FETCH_WIDTH.W))
        val inst_valid_cnt_i = Input(UInt(log2Ceil(base.FETCH_WIDTH).W))

        val pc_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val inst_valid_mask_o = Output(UInt(base.FETCH_WIDTH.W))
        val inst_valid_cnt_o = Output(UInt(log2Ceil(base.FETCH_WIDTH).W))
    })

    /* pipeline */
    var inst_valid_mask = RegInit((0.U)(base.FETCH_WIDTH.W))
    inst_valid_mask := io.inst_valid_mask_i
    io.inst_valid_mask_o := inst_valid_mask

    var pc = RegInit((0.U)(base.ADDR_WIDTH.W))
    pc := io.pc_i
    
    io.pc_vec_o(0) := pc
    io.pc_vec_o(1) := pc + 4.U
    io.pc_vec_o(2) := pc + 8.U
    io.pc_vec_o(3) := pc + 12.U

    var inst_valid_cnt = RegInit((0.U)(log2Ceil(base.FETCH_WIDTH).W))
    inst_valid_cnt := io.inst_valid_cnt_i
    io.inst_valid_cnt_o := inst_valid_cnt
}