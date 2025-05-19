package cpu.core

import chisel3._
import chisel3.util._
import cpu.config._

/* 刷新流水线时的恢复,需要将异常指令后ROB内所有指令的占用的物理寄存器释放 */
class FreeRegBuffer(id : Int) extends Module
{
    val io = IO(new Bundle{
        /* retire stage */
        val rat_flush_en = Input(Bool())
        val inst_valid_retire = Input(Bool())
        val freereg_i = Input(UInt(base.PREG_WIDTH.W))

        /* output */
        val inst_valid_decode = Input(Bool())
        val freereg_o = Output(UInt(base.PREG_WIDTH.W))

        val freeregbuf_empty = Output(Bool())
        val freeregbuf_full = Output(Bool())
    })

    val regnum = (1 << base.PREG_WIDTH) / 4
    val width = log2Ceil(regnum)

    val RegIdSram = SyncReadMem((1 << base.PREG_WIDTH) / 4, UInt(base.PREG_WIDTH.W))
    
    var head = RegInit((0.U)(width.W))
    var tail = RegInit(((regnum - 1).U)(width.W))

    var freeregs_o = WireInit((0.U)(base.PREG_WIDTH.W))

    var rvalid = WireInit(false.B)
    rvalid := io.inst_valid_decode & ~io.freeregbuf_empty

    var wvalid = WireInit(false.B)
    wvalid := io.inst_valid_retire & ~io.freeregbuf_full

    io.freeregbuf_empty := head === tail
    io.freeregbuf_full := tail + 1.U === head

    when(rvalid & ~io.rat_flush_en){
        head := head + 1.U
    }

    when(wvalid & ~io.rat_flush_en){
        tail := tail + 1.U
    }
    
    /* 初始化 */
    when(reset.asBool){
        for(i <- 0 until regnum){
            RegIdSram.write(i.U, (id * 32 + i).U)
        }
    }

    /* 读取 */
    when(io.inst_valid_decode & ~io.freeregbuf_empty){
        io.freereg_o := RegIdSram.read(head)
    }.otherwise{
        io.freereg_o := 0.U
    }

    /* 写入 */
    when(io.inst_valid_retire & ~io.freeregbuf_full){
        RegIdSram.write(tail, io.freereg_i)
    }
}