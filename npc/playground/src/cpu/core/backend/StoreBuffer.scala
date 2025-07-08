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
        val rob_state = Input(Bool())
        /* Dispatch */
        val store_buffer_write_en = Input(Vec(base.FETCH_WIDTH, Bool()))
        val store_buffer_item_i = Input(Vec(base.FETCH_WIDTH, new StoreBufferItem))
        val store_buffer_write_cnt = Input(UInt((log2Ceil(base.FETCH_WIDTH) + 1).W))
        /* AGU更新store buffer */
        val agu_valid = Input(Vec(base.AGU_NUM, Bool()))
        val agu_rob_id = Input(Vec(base.AGU_NUM, UInt(base.ROBID_WIDTH.W)))
        val agu_result = Input(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val agu_wdata = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val agu_wmask = Input(Vec(base.AGU_NUM, UInt(8.W)))
        /* MemStage2 load forwarding & mem_write_en */
        val store_buffer_ren = Input(Vec(base.AGU_NUM, Bool()))
        val store_buffer_raddr = Input(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val store_ids = Input(Vec(base.AGU_NUM, UInt((base.ROBID_WIDTH + 1).W)))
        val store_buffer_rmask = Input(Vec(base.AGU_NUM, UInt(8.W)))
        val mem_write_en = Input(Vec(base.AGU_NUM, Bool()))
        /* MemStage3输出 */
        val store_buffer_rdata = Output(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val store_buffer_rdata_valid = Output(Vec(base.AGU_NUM, Bool()))
        /* retire段，ROB头部项 */
        val rob_items_i = Input(Vec(base.FETCH_WIDTH, new ROBItem))
        /* 输出队列头部2个Item */
        val store_buffer_item_o = Output(Vec(base.AGU_NUM, new StoreBufferItem))
        /* StoreBuffer是否可写入 */
        val wr_able = Output(Bool())
    })

    var storebuffer_item_reg = RegInit(VecInit(
        Seq.fill(size)((0.U).asTypeOf(new StoreBufferItem))
    ))

    var store_buffer_mapping = RegInit(VecInit(
        Seq.fill(1 << base.ROBID_WIDTH)((size.U)((width + 1).W))
    ))
    var head = RegInit((0.U)(width.W))
    var tail = RegInit((0.U)(width.W))
    var rob_head = RegInit((0.U)(width.W))

    /* 是否可写入，必须保留至少fetch_width + 1 的空间 */
    var wr_able_mask = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    for(i <- 0 until base.FETCH_WIDTH){
        wr_able_mask(i) := (tail + (i + 1).U) =/= head
    }
    io.wr_able := wr_able_mask.asUInt.andR
    /* 组合逻辑，判断load RAW相关性 */
    /* head < tail, 找最晚的匹配 */
    /* head > tail, 优先看tail，同样找最晚的匹配 */
    /* storeid对应的store指令的位置是load forwarding查找范围的上限 */
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
    var store_buffer_rvalid = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
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
                (((j.U < tail) & (head > tail)) | (head < tail)) &
                ((store_buffer_mapping(io.store_ids(i)(base.ROBID_WIDTH - 1, 0)) >= j.U) & (store_buffer_mapping(io.store_ids(i)(base.ROBID_WIDTH - 1, 0)) < tail))
            load_raw_mask_2(j) := 
                (io.store_buffer_ren(i) & storebuffer_item_reg(j).rdy) & 
                (storebuffer_item_reg(j).agu_result === io.store_buffer_raddr(i)) &
                (storebuffer_item_reg(j).wmask === io.store_buffer_rmask(i)) & 
                (((j.U >= head) & (head > tail)) | (head < tail)) & 
                (store_buffer_mapping(io.store_ids(i)(base.ROBID_WIDTH - 1, 0)) >= j.U)
        }
        load_raw_mask := Mux(load_raw_mask_1.asUInt =/= 0.U, load_raw_mask_1.asUInt, load_raw_mask_2.asUInt)
        prio_decoder_vec(i).io.in := load_raw_mask
        /* 寄存器缓存最晚的命中的store指令的数据，下一周期输出 */
        raw_stIdx(i) := Mux(load_raw_mask.asUInt =/= 0.U, prio_decoder_vec(i).io.out, size.U)
        store_buffer_rdata(i) := storebuffer_item_reg(raw_stIdx(i)(width - 1, 0)).wdata
        store_buffer_rvalid(i) := raw_stIdx(i) =/= size.U
    }
    store_buffer_item_o(0) := Mux(rob_head =/= tail, storebuffer_item_reg(rob_head), 0.U.asTypeOf(new StoreBufferItem))
    store_buffer_item_o(1) := Mux((rob_head + 1.U) =/= tail, storebuffer_item_reg(rob_head + 1.U), 0.U.asTypeOf(new StoreBufferItem))
    
    /* 时序逻辑 */
    for(i <- 0 until size){
        when(io.rob_state){
            storebuffer_item_reg(i) := 0.U.asTypeOf(new StoreBufferItem)
            store_buffer_mapping(storebuffer_item_reg(i).rob_id) := size.U
        }.elsewhen((i.U === tail) & io.store_buffer_item_i(0).valid){
            storebuffer_item_reg(i) := io.store_buffer_item_i(0)
            store_buffer_mapping(io.store_buffer_item_i(0).rob_id) := i.U
        }.elsewhen((i.U === (tail + 1.U)) & io.store_buffer_item_i(1).valid){
            storebuffer_item_reg(i) := io.store_buffer_item_i(1)
            store_buffer_mapping(io.store_buffer_item_i(1).rob_id) := i.U
        }.elsewhen((i.U === (tail + 2.U)) & io.store_buffer_item_i(2).valid){
            storebuffer_item_reg(i) := io.store_buffer_item_i(2)
            store_buffer_mapping(io.store_buffer_item_i(2).rob_id) := i.U
        }.elsewhen((i.U === (tail + 3.U)) & io.store_buffer_item_i(3).valid){
            storebuffer_item_reg(i) := io.store_buffer_item_i(3)
            store_buffer_mapping(io.store_buffer_item_i(3).rob_id) := i.U
        }.otherwise{
            for(j <- 0 until base.AGU_NUM){
                when(
                    storebuffer_item_reg(i).valid & io.agu_valid(j) &
                    (storebuffer_item_reg(i).rob_id === io.agu_rob_id(j))
                ){
                    storebuffer_item_reg(i).rdy := true.B
                    storebuffer_item_reg(i).agu_result := io.agu_result(j)
                    storebuffer_item_reg(i).wdata := io.agu_wdata(j)
                    storebuffer_item_reg(i).wmask := io.agu_wmask(j)
                }
            }
            for(j <- 0 until base.FETCH_WIDTH){
                when(
                    storebuffer_item_reg(i).valid & io.rob_items_i(j).valid &
                    (storebuffer_item_reg(i).rob_id === io.rob_items_i(j).id)
                ){
                    storebuffer_item_reg(i).rob_rdy := io.rob_items_i(j).rdy
                }
            }
        }
    }

    tail := Mux(
        io.rob_state, 
        0.U, 
        Mux(io.wr_able, tail + io.store_buffer_write_cnt, tail)
    )
    rob_head := Mux(
        io.rob_state,
        0.U,
        Mux(
            storebuffer_item_reg(rob_head).rob_rdy & storebuffer_item_reg(rob_head).rdy &
            storebuffer_item_reg(rob_head + 1.U).rob_rdy & storebuffer_item_reg(rob_head + 1.U).rdy,
            rob_head + 2.U,
            Mux(storebuffer_item_reg(rob_head).rob_rdy & storebuffer_item_reg(rob_head).rdy, rob_head + 1.U, rob_head)
        )
    )
    head := Mux(
        io.rob_state,
        0.U,
        Mux(
            (head + 1.U) =/= tail & io.mem_write_en.asUInt === "b11".U, 
            head + 2.U,
            Mux(
                (head =/= tail) & io.mem_write_en.asUInt.orR,
                head + 1.U,
                head
            )
        )
    )
    /* connect */
    io.store_buffer_rdata := store_buffer_rdata
    io.store_buffer_rdata_valid := store_buffer_rvalid
    io.store_buffer_item_o := store_buffer_item_o
}

