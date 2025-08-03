package cpu.core.backend

import chisel3._
import chisel3.util._

import cpu.config._

/* 接收ROB Item和2个操作数，计算目标存储器地址 */
class AGU extends Module
{
    val io = IO(new Bundle{
        val rat_flush_en = Input(Bool())
        val rob_state = Input(UInt(2.W))
        val rob_item_i = Input(new ROBItem)
        val rs1_data_i = Input(UInt(base.DATA_WIDTH.W))
        val rs2_data_i = Input(UInt(base.DATA_WIDTH.W))
        val result = Output(UInt(base.DATA_WIDTH.W))
        val rob_item_o = Output(new ROBItem)
        val areg_wr_addr = Output(UInt(base.AREG_WIDTH.W))
        val preg_wr_addr = Output(UInt(base.PREG_WIDTH.W))
        val mem_wr_data = Output(UInt(base.DATA_WIDTH.W))
        val mem_rw_mask = Output(UInt(8.W))
        val valid = Output(Bool())
    })

    /* pipeline */
    var rob_item_reg = RegInit((0.U).asTypeOf(new ROBItem))
    var rs1_data_reg = RegInit((0.U)(base.DATA_WIDTH.W))
    var rs2_data_reg = RegInit((0.U)(base.DATA_WIDTH.W))

    rob_item_reg := Mux(
        ~io.rat_flush_en, 
        Mux((io.rob_state === 0.U), io.rob_item_i, rob_item_reg), 
        0.U.asTypeOf(new ROBItem)
    )
    rs1_data_reg := Mux(
        ~io.rat_flush_en, 
        Mux((io.rob_state === 0.U), io.rs1_data_i, rs1_data_reg), 
        0.U
    )
    rs2_data_reg := Mux(
        ~io.rat_flush_en, 
        Mux((io.rob_state === 0.U), io.rs2_data_i, rs2_data_reg), 
        0.U
    )

    var result = WireInit((0.U)(base.DATA_WIDTH.W))
    var areg_wr_addr = WireInit((0.U)(base.AREG_WIDTH.W))
    var preg_wr_addr = WireInit((0.U)(base.PREG_WIDTH.W))
    var mem_wr_data = WireInit((0.U)(base.DATA_WIDTH.W))
    var mem_rw_mask = WireInit((0.U)(8.W))
    var rob_item_o = WireInit((0.U).asTypeOf(new ROBItem))

    areg_wr_addr := Mux(rob_item_reg.HasRd, rob_item_reg.rd, 0.U)
    preg_wr_addr := Mux(rob_item_reg.HasRd, rob_item_reg.pd, 0.U)
    rob_item_o   := rob_item_reg

    result := 0.U
    mem_wr_data := 0.U
    mem_rw_mask := 0.U
    switch(rob_item_reg.Opcode){
        is(Opcode.SW){

            switch(rob_item_reg.funct3){
                is(Funct3.SB){
                    result := (rs1_data_reg + rob_item_reg.Imm) & "hFFFF_FFFC".U
                    mem_wr_data := rs2_data_reg(7, 0)
                    mem_rw_mask := "b0001".U
                }
                is(Funct3.SH){
                    result := (rs1_data_reg + rob_item_reg.Imm) & "hFFFF_FFFC".U
                    mem_wr_data := rs2_data_reg(15, 0)
                    mem_rw_mask := "b0011".U
                }
                is(Funct3.SW){
                    result := (rs1_data_reg + rob_item_reg.Imm) & "hFFFF_FFFC".U
                    mem_wr_data := rs2_data_reg
                    mem_rw_mask := "b1111".U
                }
            }
        }
        is(Opcode.LW){
            switch(rob_item_reg.funct3){
                is(Funct3.LB, Funct3.LBU){
                    result := rs1_data_reg + rob_item_reg.Imm
                    mem_rw_mask := "b0001".U
                }
                is(Funct3.LH, Funct3.LHU){
                    result := (rs1_data_reg + rob_item_reg.Imm) & "hFFFF_FFFE".U
                    mem_rw_mask := "b0011".U                    
                }
                is(Funct3.LW){
                    result := (rs1_data_reg + rob_item_reg.Imm) & "hFFFF_FFFC".U
                    mem_rw_mask := "b1111".U
                }
            }
        }
    }

    /* connect */
    io.result := Mux(rob_item_reg.valid, result, 0.U)
    io.areg_wr_addr := areg_wr_addr
    io.preg_wr_addr := preg_wr_addr
    io.mem_wr_data := Mux(rob_item_reg.valid, mem_wr_data, 0.U)
    io.mem_rw_mask := Mux(rob_item_reg.valid, mem_rw_mask, 0.U)
    io.rob_item_o  := rob_item_o
    io.valid       := rob_item_reg.valid
}