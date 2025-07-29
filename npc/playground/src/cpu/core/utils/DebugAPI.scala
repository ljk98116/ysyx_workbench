package cpu.core.utils

import cpu.config._

import chisel3._
import chisel3.util._
import chisel3.experimental._

class CommitAPI extends BlackBox{
    val io = IO(new Bundle{
        val rst = Input(Bool())
        val valid = Input(UInt(8.W))
        val rat_write_en = Input(UInt(8.W))
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
        val pc0 = Input(UInt(base.ADDR_WIDTH.W))
        val pc1 = Input(UInt(base.ADDR_WIDTH.W))
        val pc2 = Input(UInt(base.ADDR_WIDTH.W))
        val pc3 = Input(UInt(base.ADDR_WIDTH.W))
    })
    //addPath("./playground/src/resources/CommitAPI.v")
}

class BranchPredAPI extends BlackBox{
    val io = IO(new Bundle{
        val rst = Input(Bool())
        val is_branch = Input(UInt(8.W))
        val branch_pred_err = Input(UInt(8.W))
    })
    //addPath("./playground/src/resources/BranchPredAPI.v")
}