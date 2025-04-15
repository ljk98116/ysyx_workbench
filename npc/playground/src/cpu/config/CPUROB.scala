package cpu.config

import chisel3._
import chisel3.util._

class ROBItem extends Bundle
{
    val pc = UInt(base.ADDR_WIDTH.W)
    val valid = Bool()

    var Imm = UInt(base.DATA_WIDTH.W)
    var Opcode = UInt(7.W)
    var rs1 = UInt(base.AREG_WIDTH.W)
    var rs2 = UInt(base.AREG_WIDTH.W)
    var rd = UInt(base.AREG_WIDTH.W)
    var funct3 = UInt(3.W)
    var funct7 = UInt(7.W)
    var shamt = UInt(base.AREG_WIDTH.W)
    var Type = UInt(3.W)
    var HasRs1 = Bool()
    var HasRs2 = Bool()
    var HasRd = Bool()
    /* rob id */
    var id = UInt(base.ROBID_WIDTH.W)
    var ps1 = UInt(base.PREG_WIDTH.W)
    var ps2 = UInt(base.PREG_WIDTH.W)
    var pd = UInt(base.PREG_WIDTH.W)
    var rdy1 = Bool()
    var rdy2 = Bool()
    var rdy  = Bool()
}