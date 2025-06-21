package cpu.config

import chisel3._
import chisel3.util._

/* 定义核内总线信息 */
class ALU_CDB_Channel extends Bundle{
    val arch_reg_id = UInt(base.AREG_WIDTH.W)
    val phy_reg_id = UInt(base.PREG_WIDTH.W)
    val valid = Bool()
    val reg_wr_data = UInt(base.DATA_WIDTH.W)
    val rob_id = UInt(base.ROBID_WIDTH.W)
    val branch_target_addr = UInt(base.ADDR_WIDTH.W)
    val has_exception = Bool()
    val exception_type = UInt(8.W)
}

class AGU_CDB_Channel extends Bundle{
    val arch_reg_id = UInt(base.AREG_WIDTH.W)
    val phy_reg_id = UInt(base.PREG_WIDTH.W)
    val valid = Bool()
    val reg_wr_data = UInt(base.DATA_WIDTH.W)
    val rob_id = UInt(base.ROBID_WIDTH.W)
    val wdata = UInt(base.DATA_WIDTH.W)
    val wmask = UInt(8.W)
    val has_exception = Bool()
    val exception_type = UInt(8.W)
}

class CDB extends Bundle{
    val alu_channel = Vec(base.ALU_NUM, new ALU_CDB_Channel)
    val agu_channel = Vec(base.AGU_NUM, new AGU_CDB_Channel)
}