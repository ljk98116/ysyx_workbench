package cpu.core.frontend

import chisel3._
import chisel3.util._

import cpu.config._

/* 异常发生，且该指令前面有异常，使用retireRAT，否则使用指令的rd */
/* 无异常，使用rename段优先 */
class RenameRAT extends Module
{
    val io = IO(new Bundle{
        /* RAT读写使能 */
        val exception_mask_front = Input(UInt(base.FETCH_WIDTH.W))
        val retire_rat_wen = Input(UInt(base.FETCH_WIDTH.W))
        val retire_rat_waddr = Input(Vec(base.FETCH_WIDTH, UInt(base.AREG_WIDTH.W)))
        val retire_rat_wdata = Input(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))
        
        val rat_wen = Input(UInt(base.FETCH_WIDTH.W))
        val rat_waddr = Input(Vec(base.FETCH_WIDTH, UInt(base.AREG_WIDTH.W)))
        val rat_wdata = Input(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))
        /* 读取rs1, rs2以及以前的rd */
        val rat_ren = Input(UInt((base.FETCH_WIDTH * 3).W))
        val rat_raddr = Input(Vec(base.FETCH_WIDTH * 3, UInt(base.AREG_WIDTH.W)))
        val rat_rdata = Output(Vec(base.FETCH_WIDTH * 3, UInt((base.PREG_WIDTH + 1).W)))

        /* retire stage */
        val rat_flush_en = Input(Bool())
        val rat_flush_data = Input(Vec(1 << base.AREG_WIDTH, UInt((base.PREG_WIDTH + 1).W)))  
    })

    var rat_mapping = RegInit(VecInit(
        Seq.tabulate(1 << base.AREG_WIDTH)((i) => ((1 << base.PREG_WIDTH).U)((base.PREG_WIDTH + 1).W))
    ))

    var rat_rdata = WireInit(
        VecInit(
            Seq.fill(base.FETCH_WIDTH * 3)((0.U)((base.PREG_WIDTH + 1).W))
        )
    )

    for(i <- 0 until (1 << base.AREG_WIDTH))
    {
        when(io.rat_flush_en){
            when(io.retire_rat_wen(3) & (i.U === io.retire_rat_waddr(3))){
                rat_mapping(i) := Mux(io.exception_mask_front(3), io.rat_flush_data(i), io.retire_rat_wdata(3))
            }.elsewhen(io.retire_rat_wen(2) & (i.U === io.retire_rat_waddr(2))){
                rat_mapping(i) := Mux(io.exception_mask_front(2), io.rat_flush_data(i), io.retire_rat_wdata(2))
            }.elsewhen(io.retire_rat_wen(1) & (i.U === io.retire_rat_waddr(1))){
                rat_mapping(i) := Mux(io.exception_mask_front(1), io.rat_flush_data(i), io.retire_rat_wdata(1))
            }.elsewhen(io.retire_rat_wen(0) & (i.U === io.retire_rat_waddr(0))){
                rat_mapping(i) := io.retire_rat_wdata(0)
            }.otherwise{
                rat_mapping(i) := io.rat_flush_data(i)
            }
        }.otherwise{
            when(io.rat_wen(3) & (i.U === io.rat_waddr(3))){
                rat_mapping(i) := io.rat_wdata(3)
            }.elsewhen(io.rat_wen(2) & (i.U === io.rat_waddr(2))){
                rat_mapping(i) := io.rat_wdata(2)
            }.elsewhen(io.rat_wen(1) & (i.U === io.rat_waddr(1))){
                rat_mapping(i) := io.rat_wdata(1)
            }.elsewhen(io.rat_wen(0) & (i.U === io.rat_waddr(0))){
                rat_mapping(i) := io.rat_wdata(0)
            }
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