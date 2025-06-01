package cpu.core.backend

import cpu.config._
import chisel3._
import chisel3.util._
import cpu.core.utils.PriorityDecoder

/* 处理load/store RAW依赖，输出storebuffer和dcache/datasram使能 */
/* 缓存使能到下一周期访问存储器或者storebuffer */
/* 这里访问存储器只涉及load指令 */
/* 需要注意组内RAW相关性 */
class MemStage1 extends Module
{
    val width = log2Ceil(base.STORE_BUF_SZ)
    val io = IO(new Bundle{
        val rat_flush_en = Input(Bool())
        val rob_state = Input(Bool())
        val rob_item_i = Input(Vec(base.AGU_NUM, new ROBItem))
        val agu_result_i = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val agu_rw_mask_i = Input(Vec(base.AGU_NUM, UInt(8.W)))
        val agu_mem_wdata = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val ls_flag = Input(Vec(base.AGU_NUM, Bool()))
        val rob_item_o = Output(Vec(base.AGU_NUM, new ROBItem))
        /* storebuffer Input */
        /* 从head指针开始的地址 */
        val storebuffer_addr_i = Input(Vec(base.STORE_BUF_SZ, UInt(base.ADDR_WIDTH.W)))
        
        /* storebuffer read req */
        val storebuffer_ren_o = Output(Vec(base.AGU_NUM, Bool()))
        val storebuffer_raddr_o = Output(Vec(base.AGU_NUM, UInt(width.W)))
        val storebuffer_rmask_o = Output(Vec(base.AGU_NUM, UInt(8.W)))

        /* storebuffer Item */
        val storebuffer_head_item_i = Input(new StoreBufferItem)

        /* dcache/datasram req */
        val mem_read_en_o = Output(Vec(base.AGU_NUM, Bool()))
        val mem_read_addr_o = Output(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val mem_read_mask_o = Output(Vec(base.AGU_NUM, UInt(8.W)))

        val mem_write_en_o = Output(Bool())
        val mem_write_addr_o = Output(UInt(base.ADDR_WIDTH.W))
        val mem_write_wmask_o = Output(UInt(8.W))
        val mem_write_data_o = Output(UInt(base.DATA_WIDTH.W))
    })

    /* pipeline */
    var rob_item_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U).asTypeOf(new ROBItem))
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

    var storebuffer_item_reg = RegInit((0.U).asTypeOf(new StoreBufferItem))
    rob_item_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.rob_item_i, rob_item_reg), 
        VecInit(Seq.fill(base.AGU_NUM)((0.U).asTypeOf(new ROBItem)))
    )
    agu_result_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.agu_result_i, agu_result_reg), 
        VecInit(Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W)))
    )
    agu_rw_mask_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.agu_rw_mask_i, agu_rw_mask_reg), 
        VecInit(Seq.fill(base.AGU_NUM)((0.U)(8.W)))
    )
    agu_mem_wdata_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.agu_mem_wdata, agu_mem_wdata_reg),
        VecInit(Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W)))
    )

    storebuffer_item_reg := Mux(
        ~io.rat_flush_en,
        Mux(~io.rob_state, io.storebuffer_head_item_i, storebuffer_item_reg),
        0.U.asTypeOf(new StoreBufferItem)
    )

    var storebuffer_ren_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))

    var storebuffer_raddr_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(width.W))
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

    var mem_write_en_o = WireInit(false.B)

    var mem_write_addr_o = WireInit((0.U)(base.ADDR_WIDTH.W))

    var mem_write_wmask_o = WireInit((0.U)(8.W))

    var mem_write_data_o = WireInit((0.U)(base.DATA_WIDTH.W))

    /* 找到匹配的最新的 */
    var priority_decoder_vec = Seq.fill(base.AGU_NUM)(
        Module(new PriorityDecoder(base.STORE_BUF_SZ))
    )

    for(i <- 0 until base.AGU_NUM){
        var raw_mask = WireInit(VecInit(
            Seq.fill(base.STORE_BUF_SZ)(false.B)
        ))
        for(j <- 0 until base.STORE_BUF_SZ){
            raw_mask(i) := agu_result_reg(i) === io.storebuffer_addr_i(j)
        }
        storebuffer_ren_o(i) := io.ls_flag(i) & raw_mask.asUInt.orR
        priority_decoder_vec(i).io.in := raw_mask.asUInt
        storebuffer_raddr_o(i) := priority_decoder_vec(i).io.out
        storebuffer_rmask_o(i) := agu_rw_mask_reg(i)
        mem_read_en_o(i) := ~storebuffer_ren_o(i) & rob_item_reg(i).valid & rob_item_reg(i).isLoad
        mem_read_addr_o(i) := agu_result_reg(i)
        mem_read_mask_o(i) := agu_rw_mask_reg(i)
    }

    mem_write_en_o := storebuffer_item_reg.valid & storebuffer_item_reg.rdy & storebuffer_item_reg.rob_rdy
    mem_write_addr_o := storebuffer_item_reg.agu_result
    mem_write_data_o := storebuffer_item_reg.wdata
    mem_write_wmask_o := storebuffer_item_reg.wmask

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