package cpu.core.backend

import cpu.config._
import chisel3._
import chisel3.util._

/* 访问storebuffer数据/sram/dcache数据 */
class MemStage2 extends Module{
    val width = log2Ceil(base.STORE_BUF_SZ)
    val io = IO(new Bundle {
        val rat_flush_en = Input(Bool())
        val rob_state = Input(Bool())
        val rob_item_i = Input(Vec(base.AGU_NUM, new ROBItem))
        val mem_read_en_i = Input(Vec(base.AGU_NUM, Bool()))
        val mem_read_addr_i = Input(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val mem_read_mask_i = Input(Vec(base.AGU_NUM, UInt(8.W)))
        val mem_write_en_i = Input(Vec(base.AGU_NUM, Bool()))
        val mem_write_addr_i = Input(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val mem_write_mask_i = Input(Vec(base.AGU_NUM, UInt(8.W)))
        val mem_write_data_i = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        /* store buffer req */
        val storebuffer_ren_i = Input(Vec(base.AGU_NUM, Bool()))
        val storebuffer_raddr_i = Input(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val storebuffer_rmask_i = Input(Vec(base.AGU_NUM, UInt(8.W))) 

        val mem_read_en_o = Output(Vec(base.AGU_NUM, Bool()))
        val mem_read_addr_o = Output(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val mem_read_mask_o = Output(Vec(base.AGU_NUM, UInt(8.W)))

        val mem_write_en_o = Output(Vec(base.AGU_NUM, Bool()))
        val mem_write_addr_o = Output(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val mem_write_wmask_o = Output(Vec(base.AGU_NUM, UInt(8.W)))
        val mem_write_data_o = Output(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))

        val storebuffer_ren_o = Output(Vec(base.AGU_NUM, Bool()))
        val storebuffer_raddr_o = Output(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val storebuffer_rmask_o = Output(Vec(base.AGU_NUM, UInt(8.W))) 
        val store_ids_o = Output(Vec(base.AGU_NUM, UInt((base.ROBID_WIDTH + 1).W)))
        val rob_item_o = Output(Vec(base.AGU_NUM, new ROBItem))
    })
    /* pipeline */
    var mem_read_en_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))
    var mem_read_addr_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.ADDR_WIDTH.W))
    ))
    var mem_read_mask_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(8.W))
    ))
    var mem_write_en_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))
    var mem_write_addr_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.ADDR_WIDTH.W))
    ))
    var mem_write_mask_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(8.W))
    ))
    var mem_write_data_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W))
    ))
    var storebuffer_ren_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))
    var storebuffer_raddr_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.ADDR_WIDTH.W))
    ))
    var storebuffer_rmask_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(8.W))
    ))

    var rob_item_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U).asTypeOf(new ROBItem))
    ))

    mem_read_en_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.mem_read_en_i, mem_read_en_reg),
        VecInit(Seq.fill(base.AGU_NUM)(false.B))
    )
    mem_read_addr_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.mem_read_addr_i, mem_read_addr_reg), 
        VecInit(Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W)))
    )
    mem_read_mask_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.mem_read_mask_i, mem_read_mask_reg), 
        VecInit(Seq.fill(base.AGU_NUM)((0.U)(8.W)))
    )
    mem_write_en_reg := io.mem_write_en_i
    mem_write_addr_reg := io.mem_write_addr_i
    mem_write_mask_reg := io.mem_write_mask_i
    mem_write_data_reg := io.mem_write_data_i

    storebuffer_ren_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.storebuffer_ren_i, storebuffer_ren_reg),
        VecInit(Seq.fill(base.AGU_NUM)(false.B))        
    )
    storebuffer_raddr_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.storebuffer_raddr_i, storebuffer_raddr_reg), 
        VecInit(Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W)))
    )
    storebuffer_rmask_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.storebuffer_rmask_i, storebuffer_rmask_reg), 
        VecInit(Seq.fill(base.AGU_NUM)((0.U)(8.W)))
    )    
    rob_item_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.rob_item_i, rob_item_reg),
        VecInit(Seq.fill(base.AGU_NUM)((0.U).asTypeOf(new ROBItem)))
    )

    var mem_write_en_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))
    var mem_write_addr_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.ADDR_WIDTH.W))
    ))
    var mem_write_wmask_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(8.W))
    ))
    var mem_write_data_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W))
    ))

    var mem_read_en_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))
    var mem_read_addr_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.ADDR_WIDTH.W))
    ))   
    var mem_read_mask_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(8.W))
    )) 
    var storebuffer_ren_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))
    var storebuffer_raddr_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.ADDR_WIDTH.W))
    ))
    var storebuffer_rmask_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(8.W))
    ))
    var rob_item_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U).asTypeOf(new ROBItem))
    ))

    var store_ids_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)(((1 << base.ROBID_WIDTH).U)((base.ROBID_WIDTH + 1).W))
    ))

    mem_read_en_o := mem_read_en_reg
    mem_read_addr_o := mem_read_addr_reg
    mem_read_mask_o := mem_read_mask_reg
    mem_write_en_o := mem_write_en_reg
    mem_write_addr_o := mem_write_addr_reg
    mem_write_wmask_o := mem_write_mask_reg
    mem_write_data_o := mem_write_data_reg
    storebuffer_ren_o := storebuffer_ren_reg
    storebuffer_raddr_o := storebuffer_raddr_reg
    storebuffer_rmask_o := storebuffer_rmask_reg
    rob_item_o := rob_item_reg
    for(i <- 0 until base.AGU_NUM){
        store_ids_o(i) := rob_item_reg(i).storeIdx
    }

    /* connect */
    io.mem_read_en_o := mem_read_en_o
    io.mem_read_addr_o := mem_read_addr_o
    io.mem_read_mask_o := mem_read_mask_o
    io.mem_write_en_o := mem_write_en_o
    io.mem_write_addr_o := mem_write_addr_o
    io.mem_write_wmask_o := mem_write_wmask_o
    io.mem_write_data_o := mem_write_data_o
    io.storebuffer_ren_o := storebuffer_ren_o
    io.storebuffer_raddr_o := storebuffer_raddr_o
    io.storebuffer_rmask_o := storebuffer_rmask_o
    io.store_ids_o := store_ids_o
    io.rob_item_o := rob_item_o
}