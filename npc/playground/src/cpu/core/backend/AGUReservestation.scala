package cpu.core.backend

import chisel3._
import chisel3.util._

import cpu.config._

/* 顺序接收ROB项，顺序发射，效率极低 */
/* 异常时清空队列 */
class AGUReservestation(stepsize:Int, size : Int) extends Module
{
    val width = log2Ceil(size)
    val io = IO(new Bundle{
        val rat_flush_en = Input(Bool())
        var rob_item_i = Input(Vec(stepsize, new ROBItem))
        var valid_cnt_i = Input(UInt(log2Ceil(stepsize).W))
        /* 总线状态 */
        var cdb_i = Input(new CDB)
        var rob_item_o = Output(new ROBItem)
        var write_able = Bool()
        var read_able = Bool() 
    })

    var rob_item_reg = RegInit(VecInit(
        Seq.fill(size)((0.U).asTypeOf(new ROBItem))
    ))

    var head = RegInit((0.U)(width.W))
    var tail = RegInit((0.U)(width.W))

    io.read_able := Mux(head + 1.U < tail, true.B, false.B)
    io.write_able := Mux(tail + io.valid_cnt_i < head, true.B, false.B)    

    var rob_item_o = WireInit((0.U).asTypeOf(new ROBItem))
    for(i <- 0 until stepsize){
        when(i.U < io.valid_cnt_i & io.write_able){
            rob_item_reg(tail + i.U) := io.rob_item_i(i)
        }
    }

    /* 更新 */
    for(j <- 0 until size){
        when(j.U >= head & j.U < tail & ~io.rat_flush_en){
            var issue_able_rs1_vec = WireInit(VecInit(
                Seq.fill(base.ALU_NUM + base.AGU_NUM)(false.B)
            ))
            var issue_able_rs2_vec = WireInit(VecInit(
                Seq.fill(base.ALU_NUM + base.AGU_NUM)(false.B)
            ))
            for(i <- 0 until base.ALU_NUM){
                issue_able_rs1_vec(i) := rob_item_reg(j).ps1 === io.cdb_i.alu_channel(i).phy_reg_id
                issue_able_rs2_vec(i) := rob_item_reg(j).ps2 === io.cdb_i.alu_channel(i).phy_reg_id
            }
            rob_item_reg(j).rdy1 := issue_able_rs1_vec.asUInt.orR & rob_item_reg(j).valid
            rob_item_reg(j).rdy2 := issue_able_rs2_vec.asUInt.orR & rob_item_reg(j).valid
        }.elsewhen(io.rat_flush_en){
            rob_item_reg(j) := 0.U.asTypeOf(new ROBItem)
        }
    }

    /* 仅检查队头位置 */
    var issue_able = WireInit(false.B)
    issue_able := ~(
        (rob_item_reg(head).HasRs1 & ~rob_item_reg(head).rdy1) |
        (rob_item_reg(head).HasRs2 & ~rob_item_reg(head).rdy2)
    )
    rob_item_o := Mux(issue_able, rob_item_reg(head), (0.U).asTypeOf(new ROBItem))

    head := Mux(io.read_able, head + issue_able.asUInt, Mux(~io.rat_flush_en, head, 0.U))
    tail := Mux(io.write_able, tail + io.valid_cnt_i, Mux(~io.rat_flush_en, tail, 0.U))

    /* connect */
    io.rob_item_o := rob_item_o
}