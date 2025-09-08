package cpu.core

import chisel3._
import chisel3.util._
import cpu.config._

class CSRF extends Module {
    val io = IO(new Bundle {
        val csr_ren = Input(Bool())
        val csr_wen = Input(Bool())
        val csr_widx = Input(UInt(12.W))
        val csr_ridx = Input(UInt(12.W))
        val csr_wdata = Input(UInt(base.DATA_WIDTH.W))
        val csr_rdata = Output(UInt(base.DATA_WIDTH.W))
    })

    var mtvec = RegInit((0.U)(base.DATA_WIDTH.W))
    var mstatus = RegInit((0.U)(base.DATA_WIDTH.W))
    var mepc = RegInit((0.U)(base.DATA_WIDTH.W))
    var mcause = RegInit(("h1800".U)(base.DATA_WIDTH.W))

    var csr_rdata = WireInit((0.U)(base.DATA_WIDTH.W))
    csr_rdata := 0.U
    switch (io.csr_ridx) {
        is(CSRIndex.MTVEC.U) {
            csr_rdata := mtvec
        }
        is(CSRIndex.MSTATUS.U) {
            csr_rdata := mstatus
        }
        is(CSRIndex.MEPC.U) {
            csr_rdata := mepc
        }
        is(CSRIndex.MCAUSE.U) {
            csr_rdata := mcause
        }        
    }

    switch (io.csr_widx) {
        is(CSRIndex.MTVEC.U) {
            mtvec := Mux(io.csr_wen, io.csr_wdata, mtvec)
        }
        is(CSRIndex.MSTATUS.U) {
            mstatus := Mux(io.csr_wen, io.csr_wdata, mstatus)
        }
        is(CSRIndex.MEPC.U) {
            mepc := Mux(io.csr_wen, io.csr_wdata, mepc)
        }
        is(CSRIndex.MCAUSE.U) {
            mcause := Mux(io.csr_wen, io.csr_wdata, mcause)
        }        
    }    

    io.csr_rdata := Mux(io.csr_ren, csr_rdata, 0.U)
}