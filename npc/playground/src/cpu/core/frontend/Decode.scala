package cpu.core.frontend

import chisel3._
import chisel3.util._
import cpu.config._

class Decode extends Module
{
    val io = IO(new Bundle{
        val rat_flush_en = Input(Bool())
        val rob_state = Input(Bool())
        val freereg_rd_able = Input(Vec(base.FETCH_WIDTH, Bool()))
        val pc_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val inst_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.DATA_WIDTH.W)))
        val inst_valid_mask_i = Input(UInt(base.FETCH_WIDTH.W))
        val inst_valid_cnt_i = Input(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))

        val pc_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val inst_valid_mask_o = Output(UInt(base.FETCH_WIDTH.W))
        val DecodeRes_o = Output(Vec(base.FETCH_WIDTH, new DecodeRes()))
        val inst_valid_cnt_o = Output(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))
    })

    /* pipeline */
    var pc_vec_reg = RegInit(VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.ADDR_WIDTH.W))))
    var inst_valid_mask_reg = RegInit((0.U)(base.FETCH_WIDTH.W))
    var inst_valid_cnt_reg = RegInit((0.U)(log2Ceil(base.FETCH_WIDTH + 1).W))

    pc_vec_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state & io.freereg_rd_able.asUInt.andR, io.pc_vec_i, pc_vec_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.ADDR_WIDTH.W)))
    )
    inst_valid_mask_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state & io.freereg_rd_able.asUInt.andR, io.inst_valid_mask_i, inst_valid_mask_reg), 
        0.U
    )
    inst_valid_cnt_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state & io.freereg_rd_able.asUInt.andR, io.inst_valid_cnt_i, inst_valid_cnt_reg), 
        0.U
    )

    io.pc_vec_o := pc_vec_reg
    io.inst_valid_mask_o := inst_valid_mask_reg
    io.inst_valid_cnt_o := inst_valid_cnt_reg

    /* Decode */
    var decoderes = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(0.U.asTypeOf(new DecodeRes))
    ))

    /* 暂停处理状态机 */
    /* 暂停状态使用暂存的指令 */
    var inst_state = RegInit(false.B)
    var inst_vec_stall_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.DATA_WIDTH.W))
    ))
    var inst_vec_used = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.DATA_WIDTH.W))
    ))
    /* 收到暂停信号的那一刻更新 */
    inst_vec_stall_reg := Mux(
        ~(~io.rob_state & io.freereg_rd_able.asUInt.andR) & ~inst_state,  
        io.inst_vec_i,
        inst_vec_stall_reg
    )
    /* 收到暂停信号，变化状态 */
    inst_state := ~(~io.rob_state & io.freereg_rd_able.asUInt.andR)
    /* 处于暂停状态,使用锁存的值,否则使用输入值 */
    inst_vec_used := Mux(inst_state, inst_vec_stall_reg, io.inst_vec_i)

    /* 根据opcode译码 */
    for(i <- 0 until base.FETCH_WIDTH){
        when(inst_valid_mask_reg(i) & ~io.rat_flush_en){
            switch(inst_vec_used(i)(6, 0)){
                /* type I */
                is(
                    Opcode.ADDI,
                    //Opcode.SLLI
                    //Opcode.SRLI
                    //Opcode.SRAI
                    //Opcode.SLTIU
                    Opcode.LW
                ){
                    decoderes(i).Imm    := Imm.ImmI(inst_vec_used(i))
                    decoderes(i).Opcode := inst_vec_used(i)(6, 0)
                    decoderes(i).rd     := inst_vec_used(i)(11, 7)
                    decoderes(i).rs1    := inst_vec_used(i)(19, 15)
                    decoderes(i).funct3 := inst_vec_used(i)(14, 12)
                    decoderes(i).Type   := InstType.TYPEI
                    decoderes(i).HasRs1 := true.B
                    decoderes(i).HasRs2 := false.B
                    decoderes(i).HasRd  := true.B
                    decoderes(i).IsLoad := inst_vec_used(i)(6, 0) === Opcode.LW
                }
                is(
                    Opcode.JALR
                ){
                    decoderes(i).Imm    := Imm.ImmI(inst_vec_used(i))
                    decoderes(i).Opcode := inst_vec_used(i)(6, 0)
                    decoderes(i).rd     := inst_vec_used(i)(11, 7)
                    decoderes(i).rs1    := inst_vec_used(i)(19, 15)
                    decoderes(i).funct3 := inst_vec_used(i)(14, 12)
                    decoderes(i).Type   := InstType.TYPEI
                    decoderes(i).IsBranch := true.B
                    decoderes(i).HasRs1 := true.B
                    decoderes(i).HasRs2 := false.B
                    decoderes(i).HasRd  := true.B                   
                }
                /* type U */
                is(
                    Opcode.AUIPC,
                    Opcode.LUI
                ){
                    decoderes(i).Imm    := Imm.ImmU(inst_vec_used(i))
                    decoderes(i).Opcode := inst_vec_used(i)(6, 0)
                    decoderes(i).rd     := inst_vec_used(i)(11, 7)
                    decoderes(i).Type   := InstType.TYPEU
                    decoderes(i).HasRs1 := false.B
                    decoderes(i).HasRs2 := false.B
                    decoderes(i).HasRd  := true.B
                }
                /* type UJ */
                is(
                    Opcode.JAL
                ){
                    decoderes(i).Imm    := Imm.ImmUJ(inst_vec_used(i))
                    decoderes(i).Opcode := inst_vec_used(i)(6, 0)
                    decoderes(i).rd     := inst_vec_used(i)(11, 7)
                    decoderes(i).Type   := InstType.TYPEUJ
                    decoderes(i).IsBranch := true.B
                    decoderes(i).HasRs1 := false.B
                    decoderes(i).HasRs2 := false.B
                    decoderes(i).HasRd  := true.B
                }
                /* type S */
                is(
                    Opcode.SW
                ){
                    decoderes(i).Imm    := Imm.ImmS(inst_vec_used(i))
                    decoderes(i).Opcode := inst_vec_used(i)(6, 0)
                    decoderes(i).rs1    := inst_vec_used(i)(19, 15)
                    decoderes(i).rs2    := inst_vec_used(i)(24, 20)
                    decoderes(i).funct3 := inst_vec_used(i)(14, 12)    
                    decoderes(i).Type   := InstType.TYPES        
                    decoderes(i).IsStore := true.B 
                    decoderes(i).HasRs1 := true.B
                    decoderes(i).HasRs2 := true.B
                    decoderes(i).HasRd  := false.B
                }
                /* type R */
                is(
                    Opcode.ADD
                    //Opcode.SUB
                ){
                    decoderes(i).Opcode := inst_vec_used(i)(6, 0)
                    decoderes(i).rs1    := inst_vec_used(i)(19, 15)
                    decoderes(i).rs2    := inst_vec_used(i)(24, 20)
                    decoderes(i).rd     := inst_vec_used(i)(11, 7)
                    decoderes(i).funct3 := inst_vec_used(i)(14, 12)
                    decoderes(i).funct7 := inst_vec_used(i)(31, 25)    
                    decoderes(i).Type   := InstType.TYPER        
                    decoderes(i).HasRs1 := true.B
                    decoderes(i).HasRs2 := true.B
                    decoderes(i).HasRd  := true.B                    
                }
                /* type SB */
                is(
                    Opcode.BEQ,
                    // Opcode.BNE
                ){
                    decoderes(i).Imm    := Imm.ImmSB(inst_vec_used(i))
                    decoderes(i).Opcode := inst_vec_used(i)(6, 0)
                    decoderes(i).rs1    := inst_vec_used(i)(19, 15)
                    decoderes(i).rs2    := inst_vec_used(i)(24, 20)
                    decoderes(i).Type   := InstType.TYPESB
                    decoderes(i).funct3 := inst_vec_used(i)(14, 12)
                    decoderes(i).IsBranch := true.B
                    decoderes(i).HasRs1 := true.B
                    decoderes(i).HasRs2 := true.B
                    decoderes(i).HasRd  := false.B                    
                }
            }
        }
    }

    io.DecodeRes_o := decoderes
}