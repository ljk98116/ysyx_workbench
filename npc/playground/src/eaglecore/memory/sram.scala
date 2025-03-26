package eaglecore.memory

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Sram extends BlackBox(Map(
    "ADDR_WIDTH" -> 32,
    "DATA_WIDTH" -> 32
)) with HasBlackBoxPath
{
    addPath("./playground/src/resources/sram.v")
    val io = IO(new Bundle{
        val clk = Input(Clock())
        val rst = Input(Bool())
        val en = Input(Bool())
        val wen = Input(Bool())
        val addr = Input(UInt(32.W))
        val wmask = Input(UInt(8.W))
        val wdata = Input(UInt(32.W))
        val rdata = Output(UInt(32.W))
    })
}

