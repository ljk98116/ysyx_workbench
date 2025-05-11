package cpu.core.backend

import cpu.config._
import chisel3._
import chisel3.util._

/* 处理sram/dcache/store buffer访问结果 */
class MemStage3 extends Module{
    val width = log2Ceil(base.STORE_BUF_SZ)
    val io = IO(new Bundle {
        val rob_item_i = Input(Vec(base.AGU_NUM, new ROBItem))
        val rob_item_o = Output(Vec(base.AGU_NUM, new ROBItem))
        val mem_read_en_i = Input(Vec(base.AGU_NUM, Bool()))
        val storebuffer_rdata = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val mem_read_data_i = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val mem_read_data_o = Output(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
    })

    /* pipeline */
    var rob_item_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U).asTypeOf(new ROBItem))
    ))

    rob_item_reg := io.rob_item_i

    var rob_item_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U).asTypeOf(new ROBItem))
    ))
    var mem_read_data_mid = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W))
    ))
    var mem_read_data_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W))
    ))

    rob_item_o := rob_item_reg

    for(i <- 0 until base.AGU_NUM){
        when(rob_item_reg(i).valid){
            mem_read_data_mid(i) := Mux(io.mem_read_en_i(i), io.mem_read_data_i(i), io.storebuffer_rdata(i))
            /* load处理 */
            
        }
    }

    /* connect */
    io.rob_item_o := rob_item_o
    io.mem_read_data_o := mem_read_data_o
}