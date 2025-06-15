package cpu.core

import chisel3._
import chisel3.util._

import cpu.config._
import cpu.core.frontend._

/* 监听总线，看是否就绪 */
/* 状态机刷新掉head->tail之间的部分,必须是状态机来恢复freereg状态，避免与RAT撞车 */
/* flush过程需要暂停整个CPU */
class ROB extends Module
{
    val io = IO(new Bundle {
        var rat_flush_en = Input(Bool())
        var retire_rdy_mask = Input(UInt(base.FETCH_WIDTH.W))
        var rob_state = Output(Bool())
        val rob_item_i = Input(Vec(base.FETCH_WIDTH, new ROBItem))
        /* 输出头部的4条指令,用来提交或者恢复现场 */
        val rob_item_o = Output(Vec(base.FETCH_WIDTH, new ROBItem))
        /* 核内总线消息 */
        val cdb_i = Input(new CDB)
        /* rob容量信息 */
        val robw_able = Output(Bool())
        val robr_able = Output(Bool())
    })

    val bankcap = (1 << base.ROBID_WIDTH) >> 2
    val bankwidth = log2Ceil(bankcap)
    var ROBBankRegs = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(
            VecInit(Seq.fill(bankcap)(0.U.asTypeOf(new ROBItem)))
        ))
    )
    val normal::flush::Nil = Enum(2)
    var rob_state = RegInit(normal)
    var next_rob_state = WireInit(normal)

    dontTouch(ROBBankRegs)

    var ROBIDLocMem = RegInit(VecInit(
        Seq.fill(1 << base.ROBID_WIDTH)((0.U)(bankwidth.W))
    ))

    var head = RegInit((0.U)(bankwidth.W))
    var tail = RegInit((0.U)(bankwidth.W))

    var rob_item_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new ROBItem))
    ))
    io.robr_able := head =/= tail
    io.robw_able := tail + 1.U =/= head

    var rob_input_valid = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))

    for(i <- 0 until base.FETCH_WIDTH){
        for(j <- 0 until bankcap){
            when(io.rat_flush_en){
                ROBBankRegs(i)(j) := 0.U.asTypeOf(new ROBItem)
                ROBIDLocMem(io.rob_item_i(i).id) := 0.U
            }
        }
    }

    for(i <- 0 until base.FETCH_WIDTH){
        rob_input_valid(i) := io.rob_item_i(i).valid
        rob_item_o(i) := ROBBankRegs(i)(head)
    }

    for(i <- 0 until base.ALU_NUM){
        when(io.cdb_i.alu_channel(i).valid & ~io.rob_state & ~io.rat_flush_en){
            ROBBankRegs(io.cdb_i.alu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.alu_channel(i).rob_id)).rdy := true.B
            ROBBankRegs(io.cdb_i.alu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.alu_channel(i).rob_id)).reg_wb_data := io.cdb_i.alu_channel(i).reg_wr_data
            ROBBankRegs(io.cdb_i.alu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.alu_channel(i).rob_id)).targetBrAddr := io.cdb_i.alu_channel(i).branch_target_addr
            ROBBankRegs(io.cdb_i.alu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.alu_channel(i).rob_id)).hasException := io.cdb_i.alu_channel(i).has_exception
            ROBBankRegs(io.cdb_i.alu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.alu_channel(i).rob_id)).ExceptionType := io.cdb_i.alu_channel(i).exception_type
        }
    }
    for(i <- 0 until base.AGU_NUM){
        when(io.cdb_i.agu_channel(i).valid & ~io.rob_state & ~io.rat_flush_en){
            ROBBankRegs(io.cdb_i.agu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.agu_channel(i).rob_id)).rdy := true.B
            ROBBankRegs(io.cdb_i.agu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.agu_channel(i).rob_id)).reg_wb_data := io.cdb_i.agu_channel(i).reg_wr_data
        }
    }

    for(i <- 0 until base.FETCH_WIDTH){
        when(io.robw_able & ~io.rat_flush_en & io.rob_item_i(i).valid){
            ROBBankRegs(i)(tail) := io.rob_item_i(i)
            ROBIDLocMem(io.rob_item_i(i).id) := tail
        }
    }

    /* Retire出现异常，normal->flush */
    /* head + 1.U == tail, flush->normal */
    when(rob_state === normal & io.rat_flush_en){
        next_rob_state := flush
    }.elsewhen(rob_state === flush & head + 1.U === tail){
        next_rob_state := normal
    }.otherwise{
        next_rob_state := rob_state
    }

    rob_state := next_rob_state

    when(io.robw_able & rob_input_valid.asUInt.orR & rob_state === normal & ~io.rat_flush_en){
        tail := tail + 1.U
    }

    when(
        (io.robr_able & io.retire_rdy_mask.andR & rob_state === normal & ~io.rat_flush_en) |
        (io.robw_able & rob_state === flush & head =/= tail)
    ){
        head := head + 1.U
    }

    /* connect */
    io.rob_item_o := rob_item_o
    io.rob_state  := rob_state
}