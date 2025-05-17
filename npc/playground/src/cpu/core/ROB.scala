package cpu.core

import chisel3._
import chisel3.util._

import cpu.config._
import cpu.core.frontend._

/* 监听总线，看是否就绪 */
class ROB extends Module
{
    val io = IO(new Bundle {
        var rat_flush_en = Input(Bool())
        val rob_item_i = Input(Vec(base.FETCH_WIDTH, new ROBItem))
        /* 写入ROB的有效指令数，用来增加tail指针 */
        val inst_valid_cnt_i = Input(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))
        /* retire */
        /* 提交的指令数，用来增加head值 */
        val commit_num_i = Input(UInt(log2Ceil(base.FETCH_WIDTH).W))
        /* 输出头部的4条指令供retire stage判断能否提交 */
        val rob_item_o = Output(Vec(base.FETCH_WIDTH, new ROBItem))
        /* 核内总线消息 */
        val cdb_i = Input(new CDB)
        /* rob容量信息 */
        val robw_able = Output(Bool())
        val robr_able = Output(Bool())
    })
    val size = 1 << base.ROBID_WIDTH
    /* ROBID对应的队列中的位置序号 */
    var ROBID2LocMem = RegInit(VecInit(
        Seq.fill((1 << base.ROBID_WIDTH))((0.U)(base.ROBID_WIDTH.W))
    ))
    /* ROB队列MEM */
    var ROBItemMem = RegInit(VecInit(
        Seq.fill((1 << base.ROBID_WIDTH))((0.U).asTypeOf(new ROBItem))
    ))

    var head = RegInit((0.U)((base.ROBID_WIDTH + 1).W))
    var tail = RegInit((0.U)((base.ROBID_WIDTH + 1).W))

    io.robw_able := (tail + 1.U =/= head) & (tail + 2.U =/= head) & (tail + 3.U =/= head) & (tail + 4.U =/= head)  
    io.robr_able := (head =/= tail) & (head + 1.U =/= tail) & (head + 2.U =/= tail) & (head + 3.U =/= tail) 

    head := Mux(io.robr_able & ~io.rat_flush_en, (head + io.commit_num_i)(base.ROBID_WIDTH - 1, 0), Mux(io.rat_flush_en, 0.U, head))
    tail := Mux(io.robw_able & ~io.rat_flush_en, (tail + io.inst_valid_cnt_i)(base.ROBID_WIDTH - 1, 0), Mux(io.rat_flush_en, 0.U, tail))

    /* ROB wr logic */
    for(i <- 0 until base.FETCH_WIDTH){
        when(io.rob_item_i(i).valid & io.robw_able & ~io.rat_flush_en){
            ROBItemMem((tail + i.U)(base.ROBID_WIDTH - 1, 0)) := io.rob_item_i(i)
            ROBID2LocMem(io.rob_item_i(i).id) := (tail + i.U)(base.ROBID_WIDTH - 1, 0)
        }
    }
    for(i <- 0 until size){
        when(io.rat_flush_en){
            ROBItemMem(i) := 0.U.asTypeOf(new ROBItem)
            ROBID2LocMem(i) := 0.U
        }
    }
    /* ROB r logic */
    /* 默认输出head -> head + 3的指令 */
    var rob_item_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new ROBItem))
    ))
    for(i <- 0 until base.FETCH_WIDTH){
        rob_item_o(i) := ROBItemMem((head + i.U)(base.ROBID_WIDTH - 1, 0))
    }

    /* ROB总线更新逻辑 */
    /* 求出映射到的队列项，更新对应的Item */
    for(i <- 0 until base.ALU_NUM){
        when(io.cdb_i.alu_channel(i).valid){
            ROBItemMem(ROBID2LocMem(io.cdb_i.alu_channel(i).rob_id)).rdy := io.cdb_i.alu_channel(i).valid
            ROBItemMem(ROBID2LocMem(io.cdb_i.alu_channel(i).rob_id)).targetBrAddr := 
                io.cdb_i.alu_channel(i).branch_target_addr
        }
    }

    for(i <- 0 until base.AGU_NUM){
        when(io.cdb_i.agu_channel(i).valid){
            ROBItemMem(ROBID2LocMem(io.cdb_i.agu_channel(i).rob_id)).rdy := io.cdb_i.agu_channel(i).valid
        }
    }    
    
    /* connect */
    io.rob_item_o := rob_item_o

}