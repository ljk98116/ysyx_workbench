package cpu.config

import chisel3._
import chisel3.util._

class LSQ_Item extends Bundle
{
    var valid = Bool()
    var LS_FLAG = Bool()
    var rw_mask = UInt(4.W)
    var rw_addr = UInt(base.ADDR_WIDTH.W)
    var rw_data = UInt(base.DATA_WIDTH.W)
    var rob_id = UInt(base.ROBID_WIDTH.W)
}