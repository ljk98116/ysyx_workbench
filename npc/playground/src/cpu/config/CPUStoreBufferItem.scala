package cpu.config

import chisel3._
import chisel3.util._

class StoreBufferItem extends Bundle
{
    var valid = Bool()
    var wmask = UInt(8.W)
    var wdata = UInt(base.DATA_WIDTH.W)
    var rob_id = UInt(base.ROBID_WIDTH.W)
    var agu_result = UInt(base.ADDR_WIDTH.W)
    var rdy = Bool()
}