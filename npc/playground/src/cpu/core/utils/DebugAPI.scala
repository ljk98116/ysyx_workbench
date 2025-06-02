package cpu.core.utils

import cpu.config._

import chisel3._
import chisel3.util._
import chisel3.experimental._

class CommitAPI extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val rst = Input(Bool())
        val rat_write_en_0 = Input(UInt(8.W))
        val rat_write_addr_0 = Input(UInt(8.W))
        val rat_write_addr_1 = Input(UInt(8.W))
        val rat_write_addr_2 = Input(UInt(8.W))
        val rat_write_addr_3 = Input(UInt(8.W))
        val rat_write_data_0 = Input(UInt(8.W))
        val rat_write_data_1 = Input(UInt(8.W))
        val rat_write_data_2 = Input(UInt(8.W))
        val rat_write_data_3 = Input(UInt(8.W))
        val reg_write_data_0 = Input(UInt(base.DATA_WIDTH.W))
        val reg_write_data_1 = Input(UInt(base.DATA_WIDTH.W))
        val reg_write_data_2 = Input(UInt(base.DATA_WIDTH.W))
        val reg_write_data_3 = Input(UInt(base.DATA_WIDTH.W))
    })
    addPath("./playground/src/resources/Commit.v")
}