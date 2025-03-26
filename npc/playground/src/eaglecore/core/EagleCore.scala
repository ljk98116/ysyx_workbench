package eaglecore.core

import chisel3._
import chisel3.util._
import eaglecore.memory.Sram

class EagleCore extends Module
{
    val io = IO(new Bundle{
        val rdata = Output(UInt(32.W))
    })
    
    val data_sram = Module(new Sram)
    data_sram.io.clk := clock
    data_sram.io.rst := reset
    data_sram.io.en := false.B
    data_sram.io.addr := 0.U
    data_sram.io.wen := false.B
    data_sram.io.wdata := 0.U
    data_sram.io.wmask := 0.U
    io.rdata := data_sram.io.rdata
}