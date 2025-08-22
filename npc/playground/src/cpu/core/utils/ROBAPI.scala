package cpu.core.utils

import cpu.config._

import chisel3._
import chisel3.util._
import chisel3.experimental._

class ROBReadAPI(val elem : String, val width: Int) extends BlackBox(Map()) with HasBlackBoxPath{
    val io = IO(new Bundle{
        val rst = Input(Bool())
        val ren = Input(Bool())
        val index = Input(UInt(8.W))
        val bankid = Input(UInt(2.W))
        val rob_rdata = Output(UInt(width.W))
    })
  // 告诉 Chisel 从这个资源文件中寻找 Verilog 模块定义
  addPath("./playground/src/resources/ROBAPI.v")

  // 告诉 FIRRTL 使用的是哪个 Verilog module 名称
  override def desiredName: String = s"ROBReadAPI$elem"
}

class ROBWriteAPI(val elem : String, val width: Int) extends BlackBox(Map()) with HasBlackBoxPath{
    val io = IO(new Bundle{
        val clk = Input(Bool())
        val rst = Input(Bool())
        val wen = Input(Bool())
        val index = Input(UInt(8.W))
        val bankid = Input(UInt(2.W))
        val rob_wdata = Input(UInt(width.W))
    })
  // 告诉 Chisel 从这个资源文件中寻找 Verilog 模块定义
  addPath("./playground/src/resources/ROBAPI.v")

  // 告诉 FIRRTL 使用的是哪个 Verilog module 名称
  override def desiredName: String = s"ROBWriteAPI$elem"
}

class ROBIdLocMemWriteAPI extends BlackBox with HasBlackBoxPath {
    val io = IO(new Bundle{
        val clk = Input(Bool())
        val rst = Input(Bool())
        val wen = Input(Bool())
        val index = Input(UInt(8.W))
        val data = Input(UInt(8.W))
    })
    addPath("./playground/src/resources/ROBAPI.v")
}

class ROBIdLocMemReadAPI extends BlackBox with HasBlackBoxPath {
    val io = IO(new Bundle{
        val rst = Input(Bool())
        val ren = Input(Bool())
        val index = Input(UInt(8.W))
        val data = Output(UInt(8.W))
    })
    addPath("./playground/src/resources/ROBAPI.v")
}