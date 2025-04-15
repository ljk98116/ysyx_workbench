package cpu.core.backend

import chisel3._
import chisel3.util._

import cpu.config._

/* 指定step长度和队列大小 */
/* 需要根据ROBID找到对应的指令位置, 使用额外的Mem */
class ReserveStation(stepsize : Int, size: Int) extends Module {
    val width = log2Ceil(size)
    val stepwidth = log2Ceil(stepsize)
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

    /* 根据valid标志位维护查找表 */
    /* 计算有效、无效项对应的尾部插入的偏移量 */
    var valid_idx_mapping = VecInit(
        Seq.tabulate(1 << stepsize)((i) => {
            var init = Array.fill(stepsize)((1 << stepsize))
            var cnt = 0
            var idx = 0
            var n = i
            while(n > 0){
                if((n & 1) != 0) {
                    init(cnt) = idx
                    idx += 1
                }
                cnt += 1
                n = n >>> 1
            }
            var ret = VecInit(Seq.tabulate(stepsize)((i) => {
                init(i).U
            }))
            ret
        })
    )
    /* 通过查找表得到当前进入保留站的所有指令对应的尾部插入的偏移 */
    var insert_off_vec = WireInit(VecInit(
        Seq.fill(stepsize)(stepsize.U(stepwidth.W))
    ))
    insert_off_vec := valid_idx_mapping(rob_valid_vec.asUInt)

    for(i <- 0 until stepsize){

    }
}