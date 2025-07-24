package cpu.config

import chisel3._
import chisel3.util._

class BTBItem extends Bundle{
    val V = Bool()
    val BIA = UInt(8.W)
    val BTA = UInt(base.ADDR_WIDTH.W)
}