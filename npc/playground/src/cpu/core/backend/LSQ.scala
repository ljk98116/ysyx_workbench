package cpu.core.backend

import cpu.config._
import chisel3._
import chisel3.util._

/* 接收并缓存AGU的计算结果 */
class LSQ(size : Int) extends Module
{
    val io = IO(new Bundle {
        val rob_item_i = Input(Vec(base.AGU_NUM, new ROBItem))
        val agu_result_i = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val agu_rw_mask_i = Input(Vec(base.AGU_NUM, UInt(4.W)))
        val agu_mem_wdata = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val lsq_item_o = Output(new LSQ_Item)
        val rd_able = Output(Bool())
        val wr_able = Output(Bool())
    })

    /* load/store指令, load为0, store为1 */
    var LS_FLAG = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))

    for(i <- 0 until base.AGU_NUM){
        LS_FLAG(i) := io.rob_item_i(i).Opcode === Opcode.SW
    }

    var lsq_item_reg = RegInit(VecInit(
        Seq.fill(size)((0.U).asTypeOf(new LSQ_Item))
    ))

    var lsq_item_o = WireInit((0.U).asTypeOf(new LSQ_Item))
    var head = RegInit((0.U)(log2Ceil(size).W))
    var tail = RegInit((0.U)(log2Ceil(size).W))
    var valid_cnt = WireInit((0.U)(log2Ceil(base.AGU_NUM).W))
    var valid_cnt_mask = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))
    var valid_idx = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(log2Ceil(base.AGU_NUM).W))
    ))

    for(i <- 0 until base.AGU_NUM){
        valid_cnt_mask(i) := io.rob_item_i(i).valid
    }

    switch(valid_cnt_mask.asUInt){
        is("b00".U){
            valid_cnt := 0.U
        }
        is("b01".U){
            valid_cnt := 1.U
            valid_idx(0) := 0.U
        }
        is("b10".U){
            valid_cnt := 1.U
            valid_idx(1) := 0.U
        }
        is("b11".U){
            valid_cnt := 2.U
            valid_idx(0) := 0.U
            valid_idx(1) := 1.U
        }
    }
    io.rd_able := Mux(head + 1.U < tail, true.B, false.B)
    io.wr_able := Mux(tail + valid_cnt < head, true.B, false.B)

    lsq_item_o := Mux(io.rd_able, lsq_item_reg(head), (0.U).asTypeOf(new LSQ_Item))

    head := Mux(io.rd_able, head + 1.U, head)
    tail := Mux(io.wr_able, tail + valid_cnt, tail)

    for(i <- 0 until base.AGU_NUM){
        lsq_item_reg(tail + valid_idx(i)).valid := io.rob_item_i(i).valid
        lsq_item_reg(tail + valid_idx(i)).LS_FLAG := LS_FLAG(i)
        lsq_item_reg(tail + valid_idx(i)).rw_addr := io.agu_result_i(i)
        lsq_item_reg(tail + valid_idx(i)).rw_mask := io.agu_rw_mask_i(i)
        lsq_item_reg(tail + valid_idx(i)).rw_data := io.agu_mem_wdata(i)
    }

    /* connect */
    io.lsq_item_o := lsq_item_o
}