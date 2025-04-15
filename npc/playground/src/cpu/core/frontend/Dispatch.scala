package cpu.core.frontend

import chisel3._
import chisel3.util._

import cpu.config._
import upickle.default

/* 发送ROB到ROB队列以及各个种类的发射保留站 */
/* 使用lut统计各种指令的数目, ALU固定4通道无需计算数量, 其他类型FU需要计算步长 */
/* 发射写入时需要判断写入哪个位置的结果 */
class Dispatch extends Module
{
    val agu_step = base.FETCH_WIDTH / base.AGU_NUM
    var io = IO(new Bundle {
        val rob_item_i = Input(Vec(base.FETCH_WIDTH, new ROBItem))
        val alu_items_vec_o = Output(Vec(base.ALU_NUM, new ROBItem))
        val agu_items_vec_o = Output(
            Vec(base.AGU_NUM, Vec(agu_step, new ROBItem)))
        val agu_items_cnt_vec_o = Output(Vec(base.AGU_NUM, UInt((log2Ceil(agu_step) + 1).W)))
    })

    /* pipeline */
    var rob_item_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new ROBItem))
    ))
    rob_item_reg := io.rob_item_i

    var agu_cnt_lut = VecInit(
        Seq.tabulate(1 << (base.FETCH_WIDTH / base.AGU_NUM))(
            (i) => Integer.bitCount(i).U((log2Ceil(agu_step) + 1).W)
        )        
    )

    var agu_valid_mask = WireInit((0.U)(base.FETCH_WIDTH.W))
    var agu_items_cnt_vec_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)((log2Ceil(agu_step) + 1).W))
    ))

    var agu_items_vec_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)(
            VecInit(Seq.fill(base.FETCH_WIDTH / base.AGU_NUM)((0.U).asTypeOf(new ROBItem)))
        )
    ))

    var alu_items_vec_o = WireInit(VecInit(
        Seq.fill(base.ALU_NUM)((0.U).asTypeOf(new ROBItem))
    ))

    /* 根据指令类型确定FU */
    for(i <- 0 until base.FETCH_WIDTH){
        when(rob_item_reg(i).valid){
            when(
                rob_item_reg(i).Opcode === Opcode.SW
            ){
                agu_valid_mask(i) := true.B
                agu_items_vec_o(i / 2)(i % 2) := rob_item_reg(i)
            }.otherwise{
                alu_items_vec_o(i) := rob_item_reg(i)
            }
        }
    }

    for(i <- 0 until base.AGU_NUM){
        agu_items_cnt_vec_o(i) := agu_cnt_lut(agu_valid_mask(agu_step * i + 1, agu_step * i))
    }

    /* connect */
    io.alu_items_vec_o := alu_items_vec_o

    io.agu_items_vec_o := agu_items_vec_o
    io.agu_items_cnt_vec_o := agu_items_cnt_vec_o

}