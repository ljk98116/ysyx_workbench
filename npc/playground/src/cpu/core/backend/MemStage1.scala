package cpu.core.backend

import cpu.config._
import chisel3._
import chisel3.util._
import cpu.core.utils.PriorityDecoder

/* 处理load/store RAW依赖，输出storebuffer和dcache/datasram使能 */
/* 缓存使能到下一周期访问存储器或者storebuffer */
/* 这里访问存储器只涉及load指令 */
/* 需要注意组内RAW相关性 */
/* 后期需要在这里进行TLB转化 */
/* 处理头部的2条store指令，看是否前推 */
class MemStage1 extends Module
{
    val width = log2Ceil(base.STORE_BUF_SZ)
    val io = IO(new Bundle{
        val rat_flush_en = Input(Bool())
        val rob_state = Input(UInt(2.W))
        val agu_valid_i = Input(Vec(base.AGU_NUM, Bool()))
        val rob_item_i = Input(Vec(base.AGU_NUM, new ROBItem))
        val agu_result_i = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val agu_rw_mask_i = Input(Vec(base.AGU_NUM, UInt(8.W)))
        val agu_mem_wdata = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val rob_item_o = Output(Vec(base.AGU_NUM, new ROBItem))
        
        /* storebuffer read req */
        val storebuffer_ren_o = Output(Vec(base.AGU_NUM, Bool()))
        val storebuffer_raddr_o = Output(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val storebuffer_rmask_o = Output(Vec(base.AGU_NUM, UInt(8.W)))

        /* storebuffer Item 头部的store指令 */
        val storebuffer_head_item_i = Input(Vec(base.AGU_NUM, new StoreBufferItem))

        /* dcache/datasram req */
        val mem_read_en_o = Output(Vec(base.AGU_NUM, Bool()))
        val mem_read_addr_o = Output(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val mem_read_mask_o = Output(Vec(base.AGU_NUM, UInt(8.W)))

        val mem_write_en_o = Output(Vec(base.AGU_NUM, Bool()))
        val mem_write_addr_o = Output(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val mem_write_wmask_o = Output(Vec(base.AGU_NUM, UInt(8.W)))
        val mem_write_data_o = Output(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
    })

    /* pipeline */
    var rob_item_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U).asTypeOf(new ROBItem))
    ))
    var agu_valid_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))
    var agu_result_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.ADDR_WIDTH.W))
    ))
    var agu_rw_mask_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(8.W))
    ))
    var agu_mem_wdata_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W))
    ))

    var storebuffer_item_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U).asTypeOf(new StoreBufferItem))
    ))

    rob_item_reg := Mux(
        ~io.rat_flush_en, 
        Mux((io.rob_state === 0.U), io.rob_item_i, rob_item_reg), 
        VecInit(Seq.fill(base.AGU_NUM)((0.U).asTypeOf(new ROBItem)))
    )
    agu_valid_reg := Mux(
        ~io.rat_flush_en, 
        Mux((io.rob_state === 0.U), io.agu_valid_i, agu_valid_reg), 
        VecInit(Seq.fill(base.AGU_NUM)(false.B))
    )
    agu_result_reg := Mux(
        ~io.rat_flush_en, 
        Mux((io.rob_state === 0.U), io.agu_result_i, agu_result_reg), 
        VecInit(Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W)))
    )
    agu_rw_mask_reg := Mux(
        ~io.rat_flush_en, 
        Mux((io.rob_state === 0.U), io.agu_rw_mask_i, agu_rw_mask_reg), 
        VecInit(Seq.fill(base.AGU_NUM)((0.U)(8.W)))
    )
    agu_mem_wdata_reg := Mux(
        ~io.rat_flush_en, 
        Mux((io.rob_state === 0.U), io.agu_mem_wdata, agu_mem_wdata_reg),
        VecInit(Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W)))
    )

    storebuffer_item_reg := io.storebuffer_head_item_i

    var storebuffer_ren_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))

    var storebuffer_raddr_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.ADDR_WIDTH.W))
    ))

    var storebuffer_rmask_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(8.W))
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
    /* load/store使能 */
    /* 将来这里给到TLB,直连TLB判断TLB是否命中 */
    for(i <- 0 until base.AGU_NUM){
        mem_read_en_o(i) := rob_item_reg(i).valid & rob_item_reg(i).isLoad & agu_valid_reg(i)
        mem_read_addr_o(i) := Mux(rob_item_reg(i).valid & rob_item_reg(i).isLoad, agu_result_reg(i), 0.U)
        mem_read_mask_o(i) := Mux(rob_item_reg(i).valid & rob_item_reg(i).isLoad, agu_rw_mask_reg(i), 0.U)

        mem_write_en_o(i) := storebuffer_item_reg(i).rdy & storebuffer_item_reg(i).rob_rdy
        mem_write_addr_o(i) := Mux(
            storebuffer_item_reg(i).rdy & storebuffer_item_reg(i).rob_rdy, 
            storebuffer_item_reg(i).agu_result,
            0.U
        )
        mem_write_data_o(i) := Mux(
            storebuffer_item_reg(i).rdy & storebuffer_item_reg(i).rob_rdy,
            storebuffer_item_reg(i).wdata,
            0.U
        )
        mem_write_wmask_o(i) := Mux(
            storebuffer_item_reg(i).rdy & storebuffer_item_reg(i).rob_rdy,
            storebuffer_item_reg(i).wmask,
            0.U            
        )
        storebuffer_ren_o(i) := rob_item_reg(i).valid & rob_item_reg(i).isLoad
        storebuffer_raddr_o(i) := Mux(rob_item_reg(i).valid & rob_item_reg(i).isLoad, agu_result_reg(i), 0.U)
        storebuffer_rmask_o(i) := Mux(rob_item_reg(i).valid & rob_item_reg(i).isLoad, agu_rw_mask_reg(i), 0.U)
    }

    /* connect */
    io.storebuffer_ren_o := storebuffer_ren_o
    io.storebuffer_raddr_o := storebuffer_raddr_o
    io.storebuffer_rmask_o := storebuffer_rmask_o
    io.mem_read_en_o := mem_read_en_o
    io.mem_read_addr_o := mem_read_addr_o
    io.mem_read_mask_o := mem_read_mask_o
    io.rob_item_o := rob_item_reg
    io.mem_write_en_o := mem_write_en_o
    io.mem_write_addr_o := mem_write_addr_o
    io.mem_write_wmask_o := mem_write_wmask_o
    io.mem_write_data_o := mem_write_data_o
}