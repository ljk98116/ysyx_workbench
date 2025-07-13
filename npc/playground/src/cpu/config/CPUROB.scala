package cpu.config

import chisel3._
import chisel3.util._
import cpu.config.base.PREG_WIDTH

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
    var ps1 = UInt((base.PREG_WIDTH + 1).W)
    var ps2 = UInt((base.PREG_WIDTH + 1).W)
    var pd = UInt(base.PREG_WIDTH.W)
    var oldpd = UInt((base.PREG_WIDTH + 1).W)
    var rdy1 = Bool()
    var rdy2 = Bool()
    var rdy  = Bool()
    var isBranch = Bool()
    var isStore = Bool()
    var isLoad = Bool()
    var hasException = Bool()
    var ExceptionType = UInt(8.W)
    var targetBrAddr = UInt(base.ADDR_WIDTH.W)
    var reg_wb_data = UInt(base.DATA_WIDTH.W)
    /* 如果是load指令，对应的前置store指令 */
    var storeIdx = UInt((base.ROBID_WIDTH + 1).W)
}