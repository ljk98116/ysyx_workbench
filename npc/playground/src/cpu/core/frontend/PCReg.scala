package cpu.core.frontend

import chisel3._
import chisel3.util._
import cpu.config._

class PCReg extends Module
{
    val io = IO(new Bundle{
        val pc_o = Output(UInt(base.ADDR_WIDTH.W))
        val inst_valid_mask_o = Output(UInt(base.FETCH_WIDTH.W))
        val inst_valid_cnt_o = Output(UInt(log2Ceil(base.FETCH_WIDTH).W))
    })

    var pc_reg = RegInit((base.RESET_VECTOR.U)(base.ADDR_WIDTH.W))
    var inst_valid_mask = WireInit((0.U)(base.FETCH_WIDTH.W))
    var inst_valid_cnt = WireInit((0.U)(log2Ceil(base.FETCH_WIDTH).W))
    var nextpc = WireInit((0.U)(base.ADDR_WIDTH.W))
    switch(pc_reg(3, 0))
    {
        is(0.U){
            inst_valid_mask := "b1111".U
            inst_valid_cnt  := 4.U
            nextpc := pc_reg + 16.U
        }
        is(4.U){
            inst_valid_mask := "b0111".U
            inst_valid_cnt  := 3.U
            nextpc := pc_reg + 12.U
        }
        is(8.U){
            inst_valid_mask := "b0011".U
            inst_valid_cnt  := 2.U
            nextpc := pc_reg + 8.U
        }
        is(12.U){
            inst_valid_mask := "b0001".U
            inst_valid_cnt  := 1.U
            nextpc := pc_reg + 4.U
        }
    }
    pc_reg := nextpc

    io.pc_o := pc_reg
    io.inst_valid_mask_o := inst_valid_mask
    io.inst_valid_cnt_o  := inst_valid_cnt
}