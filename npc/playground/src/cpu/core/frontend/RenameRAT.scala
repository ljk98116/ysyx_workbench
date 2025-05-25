package cpu.core.frontend

import chisel3._
import chisel3.util._

import cpu.config._

class RenameRAT extends Module
{
    val io = IO(new Bundle{
        /* RAT读写使能 */
        val rat_wen = Input(UInt(base.FETCH_WIDTH.W))
        val rat_waddr = Input(Vec(base.FETCH_WIDTH, UInt(base.AREG_WIDTH.W)))
        val rat_wdata = Input(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))
        /* 读取rs1, rs2以及以前的rd */
        val rat_ren = Input(UInt((base.FETCH_WIDTH * 3).W))
        val rat_raddr = Input(Vec(base.FETCH_WIDTH * 3, UInt(base.AREG_WIDTH.W)))
        val rat_rdata = Output(Vec(base.FETCH_WIDTH * 3, UInt(base.PREG_WIDTH.W)))

        /* retire stage */
        val rat_flush_en = Input(Bool())
        val rat_flush_data = Input(Vec(1 << base.AREG_WIDTH, UInt(base.PREG_WIDTH.W)))  
    })

    var rat_mapping = RegInit(VecInit(
        Seq.tabulate(1 << base.AREG_WIDTH)((i) => (i.U)(base.PREG_WIDTH.W))
    ))

    var rat_rdata = WireInit(
        VecInit(
            Seq.fill(base.FETCH_WIDTH * 3)((0.U)(base.PREG_WIDTH.W))
        )
    )

    for(i <- 0 until (1 << base.AREG_WIDTH))
    {
        for(j <- 0 until base.FETCH_WIDTH){
            when(io.rat_wen(j) & (io.rat_waddr(j) === i.U) & ~io.rat_flush_en){
                rat_mapping(i) := io.rat_wdata(j)
            }
        }
        when(io.rat_flush_en){
            rat_mapping(i) := io.rat_flush_data(i)
        }
    }

    for(j <- 0 until base.FETCH_WIDTH * 3)
    {
        when(io.rat_ren(j)){
            rat_rdata(j) := rat_mapping(io.rat_raddr(j))
        }
    }
    /* connect */
    io.rat_rdata := rat_rdata
}