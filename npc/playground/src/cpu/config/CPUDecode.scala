package cpu.config

import chisel3._
import chisel3.util._

object Opcode
{
    val LUI     = "b0110111".U
    val AUIPC   = "b0010111".U
    val ADDI    = "b0010011".U
    val JAL     = "b1101111".U
    val JALR    = "b1100111".U
    val SW      = "b0100011".U
}

object Funct3
{
    val JALR    = "b000".U
    val ADDI    = "b000".U
    val SW      = "b010".U
}

/* 符号扩展 */
object Ext{
    def sext(num : UInt, pos : Int): UInt = {
        return Cat(Fill(base.DATA_WIDTH - pos - 1, num(pos)), num(pos, 0))
    }
    def zext(num : UInt, pos : Int): UInt = {
        return Cat(Fill(base.DATA_WIDTH - pos - 1, false.B), num(pos, 0))
    }
}

/* 从指令中截取立即数 */
object Imm{
    def ImmI(inst : UInt):UInt = {
        return Ext.sext(inst(31, 20), 11)
    }
    def ImmU(inst : UInt):UInt = {
        return inst(31, 12) ## 0.U(12.W)
    }
    def ImmS(inst : UInt): UInt = {
        return Fill(20, inst(31)) ## inst(31, 25) ## inst(11, 7)
    }
    def ImmSB(inst : UInt): UInt = {
        return Fill(19, inst(31)) ## inst(7) ## inst(30, 25) ## inst(11, 8) ## false.B
    }
    def ImmUJ(inst : UInt): UInt = {
        return Fill(11, inst(31)) ## inst(19, 12) ## inst(20) ## inst(31, 21) ## false.B
    }
}

object InstType
{
    val TYPER   = "b000".U
    val TYPEI   = "b001".U
    val TYPES   = "b010".U
    val TYPESB  = "b011".U
    val TYPEU   = "b100".U
    val TYPEUJ  = "b101".U
}

class DecodeRes extends Bundle
{
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
    var IsBranch = Bool()
    var IsStore = Bool()
    var IsLoad = Bool()
}
