package cpu.core

import chisel3._
import chisel3.util._
import cpu.config._

/* 遇到分支指令或者可能触发中断异常的指令进行checkpoint记录 */
class ROBIDBuffer(id : Int) extends Module
{
    val io = IO(new Bundle{
        /* retire stage */
        val rat_flush_en = Input(Bool())
        val rat_write_en_retire = Input(Bool())
        val free_robid_i = Input(UInt(base.ROBID_WIDTH.W))

        /* output */
        val rat_write_en_rename = Input(Bool())
        val free_robid_o = Output(UInt(base.ROBID_WIDTH.W))
    })

    val regnum = (1 << base.ROBID_WIDTH) >> 2
    val width = log2Ceil(regnum)
    val free_robids = RegInit(VecInit(
        Seq.tabulate(regnum)((i) => Cat(id.U, i.U(width.W)))
    ))
    
    var head = RegInit((0.U)(width.W))
    var tail = RegInit(((regnum - 1).U)(width.W))

    var free_robid_o = WireInit((0.U)(base.ROBID_WIDTH.W))

    var rd_able = WireInit(true.B)
    var wr_able = WireInit(false.B)

    rd_able := head =/= tail
    wr_able := tail + 1.U =/= head

    for(i <- 0 until regnum){
        when(io.rat_flush_en){
            free_robids(i) := Cat(id.U, i.U(width.W))
        }.elsewhen(io.rat_write_en_retire & wr_able & (i.U === tail)){
            free_robids(i) := io.free_robid_i
        }
    }

    free_robid_o := Mux(rd_able, free_robids(head), 0.U)

    head := Mux(
        rd_able & io.rat_write_en_rename & ~io.rat_flush_en,
        head + 1.U,
        Mux(~io.rat_flush_en, head, 0.U) 
    )

    tail := Mux(
        wr_able & io.rat_write_en_retire & ~io.rat_flush_en,
        tail + 1.U,
        Mux(~io.rat_flush_en, tail, (regnum - 1).U)
    )
    /* connect */
    io.free_robid_o := free_robid_o
}