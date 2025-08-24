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
        val rob_state = Input(UInt(2.W))
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
        val commit_valid_mask = Input(UInt(base.FETCH_WIDTH.W))
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

    /* load指令的上一个store指令的rob_id的位置要晚于地址匹配的位置 */
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
            // load_raw_mask_1(j) := 
            //     Cat(
            //         Cat(
            //             (io.store_buffer_ren(i) & storebuffer_item_reg(j).rdy), 
            //             (storebuffer_item_reg(j).agu_result === io.store_buffer_raddr(i))
            //         ).andR,
            //         Cat(
            //             (storebuffer_item_reg(j).wmask === io.store_buffer_rmask(i)),
            //             (((j.U < tail) & (head > tail)) | ((head < tail) & (j.U >= head) & (j.U <= tail)))
            //         ).andR,
            //         Cat(
            //             (~store_buffer_mapping(io.store_ids(i)(base.ROBID_WIDTH - 1, 0))(width)),
            //             ((store_buffer_mapping(io.store_ids(i)(base.ROBID_WIDTH - 1, 0)) >= j.U)
            //         ).andR,
            //         (store_buffer_mapping(io.store_ids(i)(base.ROBID_WIDTH - 1, 0)) < tail))
            //     ).andR 
                
            // load_raw_mask_2(j) := Cat(
            //     Cat(
            //         (io.store_buffer_ren(i) & storebuffer_item_reg(j).rdy),
            //         (storebuffer_item_reg(j).agu_result === io.store_buffer_raddr(i))
            //     ).andR,
            //     Cat(
            //         (storebuffer_item_reg(j).wmask === io.store_buffer_rmask(i)),
            //         (~store_buffer_mapping(io.store_ids(i)(base.ROBID_WIDTH - 1, 0))(width))
            //     ).andR,
            //     Cat(
            //         (((j.U >= head) & (head > tail))),
            //         (store_buffer_mapping(io.store_ids(i)(base.ROBID_WIDTH - 1, 0)) >= j.U)
            //     ).andR
            // ).andR
            load_raw_mask_1(j) := 
                io.store_buffer_ren(i) & 
                storebuffer_item_reg(j).rdy & 
                (storebuffer_item_reg(j).agu_result === io.store_buffer_raddr(i)) &
                (io.store_ids(i)(base.ROBID_WIDTH - 1, 0) === storebuffer_item_reg(j).rob_id)               
        }
        // load_raw_mask := Mux(load_raw_mask_1.asUInt =/= 0.U, load_raw_mask_1.asUInt, load_raw_mask_2.asUInt)
        prio_decoder_vec(i).io.in := load_raw_mask_1.asUInt
        /* 寄存器缓存最晚的命中的store指令的数据，下一周期输出 */
        raw_stIdx(i) := Mux(load_raw_mask_1.asUInt =/= 0.U, prio_decoder_vec(i).io.out, size.U)
        store_buffer_rdata(i) := storebuffer_item_reg(raw_stIdx(i)(width - 1, 0)).wdata
        store_buffer_rvalid(i) := raw_stIdx(i) =/= size.U
    }
    store_buffer_item_o(0) := Mux(rob_head =/= tail, storebuffer_item_reg(rob_head), 0.U.asTypeOf(new StoreBufferItem))
    store_buffer_item_o(1) := Mux((rob_head + 1.U) =/= tail, storebuffer_item_reg(rob_head + 1.U), 0.U.asTypeOf(new StoreBufferItem))
    
    /* 时序逻辑 */
    for(i <- 0 until size){
        var is_write_loc_vec = WireInit(VecInit(
            Seq.fill(base.FETCH_WIDTH)(false.B)
        ))
        for(j <- 0 until base.FETCH_WIDTH){
            is_write_loc_vec(j) := (i.U === (tail + j.U))
        }
        var write_loc = WireInit((0.U)(2.W))
        write_loc := OHToUInt(is_write_loc_vec.asUInt)
        var id = WireInit((0.U)(base.ROBID_WIDTH.W))
        id := storebuffer_item_reg(i).rob_id
        when(io.rob_state === "b11".U){
            for(j <- 0 until 1 << base.ROBID_WIDTH){
                store_buffer_mapping(j) := size.U
            }
            storebuffer_item_reg(i) := 0.U.asTypeOf(new StoreBufferItem)
        }.elsewhen(is_write_loc_vec.asUInt.orR & io.wr_able){
            storebuffer_item_reg(i) := Mux(io.store_buffer_item_i(write_loc).valid, io.store_buffer_item_i(write_loc), storebuffer_item_reg(i))
            store_buffer_mapping(io.store_buffer_item_i(write_loc).rob_id) := i.U
        }.otherwise{
            var agu_match = WireInit(VecInit(
                Seq.fill(base.AGU_NUM)(false.B)
            ))
            var agu_match_idx = WireInit((0.U)(1.W))
            for(j <- 0 until base.AGU_NUM){
                agu_match(j) := storebuffer_item_reg(i).valid & io.agu_valid(j) &
                    (storebuffer_item_reg(i).rob_id === io.agu_rob_id(j))
            }
            var prio_enc0 = Module(new PriorityEncoder(base.FETCH_WIDTH))
            prio_enc0.io.val_i := agu_match.asUInt
            agu_match_idx := prio_enc0.io.idx_o
            storebuffer_item_reg(i).rdy := Mux(agu_match.asUInt.orR, true.B, storebuffer_item_reg(i).rdy)
            storebuffer_item_reg(i).agu_result := Mux(agu_match.asUInt.orR, io.agu_result(agu_match_idx), storebuffer_item_reg(i).agu_result)
            storebuffer_item_reg(i).wdata := Mux(agu_match.asUInt.orR, io.agu_wdata(agu_match_idx), storebuffer_item_reg(i).wdata)
            storebuffer_item_reg(i).wmask := Mux(agu_match.asUInt.orR, io.agu_wmask(agu_match_idx), storebuffer_item_reg(i).wmask)

            var rob_rdy_match = WireInit(VecInit(
                Seq.fill(base.FETCH_WIDTH)(false.B)
            ))
            var rob_rdy_match_index = WireInit((0.U)(2.W))

            for(j <- 0 until base.FETCH_WIDTH){
                rob_rdy_match(j) := 
                    Cat(
                        storebuffer_item_reg(i).valid,
                        io.rob_items_i(j).valid
                    ).andR &
                    Cat(
                        io.commit_valid_mask(j),
                        (storebuffer_item_reg(i).rob_id === io.rob_items_i(j).id)
                    ).andR
            }
            var prio_enc1 = Module(new PriorityEncoder(base.FETCH_WIDTH))
            prio_enc1.io.val_i := rob_rdy_match.asUInt
            rob_rdy_match_index := prio_enc1.io.idx_o
            storebuffer_item_reg(i).rob_rdy := Mux(rob_rdy_match.asUInt.orR, io.rob_items_i(rob_rdy_match_index).rdy, storebuffer_item_reg(i).rob_rdy)
        }
    }
    tail := Mux(
        io.rob_state === "b11".U, 
        0.U, 
        Mux(io.wr_able, tail + io.store_buffer_write_cnt, tail)
    )
    rob_head := Mux(
        io.rob_state === "b11".U,
        0.U,
        Mux(
            storebuffer_item_reg(rob_head).rob_rdy & storebuffer_item_reg(rob_head).rdy &
            storebuffer_item_reg(rob_head + 1.U).rob_rdy & storebuffer_item_reg(rob_head + 1.U).rdy,
            rob_head + 2.U,
            Mux(storebuffer_item_reg(rob_head).rob_rdy & storebuffer_item_reg(rob_head).rdy, rob_head + 1.U, rob_head)
        )
    )
    head := Mux(
        io.rob_state === "b11".U,
        0.U,
        Mux(
            ((head + 1.U) =/= tail) & (io.mem_write_en.asUInt === "b11".U), 
            head + 2.U,
            Mux(
                (head =/= tail) & (io.mem_write_en.asUInt.orR),
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

