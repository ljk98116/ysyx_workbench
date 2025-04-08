package cpu.frontend

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

        val rat_ren = Input(UInt((base.FETCH_WIDTH * 2).W))
        val rat_raddr = Input(Vec(base.FETCH_WIDTH * 2, UInt(base.AREG_WIDTH.W)))
        val rat_rdata = Output(Vec(base.FETCH_WIDTH * 2, UInt(base.AREG_WIDTH.W)))
    })

    var rat_mapping = RegInit(VecInit(
        Seq.fill(1 << base.PREG_WIDTH)((0.U)(base.PREG_WIDTH.W))
    ))

    var rat_rdata = WireInit(
        VecInit(
            Seq.fill(base.FETCH_WIDTH * 2)((0.U)(base.PREG_WIDTH.W))
        )
    )

    for(i <- 0 until (1 << base.PREG_WIDTH))
    {
        for(j <- 0 until base.FETCH_WIDTH){
            when(io.rat_wen(j) & io.rat_waddr(j) === i.U){
                rat_mapping(i) := io.rat_wdata(j)
            }
        }
        for(j <- 0 until base.FETCH_WIDTH * 2)
        {
            when(io.rat_ren(j) & io.rat_raddr(j) === i.U){
                rat_rdata(j) := rat_mapping(i)
            }
        }
    }
    /* connect */
    io.rat_rdata := rat_rdata
}