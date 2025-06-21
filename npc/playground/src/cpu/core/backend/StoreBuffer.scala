package cpu.core.backend

import cpu.config._
import chisel3._
import chisel3.util._
import cpu.core.utils._

/* store指令存储阵列*/
/* 写ROB时同时写入，根据总线消息维护对应信息 */
/* 输出所有项的store指令对应的地址 */
/* store buffer一次发射2条store指令 */
class StoreBuffer(size : Int) extends Module{
    var width = log2Ceil(size)
    val io = IO(new Bundle{
        val rat_flush_en = Input(Bool())
        /* Dispatch */
        val store_buffer_write_en = Input(Vec(base.FETCH_WIDTH, Bool()))
        val store_buffer_item_i = Input(Vec(base.FETCH_WIDTH, new StoreBufferItem))
        /* AGU更新store buffer */
        val cdb_i = Input(new CDB)
        /* MemStage1 load forwarding */
        val store_buffer_ren = Input(Vec(base.AGU_NUM, Bool()))
        val store_buffer_raddr = Input(Vec(base.AGU_NUM, UInt(width.W)))
        val store_buffer_rmask = Input(Vec(base.AGU_NUM, UInt(8.W)))
        /* MemStage2输出 */
        val store_buffer_rdata = Output(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        /* retire段，ROB头部项 */
        val rob_items_i = Input(Vec(base.FETCH_WIDTH, new ROBItem))
        /* 输出队列头部2个Item */
        val store_buffer_item_o = Output(new StoreBufferItem)
        /* StoreBuffer是否可写入 */
        val wr_able = Bool()
    })

    var storebuffer_item_reg = RegInit(VecInit(
        Seq.fill(size)((0.U).asTypeOf(new StoreBufferItem))
    ))

    var head = RegInit((0.U)(width.W))
    var tail = RegInit((0.U)(width.W))
    /* 是否可写入，必须保留至少fetch_width + 1 的空间 */
    var wr_able_mask = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    for(i <- 0 until base.FETCH_WIDTH){
        wr_able_mask(i) := tail + (i + 1).U =/= head
    }
    io.wr_able := wr_able_mask.asUInt.andR
    /* 组合逻辑，判断load RAW相关性 */
    /* head < tail, 找最晚的匹配 */
    /* head > tail, 优先看tail，同样找最晚的匹配 */
    var prio_decoder_vec = Seq.fill(base.AGU_NUM)(
        Module(new PriorityDecoder(size))
    )
    var raw_stIdx = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((size.U)((width + 1).W))
    ))
    var store_buffer_rdata = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W))
    ))
    var store_buffer_item_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U).asTypeOf(new StoreBufferItem))
    ))

    for(i <- 0 until base.AGU_NUM){
        /* head > tail时候的tail以下部分 */
        var load_raw_mask_1 = WireInit(VecInit(
            Seq.fill(size)(false.B)
        ))
        /* head > tail时候的head以上部分 */
        var load_raw_mask_2 = WireInit(VecInit(
            Seq.fill(size)(false.B)
        ))
        var load_raw_mask = WireInit((0.U)(size.W))
        for(j <- 0 until size){
            load_raw_mask_1(j) := 
                (io.store_buffer_ren(i) & storebuffer_item_reg(j).rdy) & 
                (storebuffer_item_reg(j).agu_result === io.store_buffer_raddr(i)) &
                (storebuffer_item_reg(j).wmask === io.store_buffer_rmask(i)) & 
                ((j.U < tail & head > tail) | (head < tail))
            load_raw_mask_2(j) := 
                (io.store_buffer_ren(i) & storebuffer_item_reg(j).rdy) & 
                (storebuffer_item_reg(j).agu_result === io.store_buffer_raddr(i)) &
                (storebuffer_item_reg(j).wmask === io.store_buffer_rmask(i)) & 
                ((j.U >= head & head > tail) | (head < tail))            
        }
        load_raw_mask := Mux(load_raw_mask_1.asUInt.orR, load_raw_mask_1.asUInt, load_raw_mask_2.asUInt)
        prio_decoder_vec(i).io.in := load_raw_mask
        /* 寄存器缓存最晚的命中的store指令的数据，下一周期输出 */
        raw_stIdx(i) := Mux(load_raw_mask.orR, size.U, prio_decoder_vec(i).io.out)
        store_buffer_rdata(i) := storebuffer_item_reg(raw_stIdx(i)).wdata
    }
    store_buffer_item_o(0) := Mux(head =/= tail, storebuffer_item_reg(head), 0.U.asTypeOf(new StoreBufferItem))
    store_buffer_item_o(1) := Mux(head + 1.U =/= tail, storebuffer_item_reg(head + 1.U), 0.U.asTypeOf(new StoreBufferItem))
    
    /* 时序逻辑 */
    
    /* connect */
    io.store_buffer_rdata := store_buffer_rdata
    io.store_buffer_item_o := store_buffer_item_o
}

