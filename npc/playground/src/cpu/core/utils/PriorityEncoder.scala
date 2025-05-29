package cpu.core.utils

import chisel3._
import chisel3.util._

/* 找到从低到高的第一个1出现的位置 */
/* 根据条件求取位数相关的独热码 */
/* 独热码转二进制 */
/* 4级逻辑 */
class PriorityEncoder(width : Int) extends Module{
    assert(width >= 2)
    val io = IO(new Bundle {
        val val_i = Input(UInt(width.W))
        val idx_o = Output(UInt(log2Ceil(width).W))
    })
    /* 维护每一个位置低位是否为0，且自己是1 */
    /* 2 stage logic */
    var valid_mask_vec = WireInit(VecInit(
        Seq.fill(width)(false.B)
    ))
    for(i <- 1 until width - 1){
        valid_mask_vec(i) := ~io.val_i(i-1, 0).orR & io.val_i(i)
    }
    valid_mask_vec(0) := io.val_i(0)
    valid_mask_vec(width - 1) := ~io.val_i(width - 2, 0).orR & io.val_i(width -1)
    /* 找最高位的1的位置 */
    var idx_bit_vec = WireInit(VecInit(
        Seq.fill(log2Ceil(width))(false.B)
    ))
    for(i <- 0 until log2Ceil(width)){
        var valid_vec = WireInit(VecInit(
            Seq.fill(width)(false.B)
        ))
        for(j <- 0 until width){
            if((j & (1 << i)) != 0){
                valid_vec(j) := valid_mask_vec(j)
            }
        }
        idx_bit_vec(i) := valid_vec.asUInt.orR
    }
    io.idx_o := idx_bit_vec.asUInt
}