package cpu.core.backend

import chisel3._
import chisel3.util._

import cpu.config._

class RetireRAT extends Module
{
    val io = IO(new Bundle{
        /* RAT读写使能 */
        /* 前置指令有无异常 */
        val rat_flush_en = Input(Bool())
        val exception_mask_front_i = Input(Vec(base.FETCH_WIDTH, Bool()))
        val rat_wen = Input(UInt(base.FETCH_WIDTH.W))
        val rat_waddr = Input(Vec(base.FETCH_WIDTH, UInt(base.AREG_WIDTH.W)))
        val rat_wdata = Input(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))

        val rat_all_data = Output(Vec(1 << base.AREG_WIDTH, UInt((base.PREG_WIDTH + 1).W)))
    })

    var rat_mapping = RegInit(VecInit(
        Seq.tabulate(1 << base.AREG_WIDTH)((i) => ((1 << base.PREG_WIDTH).U)((base.PREG_WIDTH + 1).W))
    ))

    for(i <- 0 until (1 << base.AREG_WIDTH))
    {
        for(j <- 0 until base.FETCH_WIDTH){
            when(io.rat_wen(j) & (io.rat_waddr(j) === i.U) & ~io.exception_mask_front_i(j)){
                rat_mapping(i) := io.rat_wdata(j)
            }
        }
    }

    /* connect */
    for(i <- 0 until 1 << base.AREG_WIDTH){
        io.rat_all_data(i) := rat_mapping(i)
    }
}