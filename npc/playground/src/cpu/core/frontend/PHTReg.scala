package cpu.core.frontend

import chisel3._
import chisel3.util._

import cpu.config._

/* fetch阶段读取PCReg段写入，读取在下一周期给出结果，会缓存读信号 */
/* 使用chisel Mem, 同时读取出饱和计数器值 */
/* 局部历史PHT和全局历史PHT，综合二者的结果 */
class PHTReg extends Module{
    val io = IO(new Bundle{
        /* retire段全局、局部历史PHT索引 */
        /* 是否分支指令 */
        val rob_state = Input(Bool())
        val retire_br_mask = Input(Vec(base.FETCH_WIDTH, Bool()))
        /* 是否跳转 */
        val retire_br_taken_vec = Input(Vec(base.FETCH_WIDTH, Bool()))
        /* 是否发生预测错误 */
        val retire_br_pred_vec = Input(Vec(base.FETCH_WIDTH, Bool()))

        val retire_gpht_idx = Input(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val retire_lpht_idx = Input(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        /* 当前全局、局部历史PHT索引 */
        val global_pht_idx_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val local_pht_idx_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        /* 分支预测方向 */
        val branch_pre_res_o = Output(Vec(base.FETCH_WIDTH, Bool()))
    })   

    var gPHTReg = Mem(1 << base.PHTID_WIDTH, UInt(2.W))
    var lPHTReg = Mem(1 << base.PHTID_WIDTH, UInt(2.W))
    /* 默认不跳转 */
    var branch_pre_res_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    /* 更新gPHT */
    for(i <- 0 until base.FETCH_WIDTH){
        when(~io.rob_state & retire_br_mask(i)){
            
        }
    }
}