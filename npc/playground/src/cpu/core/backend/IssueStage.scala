package cpu.core.backend

import chisel3._
import chisel3.util._

import cpu.config._

class IssueStage extends Module
{
    val agu_step = base.FETCH_WIDTH / base.AGU_NUM

    var io = IO(new Bundle {
        val alu_items_vec_i = Input(Vec(base.ALU_NUM, new ROBItem))
        val agu_items_vec_i = Input(
            Vec(base.AGU_NUM, Vec(agu_step, new ROBItem)))
        val agu_items_cnt_vec_i = Input(Vec(base.AGU_NUM, UInt(agu_step.W)))
        val cdb_i = Input(new CDB)
    })

    /* pipeline */
    var alu_items_vec_reg = RegInit(VecInit(
        Seq.fill(base.ALU_NUM)((0.U).asTypeOf(new ROBItem))
    ))
    var agu_items_vec_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)(VecInit(
            Seq.fill(agu_step)((0.U).asTypeOf(new ROBItem))
        ))
    ))
    var agu_items_cnt_vec_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(agu_step.W))
    ))

    alu_items_vec_reg := io.alu_items_vec_i
    agu_items_vec_reg := io.agu_items_vec_i
    agu_items_cnt_vec_reg := io.agu_items_cnt_vec_i

    /* ReserveStations接收总线信号 */


}