package cpu.core.frontend

import chisel3._
import chisel3.util._

import cpu.config._

/* 发送ROB到ROB队列以及各个种类的发射保留站 */
class Dispatch extends Module
{
    var io = IO(new Bundle {
        val rob_item_i = Input(Vec(base.FETCH_WIDTH, new ROBItem))
        
    })
}