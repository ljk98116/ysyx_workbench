package cpu.core.frontend

import chisel3._
import chisel3.util._

import cpu.config._
import cpu.config.base.PHTID_WIDTH

/* fetch阶段读取PCReg段写入，读取在下一周期给出结果，会缓存读信号 */
/* 使用chisel Mem, 同时读取出饱和计数器值 */
/* 局部历史PHT和全局历史PHT，综合二者的结果 */
class PHTReg extends Module{
    val io = IO(new Bundle{
        /* retire段全局、局部历史PHT索引 */
        /* 是否前置无异常指令的分支指令 */
        val rob_state = Input(UInt(2.W))
        val retire_br_mask = Input(Vec(base.FETCH_WIDTH, Bool()))
        /* 是否跳转 */
        val retire_br_taken_vec = Input(Vec(base.FETCH_WIDTH, Bool()))
        val retire_gbranch_pre_res_i = Input(Vec(base.FETCH_WIDTH, Bool()))
        val retire_lbranch_pre_res_i = Input(Vec(base.FETCH_WIDTH, Bool()))
        val retire_gpht_idx = Input(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val retire_lpht_idx = Input(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        /* 当前全局、局部历史PHT索引 */
        val global_pht_idx_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val local_pht_idx_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        /* 使用全局/局部历史预测 */
        val gbranch_pre_res_o = Output(Vec(base.FETCH_WIDTH, Bool()))
        val lbranch_pre_res_o = Output(Vec(base.FETCH_WIDTH, Bool()))
        /* 分支预测方向 */
        val branch_pre_res_o = Output(Vec(base.FETCH_WIDTH, Bool()))
    })   

    var gPHTReg = Mem(1 << base.PHTID_WIDTH, UInt(2.W))
    var lPHTReg = Mem(1 << base.PHTID_WIDTH, UInt(2.W))
    /* 使用lPHT的索引来寻址 */
    var cPHTReg = Mem(1 << base.PHTID_WIDTH, UInt(2.W))
    /* 默认不跳转 */
    var gbranch_pre_res_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var lbranch_pre_res_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var branch_pre_res_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    for(i <- 0 until 1 << base.PHTID_WIDTH){
        when(reset.asBool){
            gPHTReg.write(i.U, "b11".U)
            lPHTReg.write(i.U, "b11".U)
            cPHTReg.write(i.U, "b11".U)
        }
    }

    /* 更新gPHT */
    for(i <- 0 until base.FETCH_WIDTH){
        var gPHTReg_val = WireInit((0.U)(2.W))
        gPHTReg_val := gPHTReg.read(io.global_pht_idx_vec_i(i))
        when((io.rob_state =/= "b11".U) & io.retire_br_mask(i)){
            gPHTReg.write(io.retire_gpht_idx(i), Mux(
                io.retire_br_taken_vec(i),
                Mux(gPHTReg_val =/= "b11".U, gPHTReg_val + 1.U, gPHTReg_val),
                Mux(gPHTReg_val =/= "b00".U, gPHTReg_val - 1.U, gPHTReg_val)
            ))
        }
    }

    /* 更新lPHT */
    for(i <- 0 until base.FETCH_WIDTH){
        var lPHTReg_val = WireInit((0.U)(2.W))
        lPHTReg_val := lPHTReg.read(io.retire_lpht_idx(i))
        when((io.rob_state =/= "b11".U) & io.retire_br_mask(i)){
            lPHTReg.write(io.retire_lpht_idx(i), Mux(
                io.retire_br_taken_vec(i),
                Mux(lPHTReg_val =/= "b11".U, lPHTReg_val + 1.U, lPHTReg_val),
                Mux(lPHTReg_val =/= "b00".U, lPHTReg_val - 1.U, lPHTReg_val)
            ))
        }
    }

    /* 更新cPHT */
    /* 0-1使用gPHT, 2-3使用lPHT */
    for(i <- 0 until base.FETCH_WIDTH){
        var cPHTReg_val = WireInit((0.U)(2.W))
        cPHTReg_val := cPHTReg.read(io.retire_lpht_idx(i))
        when((io.rob_state =/= "b11".U) & io.retire_br_mask(i)){
            cPHTReg.write(io.retire_lpht_idx(i), Mux(
                ~(io.retire_gbranch_pre_res_i(i) ^ io.retire_lbranch_pre_res_i(i)),
                cPHTReg_val,
                Mux(
                    /* 全局分支预测正确 */
                    ~(io.retire_gbranch_pre_res_i(i) ^ io.retire_br_taken_vec(i)),
                    Mux(cPHTReg_val =/= "b00".U, cPHTReg_val - 1.U, cPHTReg_val),
                    Mux(cPHTReg_val =/= "b11".U, cPHTReg_val + 1.U, cPHTReg_val)
                )
            ))
        }
    }

    /* 计算预测方向 */
    for(i <- 0 until base.FETCH_WIDTH){
        gbranch_pre_res_o(i) := gPHTReg.read(io.global_pht_idx_vec_i(i))(1)
        lbranch_pre_res_o(i) := lPHTReg.read(io.local_pht_idx_vec_i(i))(1)
        branch_pre_res_o(i) := Mux(
            cPHTReg.read(io.local_pht_idx_vec_i(i))(1), 
            lbranch_pre_res_o(i),
            gbranch_pre_res_o(i)
        )
    }

    /* connect */
    io.gbranch_pre_res_o := gbranch_pre_res_o
    io.lbranch_pre_res_o := lbranch_pre_res_o
    io.branch_pre_res_o := branch_pre_res_o
}