package cpu.core

import chisel3._
import chisel3.util._
import cpu.config._

class ROBIDBuffer(id : Int) extends Module
{
    val io = IO(new Bundle{
        /* retire stage */
        val inst_valid_retire = Input(Bool())
        val freeid_i = Input(UInt(base.ROBID_WIDTH.W))

        /* output */
        val inst_valid_rename1 = Input(Bool())
        val freeid_o = Output(UInt(base.ROBID_WIDTH.W))

        val freeidbuf_empty = Output(Bool())
        val freeidbuf_full = Output(Bool())
    })

    val idnum = (1 << base.ROBID_WIDTH) / 4
    val width = log2Ceil(idnum)

    val FreeIdSram = SyncReadMem((1 << base.ROBID_WIDTH) / 4, UInt(base.ROBID_WIDTH.W))
    
    var head = RegInit((0.U)(width.W))
    var tail = RegInit(((idnum - 1).U)(width.W))

    var freeregs_o = WireInit((0.U)(base.ROBID_WIDTH.W))

    var rvalid = WireInit(false.B)
    rvalid := io.inst_valid_rename1 & ~io.freeidbuf_empty

    var wvalid = WireInit(false.B)
    wvalid := io.inst_valid_retire & ~io.freeidbuf_full

    io.freeidbuf_empty := head === tail
    io.freeidbuf_full := tail + 1.U === head

    when(rvalid){
        head := head + 1.U
    }

    when(wvalid){
        tail := tail + 1.U
    }
    
    /* 初始化 */
    when(reset.asBool){
        for(i <- 0 until idnum){
            FreeIdSram.write(i.U, (id * 32 + i).U)
        }
    }

    /* 读取 */
    when(io.inst_valid_rename1 & ~io.freeidbuf_empty){
        io.freeid_o := FreeIdSram.read(head)
    }.otherwise{
        io.freeid_o := 0.U
    }

    /* 写入 */
    when(io.inst_valid_retire & ~io.freeidbuf_full){
        FreeIdSram.write(tail, io.freeid_i)
    }
}