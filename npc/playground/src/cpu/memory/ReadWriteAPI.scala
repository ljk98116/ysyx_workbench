package cpu.memory

import cpu.config._
import chisel3._
import chisel3.util._
import chisel3.experimental._

class MemReadAPI extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val clk = Input(Clock())
        val rst = Input(Bool())
        val raddr = Input(UInt(base.ADDR_WIDTH.W))
        val rdata = Output(UInt(base.DATA_WIDTH.W))
    })
    //addPath("./playground/src/resources/MemRead.v")
}

class MemWriteAPI extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val clk = Input(Clock())
        val rst = Input(Bool())
        val wmask = Input(UInt(8.W))
        val waddr = Input(UInt(base.ADDR_WIDTH.W))
        val wdata = Input(UInt(base.DATA_WIDTH.W))
    })
    //addPath("./playground/src/resources/MemWrite.v")
}