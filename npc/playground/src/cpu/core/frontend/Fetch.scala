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
        val pc_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val inst_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.DATA_WIDTH.W)))
        val inst_valid_mask_o = Output(UInt(base.FETCH_WIDTH.W))
    })

    val inst_ram = Module(new InstSram)
    inst_ram.io.clk := clock
    inst_ram.io.rst := reset

    inst_ram.io.en0 := io.inst_valid_mask_i(0)
    inst_ram.io.en1 := io.inst_valid_mask_i(1)
    inst_ram.io.en2 := io.inst_valid_mask_i(2)
    inst_ram.io.en3 := io.inst_valid_mask_i(3)

    inst_ram.io.raddr0 := io.pc_i
    inst_ram.io.raddr1 := io.pc_i + 4.U
    inst_ram.io.raddr2 := io.pc_i + 8.U
    inst_ram.io.raddr3 := io.pc_i + 12.U

    io.inst_vec_o(0) := inst_ram.io.rdata0
    io.inst_vec_o(1) := inst_ram.io.rdata1
    io.inst_vec_o(2) := inst_ram.io.rdata2
    io.inst_vec_o(3) := inst_ram.io.rdata3

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
}