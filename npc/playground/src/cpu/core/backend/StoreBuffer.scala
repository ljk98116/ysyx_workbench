package cpu.core.backend

import cpu.config._
import chisel3._
import chisel3.util._
import cpu.config.base.AGU_NUM

/* store指令存储阵列*/
/* 写ROB时同时写入，根据总线消息维护对应信息 */
/* 输出所有项的store指令对应的地址 */
/* store buffer一次发射一条store指令 */
class StoreBuffer(size : Int) extends Module{
    var width = log2Ceil(size)
    val io = IO(new Bundle{
        /* Dispatch */
        val store_buffer_write_en = Input(Bool())
        val store_buffer_item_cnt = Input(UInt((log2Ceil(base.FETCH_WIDTH) + 1).W))
        val store_buffer_item_i = Input(Vec(base.FETCH_WIDTH, new StoreBufferItem))
        /* AGU更新store buffer */
        val agu_result_i = Input(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val agu_wmask_i = Input(Vec(base.AGU_NUM, UInt(8.W)))
        val agu_wdata_i = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val agu_lsflag_i = Input(Vec(base.AGU_NUM, Bool()))
        val agu_robid_i = Input(Vec(base.AGU_NUM, UInt(base.ROBID_WIDTH.W)))
        /* MemStage1检查store buffer */
        val store_buffer_target_addrs = Output(Vec(size, UInt(base.ADDR_WIDTH.W)))
        /* MemStage2 load forwarding */
        val store_buffer_ren = Input(Vec(base.AGU_NUM, Bool()))
        val store_buffer_raddr = Input(Vec(base.AGU_NUM, UInt(width.W)))
        val store_buffer_rmask = Input(Vec(base.AGU_NUM, UInt(8.W)))
        val store_buffer_rdata = Output(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        /* retire段，ROB头部项 */
        val rob_items_i = Input(Vec(base.FETCH_WIDTH, new ROBItem))
        var rob_item_rdy_mask = Input(UInt(base.FETCH_WIDTH.W))
        /* 输出队列头部Item */
        val store_buffer_item_o = Output(new StoreBufferItem)
        val wr_able = Output(Bool())
    })

    var store_buffer_vec = RegInit(VecInit(
        Seq.fill(size)((0.U).asTypeOf(new StoreBufferItem))
    ))

    var store_buffer_mapping = RegInit(VecInit(
        Seq.fill(1 << base.ROBID_WIDTH)((0.U)(width.W))
    ))

    var store_buffer_rdata = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W))
    ))

    var head = RegInit((0.U)(width.W))
    var tail = RegInit((0.U)(width.W))
    var wr_able = WireInit(true.B)
    var rd_able = WireInit(false.B)

    /* 更新写入项逻辑 */
    for(i <- 0 until base.FETCH_WIDTH){
        when(io.store_buffer_write_en & io.wr_able & io.store_buffer_item_i(i).valid){
            store_buffer_vec(tail + i.U) := io.store_buffer_item_i(i)
            store_buffer_mapping(io.store_buffer_item_i(i).rob_id) := tail + i.U
        }
    }

    when(rd_able & store_buffer_vec(head).rdy){
        store_buffer_vec(head) := 0.U.asTypeOf(new StoreBufferItem)
        store_buffer_mapping(store_buffer_vec(head).rob_id) := 0.U
    }

    wr_able := tail + io.store_buffer_item_cnt < head
    rd_able := head =/= tail

    tail := Mux(wr_able, tail + io.store_buffer_item_cnt, tail)
    head := Mux(rd_able & store_buffer_vec(head).rdy, head + 1.U, head)

    var store_buffer_target_addrs = WireInit(VecInit(
        Seq.fill(size)((0.U)(base.ADDR_WIDTH.W))
    ))
    
    for(i <- 0 until size){
        store_buffer_target_addrs(i) := store_buffer_vec(i).agu_result
    }

    for(i <- 0 until base.AGU_NUM){
        when(io.agu_lsflag_i(i)){
            store_buffer_vec(store_buffer_mapping(io.agu_robid_i(i))).agu_result := io.agu_result_i(i)
            store_buffer_vec(store_buffer_mapping(io.agu_robid_i(i))).rdy := true.B
            store_buffer_vec(store_buffer_mapping(io.agu_robid_i(i))).wmask := io.agu_wmask_i(i)
            store_buffer_vec(store_buffer_mapping(io.agu_robid_i(i))).wdata := io.agu_wdata_i(i)
        }
    }

    for(i <- 0 until base.AGU_NUM){
        when(io.store_buffer_ren(i)){
            store_buffer_rdata(i) := store_buffer_vec(io.store_buffer_raddr(i)).wdata
        }.otherwise{
            store_buffer_rdata(i) := 0.U
        }
    }

    /* 更新ROB顶部指令状态 */
    for(i <- 0 until base.FETCH_WIDTH){
        when(io.rob_item_rdy_mask(i) & (io.rob_items_i(i).Opcode === Opcode.SW)){
            store_buffer_vec(store_buffer_mapping(io.rob_items_i(i).id)).rob_rdy := 
                store_buffer_vec(store_buffer_mapping(io.rob_items_i(i).id)).valid
        }
    }

    /* connect */
    io.wr_able := wr_able
    io.store_buffer_item_o := store_buffer_vec(head)
    io.store_buffer_target_addrs := store_buffer_target_addrs
    io.store_buffer_rdata := store_buffer_rdata
}

