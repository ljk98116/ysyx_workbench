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
        val csr_data_i = Input(UInt(base.DATA_WIDTH.W))
        val result = Output(UInt(base.DATA_WIDTH.W))

        val csrf_wen = Output(Bool())
        val csrf_widx = Output(UInt(12.W))
        val csrf_wdata = Output(UInt(base.DATA_WIDTH.W))
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
    var csr_data_reg = RegInit((0.U)(base.DATA_WIDTH.W))

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
    csr_data_reg := Mux(
        ~io.rat_flush_en, 
        Mux((io.rob_state === 0.U), io.csr_data_i, csr_data_reg), 
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

    var csrf_wen = WireInit(false.B)
    var csrf_widx = WireInit((0.U)(12.W))
    var csrf_wdata = WireInit((0.U)(base.DATA_WIDTH.W))

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

    csrf_wen := false.B
    csrf_widx := 0.U
    csrf_wdata := 0.U

    switch (rob_item_reg.funct3) {
        is(Funct3.CSRRW) {

        }
        is(Funct3.CSRRS) {

        }
        is("b000".U) {
            switch (rob_item_reg.Imm) {

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
    io.valid_o := valid_o
    io.rob_id_o := rob_id_o

    io.csrf_wen := csrf_wen
    io.csrf_widx := csrf_widx
    io.csrf_wdata := csrf_wdata   
}