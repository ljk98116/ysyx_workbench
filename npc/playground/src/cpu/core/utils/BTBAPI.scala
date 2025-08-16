package cpu.core.utils

import cpu.config._

import chisel3._
import chisel3.util._
import chisel3.experimental._

class BTBWriteAPI extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val clk = Input(Bool())
        val rst = Input(Bool())
        val wen = Input(Bool())
        val waddr = Input(UInt(base.PHTID_WIDTH.W))
        val V = Input(Bool())
        val BIA = Input(UInt(8.W))
        val BTA = Input(UInt(base.ADDR_WIDTH.W))
    })
    addPath("./playground/src/resources/BTBAPI.v")
}

class BTBReadVAPI extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val rst = Input(Bool())
        val ren = Input(Bool())
        val raddr = Input(UInt(base.PHTID_WIDTH.W))
        val V = Output(Bool())
    })
    addPath("./playground/src/resources/BTBAPI.v")    
}

class BTBReadBIAAPI extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val rst = Input(Bool())
        val ren = Input(Bool())
        val raddr = Input(UInt(base.PHTID_WIDTH.W))
        val BIA = Output(UInt(8.W))
    })
    addPath("./playground/src/resources/BTBAPI.v")    
}

class BTBReadBTAAPI extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val rst = Input(Bool())
        val ren = Input(Bool())
        val raddr = Input(UInt(base.PHTID_WIDTH.W))
        val BTA = Output(UInt(32.W))
    })
    addPath("./playground/src/resources/BTBAPI.v")    
}