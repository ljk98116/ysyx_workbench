package cpu.core.backend

import chisel3._
import chisel3.util._

import cpu.config._

/* 接收ROB Item和2个操作数，计算结果 */
class ALU extends Module
{
    val io = IO(new Bundle{
        val rat_flush_en = Input(Bool())
        val rob_state = Input(Bool())
        val rob_item_i = Input(new ROBItem)
        val rs1_data_i = Input(UInt(base.DATA_WIDTH.W))
        val rs2_data_i = Input(UInt(base.DATA_WIDTH.W))
        val result = Output(UInt(base.DATA_WIDTH.W))
        /* 结果是否跳转 */
        val branch_target_addr = Output(UInt(base.ADDR_WIDTH.W))
        val areg_wr_addr = Output(UInt(base.AREG_WIDTH.W))
        val preg_wr_addr = Output(UInt(base.PREG_WIDTH.W))
        val valid_o = Output(Bool())
        val rob_id_o = Output(UInt(base.ROBID_WIDTH.W))
    })

    /* pipeline */
    var rob_item_reg = RegInit((0.U).asTypeOf(new ROBItem))
    var rs1_data_reg = RegInit((0.U)(base.DATA_WIDTH.W))
    var rs2_data_reg = RegInit((0.U)(base.DATA_WIDTH.W))

    rob_item_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.rob_item_i, rob_item_reg), 
        0.U.asTypeOf(new ROBItem)
    )
    rs1_data_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.rs1_data_i, rs1_data_reg), 
        0.U
    )
    rs2_data_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.rs2_data_i, rs2_data_reg), 
        0.U
    )

    var result = WireInit((0.U)(base.DATA_WIDTH.W))
    var areg_wr_addr = WireInit((0.U)(base.AREG_WIDTH.W))
    var preg_wr_addr = WireInit((0.U)(base.PREG_WIDTH.W))
    var branch_target_addr = WireInit((0.U)(base.ADDR_WIDTH.W))
    var valid_o = WireInit(false.B)
    var rob_id_o = WireInit((0.U)(base.ROBID_WIDTH.W))

    areg_wr_addr := Mux(rob_item_reg.HasRd, rob_item_reg.rd, 0.U)
    preg_wr_addr := Mux(rob_item_reg.HasRd, rob_item_reg.pd, 0.U)
    valid_o := rob_item_reg.valid
    rob_id_o := rob_item_reg.id

    result := 0.U
    branch_target_addr := 0.U
    switch(rob_item_reg.Opcode){
        is(Opcode.ADDI){
            result := rs1_data_reg + rob_item_reg.Imm
        }
        is(Opcode.AUIPC){
            result := rob_item_reg.pc + rob_item_reg.Imm
        }
        is(Opcode.JAL){
            result := rob_item_reg.pc + 4.U
            branch_target_addr := rob_item_reg.pc + rob_item_reg.Imm
            // to do: 异常检查
        }
        is(Opcode.JALR){
            result := rob_item_reg.pc + 4.U
            branch_target_addr := rs1_data_reg + rob_item_reg.Imm
            // to do: 异常检查         
        }
        is(Opcode.LUI){
            result := rob_item_reg.Imm
        }
        // to do
    }

    /* connect */
    io.result := result
    io.areg_wr_addr := areg_wr_addr
    io.preg_wr_addr := preg_wr_addr
    io.branch_target_addr := branch_target_addr
    io.valid_o := valid_o
    io.rob_id_o := rob_id_o
}