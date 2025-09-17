package cpu.core.backend

import chisel3._
import chisel3.util._

import cpu.config._

class SysEXU extends Module {
    val io = IO(new Bundle{
        val rat_flush_en = Input(Bool())
        val rob_state = Input(UInt(2.W))
        val rob_item_i = Input(new ROBItem)
        val rs1_data_i = Input(UInt(base.DATA_WIDTH.W))

        val csr_mtvec_rdata_i = Input(UInt(base.DATA_WIDTH.W))
        val csr_mstatus_rdata_i = Input(UInt(base.DATA_WIDTH.W))
        val csr_mepc_rdata_i = Input(UInt(base.DATA_WIDTH.W))
        val csr_mcause_rdata_i = Input(UInt(base.DATA_WIDTH.W))

        val result = Output(UInt(base.DATA_WIDTH.W))

        val csr_mtvec_wen = Output(Bool())
        val csr_mtvec_wdata = Output(UInt(base.DATA_WIDTH.W))
        val csr_mstatus_wen = Output(Bool())
        val csr_mstatus_wdata = Output(UInt(base.DATA_WIDTH.W))
        val csr_mepc_wen = Output(Bool())
        val csr_mepc_wdata = Output(UInt(base.DATA_WIDTH.W))
        val csr_mcause_wen = Output(Bool())
        val csr_mcause_wdata = Output(UInt(base.DATA_WIDTH.W))

        /* 结果是否跳转 */
        val branch_en = Output(Bool())
        val branch_target_addr = Output(UInt(base.ADDR_WIDTH.W))
        val btb_idx_o = Output(UInt(base.PHTID_WIDTH.W))
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

    var csr_mtvec_reg = RegInit((0.U)(base.DATA_WIDTH.W))
    var csr_mstatus_reg = RegInit((0.U)(base.DATA_WIDTH.W))
    var csr_mepc_reg = RegInit((0.U)(base.DATA_WIDTH.W))
    var csr_mcause_reg = RegInit((0.U)(base.DATA_WIDTH.W))

    var mode = RegInit((0.U)(2.W))

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
    csr_mtvec_reg := Mux(
        ~io.rat_flush_en, 
        Mux((io.rob_state === 0.U), io.csr_mtvec_rdata_i, csr_mtvec_reg), 
        0.U
    )
    csr_mstatus_reg := Mux(
        ~io.rat_flush_en, 
        Mux((io.rob_state === 0.U), io.csr_mstatus_rdata_i, csr_mstatus_reg), 
        0.U
    )
    csr_mepc_reg := Mux(
        ~io.rat_flush_en, 
        Mux((io.rob_state === 0.U), io.csr_mepc_rdata_i, csr_mepc_reg), 
        0.U
    )
    csr_mcause_reg := Mux(
        ~io.rat_flush_en, 
        Mux((io.rob_state === 0.U), io.csr_mcause_rdata_i, csr_mcause_reg), 
        0.U
    )
    var result = WireInit((0.U)(base.DATA_WIDTH.W))
    var areg_wr_addr = WireInit((0.U)(base.AREG_WIDTH.W))
    var preg_wr_addr = WireInit((0.U)(base.PREG_WIDTH.W))
    var branch_target_addr = WireInit((0.U)(base.ADDR_WIDTH.W))
    var branch_en = WireInit(false.B)
    var valid_o = WireInit(false.B)
    var rob_id_o = WireInit((0.U)(base.ROBID_WIDTH.W))
    var has_exception = WireInit(false.B)
    var exception_type = WireInit((0.U)(8.W))

    var csr_mtvec_wen = WireInit(false.B)
    var csr_mtvec_wdata = WireInit((0.U)(base.DATA_WIDTH.W))
    var csr_mstatus_wen = WireInit(false.B)
    var csr_mstatus_wdata = WireInit((0.U)(base.DATA_WIDTH.W))
    var csr_mepc_wen = WireInit(false.B)
    var csr_mepc_wdata = WireInit((0.U)(base.DATA_WIDTH.W))
    var csr_mcause_wen = WireInit(false.B)
    var csr_mcause_wdata = WireInit((0.U)(base.DATA_WIDTH.W))

    areg_wr_addr := Mux(rob_item_reg.HasRd, rob_item_reg.rd, 0.U)
    preg_wr_addr := Mux(rob_item_reg.HasRd, rob_item_reg.pd, 0.U)
    valid_o := rob_item_reg.valid
    rob_id_o := rob_item_reg.id

    result := 0.U
    branch_target_addr := 0.U
    has_exception := false.B
    exception_type := ExceptionType.NORMAL.U
    io.branch_en   := false.B
    io.pc_o := rob_item_reg.pc

    csr_mtvec_wen := false.B
    csr_mtvec_wdata := 0.U
    csr_mstatus_wen := false.B
    csr_mstatus_wdata := 0.U
    csr_mepc_wen := false.B
    csr_mepc_wdata := 0.U
    csr_mcause_wen := false.B
    csr_mcause_wdata := 0.U

    switch (rob_item_reg.funct3) {
        is(Funct3.CSRRW) {
            switch (rob_item_reg.Imm) {
                is (CSRIndex.MCAUSE.U) {
                    csr_mcause_wen := true.B & rob_item_reg.valid
                    csr_mcause_wdata := rs1_data_reg
                    result := csr_mcause_reg
                }
                is (CSRIndex.MSTATUS.U) {
                    csr_mstatus_wen := true.B & rob_item_reg.valid
                    csr_mstatus_wdata := rs1_data_reg
                    result := csr_mstatus_reg
                }
                is (CSRIndex.MTVEC.U) {
                    csr_mtvec_wen := true.B & rob_item_reg.valid
                    csr_mtvec_wdata := rs1_data_reg
                    result := csr_mtvec_reg
                }
                is (CSRIndex.MEPC.U) {
                    csr_mepc_wen := true.B & rob_item_reg.valid
                    csr_mepc_wdata := rs1_data_reg
                    result := csr_mepc_reg
                }
            }
        }
        is(Funct3.CSRRS) {
            switch (rob_item_reg.Imm) {
                is (CSRIndex.MCAUSE.U) {
                    csr_mcause_wen := true.B
                    csr_mcause_wdata := rs1_data_reg | csr_mcause_reg
                    result := csr_mcause_reg
                }
                is (CSRIndex.MSTATUS.U) {
                    csr_mstatus_wen := true.B
                    csr_mstatus_wdata := rs1_data_reg | csr_mstatus_reg
                    result := csr_mstatus_reg
                }
                is (CSRIndex.MTVEC.U) {
                    csr_mtvec_wen := true.B
                    csr_mtvec_wdata := rs1_data_reg | csr_mtvec_reg
                    result := csr_mtvec_reg
                }
                is (CSRIndex.MEPC.U) {
                    csr_mepc_wen := true.B
                    csr_mepc_wdata := rs1_data_reg | csr_mepc_reg
                    result := csr_mepc_reg
                }
            }
        }
        is("b000".U) {
            switch (rob_item_reg.Imm) {
                // mret
                is ("b001100000010".U) {
                    has_exception := (~rob_item_reg.branch_res | (
                        rob_item_reg.branch_pred_addr =/= (csr_mepc_reg + 4.U)
                    )) & rob_item_reg.valid
                    exception_type := Mux(
                        ~rob_item_reg.branch_res | (
                            rob_item_reg.branch_pred_addr =/= (csr_mepc_reg + 4.U)
                        ),
                        ExceptionType.BRANCH_PREDICTION_ERROR.U,
                        ExceptionType.NORMAL.U
                    )
                    csr_mstatus_wen := true.B & rob_item_reg.valid
                    csr_mstatus_wdata := Cat(
                        csr_mstatus_reg(31, 8), // 31: 8
                        true.B, // 7
                        csr_mstatus_reg(6, 4), // 6:4
                        csr_mstatus_reg(7), // 3
                        csr_mstatus_reg(2, 0) // 2:0
                    ) 
                    mode := csr_mstatus_reg(12, 11)
                    branch_en := true.B & rob_item_reg.valid
                    branch_target_addr := csr_mepc_reg + 4.U               
                }
                is ("b000000000000".U) {
                    csr_mepc_wen := true.B & rob_item_reg.valid
                    csr_mepc_wdata := rob_item_reg.pc
                    csr_mcause_wen := true.B & rob_item_reg.valid
                    csr_mcause_wdata := rs1_data_reg
                    has_exception := (~rob_item_reg.branch_res | (
                        rob_item_reg.branch_pred_addr =/= (csr_mtvec_reg)
                    )) & rob_item_reg.valid
                    exception_type := Mux(
                        ~rob_item_reg.branch_res | (
                            rob_item_reg.branch_pred_addr =/= csr_mtvec_reg
                        ),
                        ExceptionType.BRANCH_PREDICTION_ERROR.U,
                        ExceptionType.NORMAL.U
                    ) 
                    mode := csr_mstatus_reg(12, 11)
                    csr_mstatus_wen := true.B & rob_item_reg.valid
                    csr_mstatus_wdata := Cat(
                        csr_mstatus_reg(31, 13), // 31: 13
                        "b11".U, // 12:11
                        csr_mstatus_reg(10, 8),
                        csr_mstatus_reg(3), // 7
                        csr_mstatus_reg(6, 4), // 6:4
                        false.B, // 3
                        csr_mstatus_reg(2, 0) // 2:0
                    )
                    branch_en := true.B & rob_item_reg.valid
                    branch_target_addr := csr_mtvec_reg                  
                }
            }
        }
    }    

    /* connect */
    io.btb_idx_o := rob_item_reg.btb_idx
    io.has_exception := has_exception
    io.exception_type := exception_type
    io.result := Mux(rob_item_reg.rd =/= 0.U, result, 0.U)
    io.areg_wr_addr := areg_wr_addr
    io.preg_wr_addr := preg_wr_addr
    io.branch_target_addr := branch_target_addr
    io.branch_en := branch_en
    io.valid_o := valid_o
    io.rob_id_o := rob_id_o

    io.csr_mtvec_wen := csr_mtvec_wen
    io.csr_mtvec_wdata := csr_mtvec_wdata

    io.csr_mstatus_wen := csr_mstatus_wen
    io.csr_mstatus_wdata := csr_mstatus_wdata

    io.csr_mepc_wen := csr_mepc_wen
    io.csr_mepc_wdata := csr_mepc_wdata

    io.csr_mcause_wen := csr_mcause_wen
    io.csr_mcause_wdata := csr_mcause_wdata
        
}