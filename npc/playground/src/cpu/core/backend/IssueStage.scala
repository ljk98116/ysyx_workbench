package cpu.core.backend

import chisel3._
import chisel3.util._

import cpu.config._
import cpu.config.base.FETCH_WIDTH

class IssueStage extends Module
{
    val agu_step = base.FETCH_WIDTH / base.AGU_NUM

    var io = IO(new Bundle {
        val rat_flush_en = Input(Bool())
        val alu_items_vec_i = Input(Vec(base.ALU_NUM, new ROBItem))
        val agu_items_vec_i = Input(
            Vec(base.AGU_NUM, Vec(agu_step, new ROBItem)))
        val agu_items_cnt_vec_i = Input(Vec(base.AGU_NUM, UInt(agu_step.W)))
        val cdb_i = Input(new CDB)
        val alu_fu_items_o = Output(Vec(base.ALU_NUM, new ROBItem))
        val agu_fu_items_o = Output(Vec(base.AGU_NUM, new ROBItem))
        val alu_issue_read_able = Output(Vec(base.ALU_NUM, Bool()))
        val alu_issue_write_able = Output(Vec(base.ALU_NUM, Bool()))
        val agu_issue_read_able = Output(Vec(base.AGU_NUM, Bool()))
        val agu_issue_write_able = Output(Vec(base.AGU_NUM, Bool()))
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

    alu_items_vec_reg := Mux(~io.rat_flush_en, io.alu_items_vec_i, VecInit(Seq.fill(base.ALU_NUM)((0.U).asTypeOf(new ROBItem))))
    agu_items_vec_reg := Mux(~io.rat_flush_en, io.agu_items_vec_i, 
        VecInit(Seq.fill(base.AGU_NUM)(
            VecInit(Seq.fill(agu_step)((0.U).asTypeOf(new ROBItem)))
        )))
    agu_items_cnt_vec_reg := Mux(~io.rat_flush_en, io.agu_items_cnt_vec_i, VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(agu_step.W))
    ))

    /* ReserveStations接收总线信号 */
    /* ALU */
    var alu_reserve_stations = Seq.fill(base.ALU_NUM)(
        Module(new ALUReserveStation(4))
    )

    for(i <- 0 until base.ALU_NUM){
        alu_reserve_stations(i).io.cdb_i := io.cdb_i
        alu_reserve_stations(i).io.rob_item_i := alu_items_vec_reg(i)
        alu_reserve_stations(i).io.rat_flush_en := io.rat_flush_en
    }
    var alu_fu_items_o = WireInit(VecInit(
        Seq.fill(base.ALU_NUM)((0.U).asTypeOf(new ROBItem))
    ))
    var alu_issue_read_able = WireInit(VecInit(
        Seq.fill(base.ALU_NUM)(false.B)
    ))

    var alu_issue_write_able = WireInit(VecInit(
        Seq.fill(base.ALU_NUM)(false.B)
    ))
    for(i <- 0 until base.ALU_NUM){
        alu_fu_items_o(i) := alu_reserve_stations(i).io.rob_item_o
        alu_issue_read_able(i) := alu_reserve_stations(i).io.read_able
        alu_issue_write_able(i) := alu_reserve_stations(i).io.write_able
    }

    /* AGU */
    var agu_reserve_stations = Seq.fill(base.AGU_NUM){
        Module(new AGUReservestation(agu_step, 8))
    }
    for(i <- 0 until base.AGU_NUM){
        agu_reserve_stations(i).io.cdb_i := io.cdb_i
        agu_reserve_stations(i).io.rob_item_i := agu_items_vec_reg(i)
        agu_reserve_stations(i).io.valid_cnt_i := agu_items_cnt_vec_reg(i)
        agu_reserve_stations(i).io.rat_flush_en := io.rat_flush_en
    }

    var agu_fu_items_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U).asTypeOf(new ROBItem))
    ))
    var agu_issue_read_able = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))

    var agu_issue_write_able = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))    

    for(i <- 0 until base.AGU_NUM){
        agu_fu_items_o(i) := agu_reserve_stations(i).io.rob_item_o
        agu_issue_read_able(i) := agu_reserve_stations(i).io.read_able
        agu_issue_write_able(i) := agu_reserve_stations(i).io.write_able
    }

    /* connect */
    io.alu_fu_items_o := alu_fu_items_o
    io.alu_issue_read_able := alu_issue_read_able
    io.alu_issue_write_able := alu_issue_write_able
    io.agu_fu_items_o := agu_fu_items_o
    io.agu_issue_read_able := agu_issue_read_able
    io.agu_issue_write_able := agu_issue_write_able
}