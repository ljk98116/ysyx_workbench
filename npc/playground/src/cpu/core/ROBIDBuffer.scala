package cpu.core

import chisel3._
import chisel3.util._
import cpu.config._

/* 遇到分支指令或者可能触发中断异常的指令进行checkpoint记录 */
class ROBIDBuffer extends Module
{
    val io = IO(new Bundle{
        /* retire stage */
        val rat_flush_en = Input(Bool())

        val retire_cnt = Input(UInt((log2Ceil(base.FETCH_WIDTH) + 1).W))
        val retire_rdy_mask = Input(UInt(base.FETCH_WIDTH.W))
        val freeid_i = Input(Vec(base.FETCH_WIDTH, UInt(base.ROBID_WIDTH.W)))

        /* output */
        val inst_valid_cnt = Input(UInt((log2Ceil(base.FETCH_WIDTH) + 1).W))
        /* 输出头部fetch_width个ID */
        val freeid_o = Output(Vec(base.FETCH_WIDTH, UInt(base.ROBID_WIDTH.W)))

        val rd_able = Output(Bool())
        val wr_able = Output(Bool())
    })

    val idnum = (1 << base.ROBID_WIDTH)
    val width = log2Ceil(idnum)

    val FreeIdReg = RegInit(VecInit(
        Seq.tabulate(idnum)((i) => {i.U})
    ))
    
    var head = RegInit((0.U)(width.W))
    var tail = RegInit(((idnum - 1).U)(width.W))

    var free_id_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.ROBID_WIDTH.W))
    ))

    io.rd_able := head =/= tail & head + 1.U =/= tail & head + 2.U =/= tail & head + 3.U =/= tail
    io.wr_able := tail + 1.U =/= head & tail + 2.U =/= head & tail + 3.U =/= head & tail + 4.U =/= head

    head := Mux(io.rd_able & ~io.rat_flush_en, head + io.inst_valid_cnt, head)
    tail := Mux(io.wr_able, tail + io.retire_cnt, tail)

    for(i <- 0 until base.FETCH_WIDTH){
        free_id_o(i) := FreeIdReg(head + i.U)
    }

    for(i <- 0 until base.FETCH_WIDTH){
        when(io.retire_rdy_mask(i)){
            FreeIdReg(tail + i.U) := io.freeid_i(i)
        }
    }

    /* connect */
    io.freeid_o := free_id_o
}