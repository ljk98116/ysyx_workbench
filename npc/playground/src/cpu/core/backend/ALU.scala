package cpu.core.backend

import chisel3._
import chisel3.util._

import cpu.config._

/* 接收ROB Item和2个操作数，计算结果 */
class ALU extends Module
{
    val io = IO(new Bundle{
        val rat_flush_en = Input(Bool())
        val rob_state = Input(UInt(2.W))
        val rob_item_i = Input(new ROBItem)
        val rs1_data_i = Input(UInt(base.DATA_WIDTH.W))
        val rs2_data_i = Input(UInt(base.DATA_WIDTH.W))
        val result = Output(UInt(base.DATA_WIDTH.W))
        /* 结果是否跳转 */
        val branch_en = Output(Bool())
        val branch_target_addr = Output(UInt(base.ADDR_WIDTH.W))
        val pc_o = Output(UInt(base.ADDR_WIDTH.W))
        val areg_wr_addr = Output(UInt(base.AREG_WIDTH.W))
        val preg_wr_addr = Output(UInt(base.PREG_WIDTH.W))
        val valid_o = Output(Bool())
        val rob_id_o = Output(UInt(base.ROBID_WIDTH.W))
        val has_exception = Output(Bool())
        val exception_type = Output(UInt(8.W))
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
    var branch_target_addr = WireInit((0.U)(base.ADDR_WIDTH.W))
    var valid_o = WireInit(false.B)
    var rob_id_o = WireInit((0.U)(base.ROBID_WIDTH.W))
    var has_exception = WireInit(false.B)
    var exception_type = WireInit((0.U)(8.W))

    areg_wr_addr := Mux(rob_item_reg.HasRd, rob_item_reg.rd, 0.U)
    preg_wr_addr := Mux(rob_item_reg.HasRd, rob_item_reg.pd, 0.U)
    valid_o := rob_item_reg.valid & (
        rob_item_reg.HasRd | rob_item_reg.HasRs1
    )
    rob_id_o := rob_item_reg.id

    result := 0.U
    branch_target_addr := 0.U
    has_exception := false.B
    exception_type := ExceptionType.NORMAL.U
    io.branch_en   := false.B
    io.pc_o := rob_item_reg.pc
    switch(rob_item_reg.Opcode){
        is(Opcode.ADD){
            switch(rob_item_reg.funct7){
                is(Funct7.ADD){
                    result := rs1_data_reg + rs2_data_reg
                }
                is(Funct7.SUB){
                    result := rs1_data_reg - rs2_data_reg
                }
            }
        }
        is(Opcode.AUIPC){
            result := rob_item_reg.pc + rob_item_reg.Imm
        }
        is(
            Opcode.ADDI,
            // Opcode.SLLI,
            // Opcode.SRLI,
            // Opcode.SRAI,
        ){
            switch(rob_item_reg.funct3){
                is(Funct3.ADDI){
                    result := rs1_data_reg + rob_item_reg.Imm
                }
                is(Funct3.SLLI){
                    switch(rob_item_reg.funct7){
                        is(Funct7.SLLI){
                            result := rs1_data_reg << rob_item_reg.Imm(4, 0)
                        }
                    }
                }
                is(Funct3.SRLI){
                    switch(rob_item_reg.funct7){
                        is(Funct7.SRLI){
                            result := rs1_data_reg >> rob_item_reg.Imm(4, 0)
                        }
                        is(Funct7.SRAI){
                            result := (rs1_data_reg.asSInt >> rob_item_reg.Imm(4, 0)).asUInt
                        }
                    }
                }
                is(Funct3.SLTIU){
                    result := Mux(rs1_data_reg === 0.U, 1.U, 0.U)
                }
            }
        }
        is(Opcode.JAL){
            result := rob_item_reg.pc + 4.U
            branch_target_addr := rob_item_reg.pc + rob_item_reg.Imm
            has_exception := ~rob_item_reg.branch_res | (
                rob_item_reg.branch_pred_addr =/= (rob_item_reg.pc + rob_item_reg.Imm)
            )
            exception_type := Mux(
                ~rob_item_reg.branch_res | (
                    rob_item_reg.branch_pred_addr =/= (rob_item_reg.pc + rob_item_reg.Imm)
                ),
                ExceptionType.BRANCH_PREDICTION_ERROR.U,
                ExceptionType.NORMAL.U
            )
            io.branch_en := true.B
            // to do: 异常检查
        }
        is(Opcode.JALR){
            result := rob_item_reg.pc + 4.U
            branch_target_addr := rs1_data_reg + rob_item_reg.Imm
            has_exception := ~rob_item_reg.branch_res | (
                rob_item_reg.branch_pred_addr =/= (rs1_data_reg + rob_item_reg.Imm)
            )
            exception_type := Mux(
                ~rob_item_reg.branch_res | (
                    rob_item_reg.branch_pred_addr =/= (rs1_data_reg + rob_item_reg.Imm)
                ),
                ExceptionType.BRANCH_PREDICTION_ERROR.U,
                ExceptionType.NORMAL.U
            )
            io.branch_en := true.B
            // to do: 异常检查         
        }
        is(Opcode.LUI){
            result := rob_item_reg.Imm
        }
        is(Opcode.BEQ){
            io.branch_en := true.B
            switch(rob_item_reg.funct3){
                is(Funct3.BEQ){
                    branch_target_addr := Mux(
                        rs1_data_reg === rs2_data_reg,
                        rob_item_reg.pc + rob_item_reg.Imm,
                        rob_item_reg.pc + 4.U
                    )
                    has_exception := (rob_item_reg.branch_res ^ (rs1_data_reg === rs2_data_reg)) | (
                        rob_item_reg.branch_pred_addr =/= (rob_item_reg.pc + rob_item_reg.Imm)
                    )
                    exception_type := Mux(
                        (rob_item_reg.branch_res ^ (rs1_data_reg === rs2_data_reg)) | (
                            rob_item_reg.branch_pred_addr =/= (rob_item_reg.pc + rob_item_reg.Imm)
                        ),
                        ExceptionType.BRANCH_PREDICTION_ERROR.U,
                        ExceptionType.NORMAL.U
                    )
                }
                is(Funct3.BNE){
                    branch_target_addr := Mux(
                        rs1_data_reg =/= rs2_data_reg,
                        rob_item_reg.pc + rob_item_reg.Imm,
                        rob_item_reg.pc + 4.U
                    )
                    has_exception := (rob_item_reg.branch_res ^ (rs1_data_reg =/= rs2_data_reg)) | (
                        rob_item_reg.branch_pred_addr =/= (rob_item_reg.pc + rob_item_reg.Imm)
                    )
                    exception_type := Mux(
                        (rob_item_reg.branch_res ^ (rs1_data_reg =/= rs2_data_reg)) | (
                            rob_item_reg.branch_pred_addr =/= (rob_item_reg.pc + rob_item_reg.Imm)
                        ),
                        ExceptionType.BRANCH_PREDICTION_ERROR.U,
                        ExceptionType.NORMAL.U
                    )                   
                }
            }
        }
        // to do
    }

    /* connect */
    io.has_exception := has_exception
    io.exception_type := exception_type
    io.result := Mux(rob_item_reg.rd =/= 0.U, result, 0.U)
    io.areg_wr_addr := areg_wr_addr
    io.preg_wr_addr := preg_wr_addr
    io.branch_target_addr := branch_target_addr
    io.valid_o := valid_o
    io.rob_id_o := rob_id_o
}