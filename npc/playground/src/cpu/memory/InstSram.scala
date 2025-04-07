package cpu.memory
import cpu.config._
import chisel3._
import chisel3.util._
import chisel3.experimental._

class InstSram extends BlackBox(Map(
    "ADDR_WIDTH" -> base.ADDR_WIDTH,
    "DATA_WIDTH" -> base.DATA_WIDTH
)) with HasBlackBoxPath
{
    val io = IO(new Bundle{
        val clk = Input(Clock())
        val rst = Input(Bool())

        val en0 = Input(Bool())
        val raddr0 = Input(UInt(base.ADDR_WIDTH.W))
        val rdata0 = Output(UInt(base.DATA_WIDTH.W))

        val en1 = Input(Bool())
        val raddr1 = Input(UInt(base.ADDR_WIDTH.W))
        val rdata1 = Output(UInt(base.DATA_WIDTH.W))

        val en2 = Input(Bool())
        val raddr2 = Input(UInt(base.ADDR_WIDTH.W))
        val rdata2 = Output(UInt(base.DATA_WIDTH.W))

        val en3 = Input(Bool())
        val raddr3 = Input(UInt(base.ADDR_WIDTH.W))
        val rdata3 = Output(UInt(base.DATA_WIDTH.W))
    })
    addPath("./playground/src/resources/InstSram.v")
}

