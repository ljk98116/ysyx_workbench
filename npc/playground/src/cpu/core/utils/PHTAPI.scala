package cpu.core.utils

import cpu.config._

import chisel3._
import chisel3.util._
import chisel3.experimental._

class lPHTWriteAPI extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val clk = Input(Bool())
        val rst = Input(Bool())
        val wen = Input(Bool())
        val waddr = Input(UInt(base.PHTID_WIDTH.W))
        val wdata = Input(UInt(2.W))
    })
    addPath("./playground/src/resources/PHTAPI.v")
}

class lPHTReadAPI extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val rst = Input(Bool())
        val ren = Input(Bool())
        val raddr = Input(UInt(base.PHTID_WIDTH.W))
        val rdata = Output(UInt(2.W))
    })
    addPath("./playground/src/resources/PHTAPI.v")
}

class gPHTWriteAPI extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val clk = Input(Bool())
        val rst = Input(Bool())
        val wen = Input(Bool())
        val waddr = Input(UInt(base.PHTID_WIDTH.W))
        val wdata = Input(UInt(2.W))
    })
    addPath("./playground/src/resources/PHTAPI.v")
}

class gPHTReadAPI extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val rst = Input(Bool())
        val ren = Input(Bool())
        val raddr = Input(UInt(base.PHTID_WIDTH.W))
        val rdata = Output(UInt(2.W))
    })
    addPath("./playground/src/resources/PHTAPI.v")
}

class cPHTWriteAPI extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val clk = Input(Bool())
        val rst = Input(Bool())
        val wen = Input(Bool())
        val waddr = Input(UInt(base.PHTID_WIDTH.W))
        val wdata = Input(UInt(2.W))
    })
    addPath("./playground/src/resources/PHTAPI.v")
}

class cPHTReadAPI extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val rst = Input(Bool())
        val ren = Input(Bool())
        val raddr = Input(UInt(base.PHTID_WIDTH.W))
        val rdata = Output(UInt(2.W))
    })
    addPath("./playground/src/resources/PHTAPI.v")
}


