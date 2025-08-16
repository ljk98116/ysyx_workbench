package cpu.config

import chisel3._
import chisel3.util._

class BTBItem extends Bundle{
    val V = Bool()
    val BIA = UInt(8.W)
    val BTA = UInt(base.ADDR_WIDTH.W)
    /* 上一条分支指令的ROBID，分支预测失败恢复BTB状态, to do */
    val BRID = UInt(base.ROBID_WIDTH.W)
    val PredBTA = UInt(base.ADDR_WIDTH.W)
}