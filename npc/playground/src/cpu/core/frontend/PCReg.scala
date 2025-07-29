package cpu.core.frontend

import chisel3._
import chisel3.util._
import cpu.config._

/* 13 bit bhr ^ pc[15:3] IPC:0.303324 branch_pred_rate: 68.47% */
/* 8 bit bhr 拼接pc(10, 3) IPC: 0.303324 branch_pred_rate: 68.47% */
/* GHR与PC异或寻址PHT IPC:  branch_pred_rate: */
class PCReg extends Module
{
    val io = IO(new Bundle{
        /* retire stage */
        val freereg_rd_able = Input(Vec(base.FETCH_WIDTH, Bool()))
        val rat_flush_en = Input(Bool())
        val rob_state = Input(UInt(2.W))
        val rat_flush_pc = Input(UInt(base.ADDR_WIDTH.W))
        val store_buffer_wr_able = Input(Bool())
        /* fetch stage */
        val branch_pred_en = Input(Bool())
        val branch_pred_addr = Input(UInt(base.ADDR_WIDTH.W))
        /* 是否分支指令 */
        val retire_br_mask = Input(Vec(base.FETCH_WIDTH, Bool()))
        /* 是否跳转 */
        val retire_br_taken_vec = Input(Vec(base.FETCH_WIDTH, Bool()))
        /* BHT表序号 */
        val retire_bht_idx = Input(Vec(base.FETCH_WIDTH, UInt(8.W)))
        /* output */
        val global_pht_idx_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val local_pht_idx_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val bht_idx_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.BHTID_WIDTH.W)))
        val pc_o = Output(UInt(base.ADDR_WIDTH.W))
        val inst_valid_mask_o = Output(UInt(base.FETCH_WIDTH.W))
        val inst_valid_cnt_o = Output(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))
        /* control */
        val issue_wr_able = Input(Bool())
        val rob_wr_able = Input(Bool())
        val rob_freeid_rd_able = Input(Vec(base.FETCH_WIDTH, Bool()))
    })

    var pc_reg = RegInit((base.RESET_VECTOR.U)(base.ADDR_WIDTH.W))
    var inst_valid_mask = WireInit((0.U)(base.FETCH_WIDTH.W))
    var inst_valid_cnt = WireInit((0.U)(log2Ceil(base.FETCH_WIDTH + 1).W))
    var nextpc = WireInit((0.U)(base.ADDR_WIDTH.W))
    /* BHT Table */
    var bht_table_reg = RegInit(VecInit(
        Seq.fill(1 << base.BHTID_WIDTH)((0.U)(base.BHRID_WIDTH.W))
    ))
    switch(io.pc_o(3, 0))
    {
        is(0.U){
            inst_valid_mask := "b1111".U
            inst_valid_cnt  := 4.U
            nextpc := io.pc_o + 16.U
        }
        is(4.U){
            inst_valid_mask := "b0111".U
            inst_valid_cnt  := 3.U
            nextpc := io.pc_o + 12.U
        }
        is(8.U){
            inst_valid_mask := "b0011".U
            inst_valid_cnt  := 2.U
            nextpc := io.pc_o + 8.U
        }
        is(12.U){
            inst_valid_mask := "b0001".U
            inst_valid_cnt  := 1.U
            nextpc := io.pc_o + 4.U
        }
    }

    pc_reg := Mux(io.rat_flush_en, io.rat_flush_pc, 
        Mux(
            ((io.rob_state === 0.U)) & 
            io.freereg_rd_able.asUInt.andR & 
            io.store_buffer_wr_able &
            io.issue_wr_able &
            io.rob_wr_able & 
            io.rob_freeid_rd_able.asUInt.andR, 
            nextpc, 
            pc_reg
        ))

    /* 分支预测使用 */
    /* 分支全局历史移位寄存器 */
    var GHR = RegInit((0.U)(base.PHTID_WIDTH.W))
    var GHR_step = WireInit((0.U)((log2Ceil(base.FETCH_WIDTH) + 1).W))
    var GHR_step_mid = WireInit(VecInit(
        Seq.fill(log2Ceil(base.FETCH_WIDTH))((0.U)((log2Ceil(base.FETCH_WIDTH) + 1).W))
    ))
    GHR_step_mid(0) := 
        io.retire_br_mask(0).asTypeOf(UInt((log2Ceil(base.FETCH_WIDTH) + 1).W)) + 
        io.retire_br_mask(1).asTypeOf(UInt((log2Ceil(base.FETCH_WIDTH) + 1).W))
    GHR_step_mid(1) := 
        io.retire_br_mask(2).asTypeOf(UInt((log2Ceil(base.FETCH_WIDTH) + 1).W)) + 
        io.retire_br_mask(3).asTypeOf(UInt((log2Ceil(base.FETCH_WIDTH) + 1).W))        
    GHR_step := GHR_step_mid(0) + GHR_step_mid(1)

    var retire_br_taken_vec_mid = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    for(i <- 0 until base.FETCH_WIDTH){
        retire_br_taken_vec_mid(i) := false.B
    }
    switch(io.retire_br_mask.asUInt){
        is("b0000".U){}
        is("b0001".U){
            retire_br_taken_vec_mid(0) := io.retire_br_taken_vec(0)
        }
        is("b0010".U){
            retire_br_taken_vec_mid(0) := io.retire_br_taken_vec(1)
        }
        is("b0011".U){
            retire_br_taken_vec_mid(0) := io.retire_br_taken_vec(0)
            retire_br_taken_vec_mid(1) := io.retire_br_taken_vec(1)
        }
        is("b0100".U){
            retire_br_taken_vec_mid(0) := io.retire_br_taken_vec(2)
        }
        is("b0101".U){
            retire_br_taken_vec_mid(0) := io.retire_br_taken_vec(0)
            retire_br_taken_vec_mid(1) := io.retire_br_taken_vec(2)
        }
        is("b0110".U){
            retire_br_taken_vec_mid(0) := io.retire_br_taken_vec(1)
            retire_br_taken_vec_mid(1) := io.retire_br_taken_vec(2)
        }
        is("b0111".U){
            retire_br_taken_vec_mid(0) := io.retire_br_taken_vec(0)
            retire_br_taken_vec_mid(1) := io.retire_br_taken_vec(1)
            retire_br_taken_vec_mid(2) := io.retire_br_taken_vec(2)
        }
        is("b1000".U){
            retire_br_taken_vec_mid(0) := io.retire_br_taken_vec(3)
        }
        is("b1001".U){
            retire_br_taken_vec_mid(0) := io.retire_br_taken_vec(0)
            retire_br_taken_vec_mid(1) := io.retire_br_taken_vec(3)
        }
        is("b1010".U){
            retire_br_taken_vec_mid(0) := io.retire_br_taken_vec(1)
            retire_br_taken_vec_mid(1) := io.retire_br_taken_vec(3)
        }
        is("b1011".U){
            retire_br_taken_vec_mid(0) := io.retire_br_taken_vec(0)
            retire_br_taken_vec_mid(1) := io.retire_br_taken_vec(1)
            retire_br_taken_vec_mid(2) := io.retire_br_taken_vec(3)
        }
        is("b1100".U){
            retire_br_taken_vec_mid(0) := io.retire_br_taken_vec(2)
            retire_br_taken_vec_mid(1) := io.retire_br_taken_vec(3)
        }
        is("b1101".U){
            retire_br_taken_vec_mid(0) := io.retire_br_taken_vec(0)
            retire_br_taken_vec_mid(1) := io.retire_br_taken_vec(2)
            retire_br_taken_vec_mid(2) := io.retire_br_taken_vec(3)
        }
        is("b1110".U){
            retire_br_taken_vec_mid(0) := io.retire_br_taken_vec(1)
            retire_br_taken_vec_mid(1) := io.retire_br_taken_vec(2)
            retire_br_taken_vec_mid(2) := io.retire_br_taken_vec(3)
        }
        is("b1111".U){
            retire_br_taken_vec_mid(0) := io.retire_br_taken_vec(0)
            retire_br_taken_vec_mid(1) := io.retire_br_taken_vec(1)
            retire_br_taken_vec_mid(2) := io.retire_br_taken_vec(2)
            retire_br_taken_vec_mid(3) := io.retire_br_taken_vec(3)
        }
    }

    /* 更新GHR */
    GHR := (GHR << GHR_step) | retire_br_taken_vec_mid.asUInt
    var global_pht_idx_vec_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PHTID_WIDTH.W))
    ))
    /* PC hash mapping */
    for(i <- 0 until base.FETCH_WIDTH){
        global_pht_idx_vec_o(i) := pc_reg(15, 3) ^ GHR
    }

    /* BHT也使用PC ^ GHR寻址，避免BHR别名问题 */
    /* 
        BHR的PHT使用(PC ^ GHR) | BHR寻址, 取PC和GHR的最新的8次分支结果拼接BHR的结果，
        避免纯BHR寻址的别名情况 
    */
    var local_pht_idx_vec_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PHTID_WIDTH.W))
    ))
    var bht_idx_vec_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.BHTID_WIDTH.W))
    ))
    for(i <- 0 until base.FETCH_WIDTH){
        local_pht_idx_vec_o(i) := Cat(
            pc_reg(10, 3), 
            bht_table_reg(pc_reg(10, 3))
        )
    }
    /* 更新BHR */
    for(i <- 0 until base.FETCH_WIDTH){
        bht_table_reg(io.retire_bht_idx(i)) := Mux(
            io.retire_br_mask(i), 
            (bht_table_reg(io.retire_bht_idx(i)) << 1) | io.retire_br_taken_vec(i), 
            bht_table_reg(io.retire_bht_idx(i))
        )
    }
    /* 有分支使能，输出分支指令目标PC */
    io.pc_o := Mux(io.branch_pred_en, io.branch_pred_addr, pc_reg)
    io.inst_valid_mask_o := Mux(~io.rat_flush_en, inst_valid_mask, 0.U)
    io.inst_valid_cnt_o  := Mux(~io.rat_flush_en, inst_valid_cnt, 0.U)
    io.global_pht_idx_vec_o := global_pht_idx_vec_o
    io.local_pht_idx_vec_o := local_pht_idx_vec_o
    io.bht_idx_vec_o := bht_idx_vec_o
}