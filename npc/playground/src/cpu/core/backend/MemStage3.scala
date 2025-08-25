package cpu.core.backend

import cpu.config._
import chisel3._
import chisel3.util._

/* 处理sram/dcache/store buffer访问结果 */
class MemStage3 extends Module{
    val width = log2Ceil(base.STORE_BUF_SZ)
    val io = IO(new Bundle {
        val rat_flush_en = Input(Bool())
        val rob_state = Input(UInt(2.W))
        val rob_item_i = Input(Vec(base.AGU_NUM, new ROBItem))
        val rob_item_o = Output(Vec(base.AGU_NUM, new ROBItem))
        val mem_read_en_i = Input(Vec(base.AGU_NUM, Bool()))
        val mem_read_mask_i = Input(Vec(base.AGU_NUM, UInt(8.W)))
        val storebuffer_rdata = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val storebuffer_rdata_valid = Input(Vec(base.AGU_NUM, Bool()))
        val mem_read_data_i = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val mem_read_data_o = Output(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
    })

    /* pipeline */
    var rob_item_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U).asTypeOf(new ROBItem))
    ))
    var mem_read_en_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))

    var mem_read_mask_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(8.W))
    ))

    var store_buffer_rdata_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W))
    ))
    var store_buffer_rdata_valid_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))

    rob_item_reg := Mux(
        ~io.rat_flush_en, 
        Mux((io.rob_state === 0.U), io.rob_item_i, rob_item_reg),
        VecInit(Seq.fill(base.AGU_NUM)((0.U).asTypeOf(new ROBItem)))
    )
    mem_read_en_reg := Mux(
        ~io.rat_flush_en, 
        Mux((io.rob_state === 0.U), io.mem_read_en_i, mem_read_en_reg),
        VecInit(Seq.fill(base.AGU_NUM)(false.B))
    )
    mem_read_mask_reg := Mux(
        ~io.rat_flush_en, 
        Mux((io.rob_state === 0.U), io.mem_read_mask_i, mem_read_mask_reg),
        VecInit(Seq.fill(base.AGU_NUM)((0.U)(8.W)))
    )
    store_buffer_rdata_reg := Mux(
        ~io.rat_flush_en,
        Mux((io.rob_state === 0.U), io.storebuffer_rdata, store_buffer_rdata_reg),
        VecInit(Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W)))
    )
    store_buffer_rdata_valid_reg := Mux(
        ~io.rat_flush_en,
        Mux((io.rob_state === 0.U), io.storebuffer_rdata_valid, store_buffer_rdata_valid_reg),
        VecInit(Seq.fill(base.AGU_NUM)(false.B))        
    )
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
            mem_read_data_mid(i) := Mux(~store_buffer_rdata_valid_reg(i), io.mem_read_data_i(i), store_buffer_rdata_reg(i))
            /* load处理 */
            switch(rob_item_reg(i).funct3){
                is(Funct3.LB){
                    mem_read_data_o(i) := Cat(Fill(24, mem_read_data_mid(i)(7)), mem_read_data_mid(i)(7, 0))
                }
                is(Funct3.LBU){
                    mem_read_data_o(i) := Cat(Fill(24, false.B), mem_read_data_mid(i)(7, 0))
                }
                is(Funct3.LH){
                    mem_read_data_o(i) := Cat(Fill(16, mem_read_data_mid(i)(15)), mem_read_data_mid(i)(15, 0))
                }
                is(Funct3.LHU){
                    mem_read_data_o(i) := Cat(Fill(16, false.B), mem_read_data_mid(i)(15, 0))
                }
                is(Funct3.LW){
                    mem_read_data_o(i) := mem_read_data_mid(i)
                }                
            }
        }
    }

    /* connect */
    io.rob_item_o := rob_item_o
    io.mem_read_data_o := mem_read_data_o
}