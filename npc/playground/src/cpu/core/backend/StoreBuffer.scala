package cpu.core.backend

import cpu.config._
import chisel3._
import chisel3.util._

/* store指令存储阵列*/
/* 写ROB时同时写入，根据总线消息维护对应信息 */
class StoreBuffer(size : Int) extends Module{
    val io = IO(new Bundle{
        
    })
}

