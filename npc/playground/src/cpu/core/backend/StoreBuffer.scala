package cpu.core.backend

import cpu.config._
import chisel3._
import chisel3.util._
import cpu.config.base.AGU_NUM

/* store指令存储阵列*/
/* 写ROB时同时写入，根据总线消息维护对应信息 */
/* 输出所有项的store指令对应的地址 */
/* store buffer一次发射2条store指令 */
class StoreBufferBank(size : Int) extends Module{
    assert(size == 4)
    var width = log2Ceil(size)
    val io = IO(new Bundle{
        val rat_flush_en = Input(Bool())
        /* Dispatch */
        val store_buffer_write_en = Input(Vec(base.FETCH_WIDTH / base.AGU_NUM, Bool()))
        val store_buffer_item_i = Input(Vec(base.FETCH_WIDTH / base.AGU_NUM, new StoreBufferItem))
        /* AGU */
        val cdb_i = Input(new CDB)
        /* MemStage1检查store buffer */
        val load_ren = Input(Bool())
        val load_raddr_i = Input(UInt(base.ADDR_WIDTH.W))
        val load_raw_mask = Output(UInt(size.W))
        /* MemStage2 load forwarding */
        val store_buffer_ren = Input(Bool())
        val store_buffer_raddr = Input(UInt(width.W))
        val store_buffer_rdata = Output(UInt(base.DATA_WIDTH.W))  
        /* retire段，ROB头部项 */
        val rob_items_i = Input(Vec(base.FETCH_WIDTH, new ROBItem))
        /* 输出队列头部Item */
        val store_buffer_item_o = Output(new StoreBufferItem)
        val wr_able = Output(Bool())    
        val rd_able = Output(Bool()) 
    })

    var store_buffer_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new StoreBufferItem))
    ))

    var head = RegInit((0.U)(width.W))
    var tail = RegInit((0.U)(width.W))

    var load_raw_mask = WireInit(VecInit(
        Seq.fill(size)(false.B)
    ))

    var store_buffer_item_i_valid_mask = WireInit(VecInit(
        Seq.fill(2)(false.B)
    ))
    store_buffer_item_i_valid_mask(0) := io.store_buffer_item_i(0).valid
    store_buffer_item_i_valid_mask(1) := io.store_buffer_item_i(1).valid

    io.wr_able := ((tail + 2.U =/= head) & store_buffer_item_i_valid_mask.asUInt.xorR) | 
        ((tail + 3.U =/= head) & store_buffer_item_i_valid_mask.asUInt.andR) | 
        ((tail + 1.U =/= head) & ~store_buffer_item_i_valid_mask.asUInt.orR)
    
    io.rd_able := head =/= tail
    var wstep = WireInit((0.U)(base.AGU_NUM.W))
    var rstep = WireInit(false.B)

    wstep := store_buffer_item_i_valid_mask(0) + store_buffer_item_i_valid_mask(1)
    rstep := store_buffer_reg(head).rob_rdy

    head := Mux(io.rd_able & ~io.rat_flush_en, head + rstep, Mux(io.rat_flush_en, 0.U, head))
    tail := Mux(io.wr_able & ~io.rat_flush_en, tail + wstep, Mux(io.rat_flush_en, 0.U, tail))
    for(i <- 0 until size){
        load_raw_mask(i) := store_buffer_reg(i).agu_result === io.load_raddr_i & io.load_ren

        var retire_rdy_mask = WireInit(VecInit(
            Seq.fill(base.FETCH_WIDTH)(false.B)
        ))
        for(j <- 0 until base.FETCH_WIDTH){
            retire_rdy_mask(j) := 
                (store_buffer_reg(i).rob_id === io.rob_items_i(j).id) & 
                io.rob_items_i(j).rdy & 
                store_buffer_reg(i).valid
        }
        when(io.rat_flush_en){
            store_buffer_reg(i) := 0.U.asTypeOf(new StoreBufferItem)
        }.elsewhen((i.U === tail) & io.store_buffer_item_i(0).valid & io.store_buffer_write_en(0)){
            store_buffer_reg(i) := io.store_buffer_item_i(0)
        }.elsewhen((i.U === (tail + 1.U)) & io.store_buffer_item_i(1).valid & io.store_buffer_write_en(1)){
            store_buffer_reg(i) := io.store_buffer_item_i(1)
        }.elsewhen(retire_rdy_mask.asUInt.orR){
            store_buffer_reg(i).rob_rdy := true.B
        }.otherwise{
            var agu_hit_mask = WireInit(VecInit(
                Seq.fill(base.AGU_NUM)(false.B)
            ))
            for(j <- 0 until base.AGU_NUM){
                agu_hit_mask(j) := store_buffer_reg(i).valid & 
                    store_buffer_reg(i).rob_id === io.cdb_i.agu_channel(j).rob_id & 
                    io.cdb_i.agu_channel(j).valid
            }
            switch(agu_hit_mask.asUInt){
                is("b00".U){}
                is("b01".U){
                    store_buffer_reg(i).rdy := true.B
                    store_buffer_reg(i).agu_result := io.cdb_i.agu_channel(0).reg_wr_data
                    store_buffer_reg(i).wdata := io.cdb_i.agu_channel(0).wdata
                    store_buffer_reg(i).wmask := io.cdb_i.agu_channel(0).wmask
                }
                is("b10".U){
                    store_buffer_reg(i).rdy := true.B
                    store_buffer_reg(i).agu_result := io.cdb_i.agu_channel(1).reg_wr_data
                    store_buffer_reg(i).wdata := io.cdb_i.agu_channel(1).wdata
                    store_buffer_reg(i).wmask := io.cdb_i.agu_channel(1).wmask                    
                }
            }
        }
    }
    /* connect */

    io.load_raw_mask := load_raw_mask

}

class StoreBuffer(size : Int) extends Module{
    assert(size == 4)
    var width = log2Ceil(size)
    val io = IO(new Bundle{
        val rat_flush_en = Input(Bool())
        /* Dispatch */
        val store_buffer_write_en = Input(Vec(base.FETCH_WIDTH, Bool()))
        val store_buffer_item_i = Input(Vec(base.FETCH_WIDTH, new StoreBufferItem))
        /* AGU更新store buffer */
        val cdb_i = Input(new CDB)
        /* MemStage1检查store buffer */
        val load_raddr_i = Input(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val load_raw_mask = Output(Vec(base.AGU_NUM, UInt(size.W)))
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


}

