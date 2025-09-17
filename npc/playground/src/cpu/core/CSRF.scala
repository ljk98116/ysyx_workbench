package cpu.core

import chisel3._
import chisel3.util._
import cpu.config._

class CSRF extends Module {
    val io = IO(new Bundle {
        val csr_mtvec_ren = Input(Bool())
        val csr_mtvec_wen = Input(Bool())
        val csr_mtvec_rdata = Output(UInt(base.DATA_WIDTH.W))
        val csr_mtvec_wdata = Input(UInt(base.DATA_WIDTH.W))

        val csr_mstatus_ren = Input(Bool())
        val csr_mstatus_wen = Input(Bool())
        val csr_mstatus_rdata = Output(UInt(base.DATA_WIDTH.W))
        val csr_mstatus_wdata = Input(UInt(base.DATA_WIDTH.W))

        val csr_mepc_ren = Input(Bool())
        val csr_mepc_wen = Input(Bool())
        val csr_mepc_rdata = Output(UInt(base.DATA_WIDTH.W))
        val csr_mepc_wdata = Input(UInt(base.DATA_WIDTH.W))

        val csr_mcause_ren = Input(Bool())
        val csr_mcause_wen = Input(Bool())
        val csr_mcause_rdata = Output(UInt(base.DATA_WIDTH.W))
        val csr_mcause_wdata = Input(UInt(base.DATA_WIDTH.W))        
    })

    var mtvec = RegInit((0.U)(base.DATA_WIDTH.W))
    var mstatus = RegInit((0.U)(base.DATA_WIDTH.W))
    var mepc = RegInit((0.U)(base.DATA_WIDTH.W))
    var mcause = RegInit(("h1800".U)(base.DATA_WIDTH.W))

    io.csr_mtvec_rdata := Mux(io.csr_mtvec_ren, mtvec, 0.U)
    mtvec := Mux(io.csr_mtvec_wen, io.csr_mtvec_wdata, mtvec)

    io.csr_mstatus_rdata := Mux(io.csr_mstatus_ren, mstatus, 0.U)
    mstatus := Mux(io.csr_mstatus_wen, io.csr_mstatus_wdata, mstatus)

    io.csr_mepc_rdata := Mux(io.csr_mepc_ren, mepc, 0.U)
    mepc := Mux(io.csr_mepc_wen, io.csr_mepc_wdata, mepc)

    io.csr_mcause_rdata := Mux(io.csr_mcause_ren, mcause, 0.U)
    mcause := Mux(io.csr_mcause_wen, io.csr_mcause_wdata, mcause)
}