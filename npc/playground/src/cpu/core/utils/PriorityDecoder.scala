package cpu.core.utils

import chisel3._
import chisel3.util._

/* 返回最高位开始第一个1的位置 */
class PriorityDecoder(width : Int) extends Module{
    val io = IO(new Bundle {
        val in = Input(UInt(width.W))
        val out = Output(UInt(log2Ceil(width).W))
    })

    var sign = WireInit(VecInit(
        Seq.fill(width)(false.B)
    ))

    /* 某位高位全0且本位为1 */
    for(i <- 0 until width){
        sign(i) := ~(io.in(width - 1, i+1).orR) & io.in(i)
    }

    var out = WireInit((0.U)(log2Ceil(width).W))
    /* 独热码转二进制 */
    out := Mux(sign.asUInt === 0.U, 0.U, OHToUInt(sign))
}