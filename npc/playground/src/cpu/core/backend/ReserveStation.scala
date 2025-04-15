package cpu.core.backend

import chisel3._
import chisel3.util._

import cpu.config._

/* 指定step长度和队列大小 */
/* 需要根据ROBID找到对应的指令位置, 使用额外的Mem */
class ReserveStation(stepsize : Int, size: Int) extends Module {
    val width = log2Ceil(size)
    val io = IO(new Bundle {
        val rob_item_i = Input(Vec(stepsize, new ROBItem))
        val valid_cnt_i = Input(UInt((log2Ceil(stepsize) + 1).W))
        val cdb_i = Input(new CDB)
        val rob_item_o = Output(new ROBItem)
        val write_able = Bool()
        val read_able = Bool()
    })

    /* 记录保留站每个位置的指令能否发射 */
    var issue_able = WireInit((0.U)(size.W))
    var ROBItemMem = Mem(size, new ROBItem)
    var ROBID2LocMem = Mem((1 << base.ROBID_WIDTH), UInt(width.W))

    var head = RegInit((0.U)(width.W))
    var tail = RegInit((0.U)(width.W))

    io.read_able := head + 1.U <= tail
    io.write_able := tail + (stepsize + 1).U < head

    head := Mux(io.read_able & issue_able.orR, head + 1.U, head)
    tail := Mux(io.write_able, tail + io.valid_cnt_i, tail)

    var rob_valid_vec = WireInit(VecInit(
        Seq.fill(stepsize)(false.B)
    ))

    for(i <- 0 until stepsize){
        rob_valid_vec(i) := io.rob_item_i(i).valid
    }

    for(i <- 0 until stepsize){
        when()
    }
}