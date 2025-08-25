package cpu.core.frontend

import chisel3._
import chisel3.util._
import cpu.config._

class Decode extends Module
{
    val io = IO(new Bundle{
        val rat_flush_en = Input(Bool())
        val rob_state = Input(UInt(2.W))
        val freereg_rd_able = Input(Vec(base.FETCH_WIDTH, Bool()))
        val store_buffer_wr_able = Input(Bool())
        val pc_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val inst_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.DATA_WIDTH.W)))
        val inst_valid_mask_i = Input(UInt(base.FETCH_WIDTH.W))
        val inst_valid_cnt_i = Input(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))


        /* 分支预测结果 */
        /* 使用全局/局部历史预测 */
        val gbranch_pre_res_i = Input(Vec(base.FETCH_WIDTH, Bool()))
        val lbranch_pre_res_i = Input(Vec(base.FETCH_WIDTH, Bool()))
        /* 分支预测方向 */
        val branch_pre_res_i = Input(Vec(base.FETCH_WIDTH, Bool()))
        val btb_hit_vec_i = Input(Vec(base.FETCH_WIDTH, Bool()))
        val btb_pred_addr_i = Input(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val btb_idx_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))

        /* 分支预测使能 */
        val branch_en_pred = Output(Bool())
        val branch_addr_pred = Output(UInt(base.ADDR_WIDTH.W))
        
        /* 当前全局、局部历史PHT索引/BHT索引 */
        val global_pht_idx_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val local_pht_idx_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val bht_idx_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.BHTID_WIDTH.W)))
        val btb_idx_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))

        val pc_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val inst_valid_mask_o = Output(UInt(base.FETCH_WIDTH.W))
        val DecodeRes_o = Output(Vec(base.FETCH_WIDTH, new DecodeRes()))
        val inst_valid_cnt_o = Output(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))

        /* 使用全局/局部历史预测 */
        val gbranch_pre_res_o = Output(Vec(base.FETCH_WIDTH, Bool()))
        val lbranch_pre_res_o = Output(Vec(base.FETCH_WIDTH, Bool()))
        /* 分支预测方向 */
        val branch_pre_res_o = Output(Vec(base.FETCH_WIDTH, Bool()))
        /* PHT索引，BHT索引 */
        val global_pht_idx_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val local_pht_idx_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val bht_idx_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.BHTID_WIDTH.W)))

        val decode_br_mask = Output(Vec(base.FETCH_WIDTH, Bool()))
        val decode_br_addr = Output(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))

        val btb_hit_vec_o = Output(Vec(base.FETCH_WIDTH, Bool()))
        val btb_pred_addr_o = Output(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))    

        /* control */
        val issue_wr_able = Input(Bool())   
        val rob_wr_able = Input(Bool())
        val rob_freeid_rd_able = Input(Vec(base.FETCH_WIDTH, Bool()))
    })

    /* pipeline */
    var stall = WireInit(false.B)
    stall := 
        (io.rob_state === 0.U) & 
        io.freereg_rd_able.asUInt.andR & 
        io.store_buffer_wr_able &
        io.issue_wr_able &
        io.rob_wr_able &
        io.rob_freeid_rd_able.asUInt.andR

    var pc_vec_reg = RegInit(VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.ADDR_WIDTH.W))))
    var inst_valid_mask_reg = RegInit((0.U)(base.FETCH_WIDTH.W))
    var inst_valid_cnt_reg = RegInit((0.U)(log2Ceil(base.FETCH_WIDTH + 1).W))
    var gbranch_pre_res_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var lbranch_pre_res_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var branch_pre_res_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))    
    var global_pht_idx_vec_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PHTID_WIDTH.W))
    ))
    var local_pht_idx_vec_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PHTID_WIDTH.W))
    ))
    var bht_idx_vec_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.BHTID_WIDTH.W))
    ))
    var btb_hit_vec_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var btb_pred_addr_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.ADDR_WIDTH.W))
    ))

    pc_vec_reg := Mux(
        ~io.rat_flush_en, 
        Mux(stall, io.pc_vec_i, pc_vec_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.ADDR_WIDTH.W)))
    )
    inst_valid_mask_reg := Mux(
        ~io.rat_flush_en, 
        Mux(stall, io.inst_valid_mask_i, inst_valid_mask_reg), 
        0.U
    )
    inst_valid_cnt_reg := Mux(
        ~io.rat_flush_en, 
        Mux(stall, io.inst_valid_cnt_i, inst_valid_cnt_reg), 
        0.U
    )
    gbranch_pre_res_reg := Mux(
        ~io.rat_flush_en,
        Mux(stall, io.gbranch_pre_res_i, gbranch_pre_res_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)(false.B))
    )
    lbranch_pre_res_reg := Mux(
        ~io.rat_flush_en,
        Mux(stall, io.lbranch_pre_res_i, lbranch_pre_res_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)(false.B))
    )
    branch_pre_res_reg := Mux(
        ~io.rat_flush_en,
        Mux(stall, io.branch_pre_res_i, branch_pre_res_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)(false.B))
    )
    global_pht_idx_vec_reg := Mux(
        ~io.rat_flush_en,
        Mux(stall, io.global_pht_idx_vec_i, global_pht_idx_vec_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.PHTID_WIDTH.W)))
    )
    local_pht_idx_vec_reg := Mux(
        ~io.rat_flush_en,
        Mux(stall, io.local_pht_idx_vec_i, local_pht_idx_vec_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.PHTID_WIDTH.W)))
    )
    bht_idx_vec_reg := Mux(
        ~io.rat_flush_en,
        Mux(stall, io.bht_idx_vec_i, bht_idx_vec_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.BHTID_WIDTH.W)))
    )

    btb_hit_vec_reg := Mux(
        ~io.rat_flush_en,
        Mux(stall, io.btb_hit_vec_i, btb_hit_vec_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)(false.B))
    )

    btb_pred_addr_reg := Mux(
        ~io.rat_flush_en,
        Mux(stall, io.btb_pred_addr_i, btb_pred_addr_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.ADDR_WIDTH.W)))
    )

    var btb_idx_vec_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PHTID_WIDTH.W))
    ))
    btb_idx_vec_reg := Mux(
        ~io.rat_flush_en, 
        Mux(stall, io.btb_idx_vec_i, btb_idx_vec_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.PHTID_WIDTH.W)))        
    )
    
    io.pc_vec_o := pc_vec_reg
    io.inst_valid_mask_o := inst_valid_mask_reg
    io.inst_valid_cnt_o := inst_valid_cnt_reg
    io.gbranch_pre_res_o := gbranch_pre_res_reg
    io.lbranch_pre_res_o := lbranch_pre_res_reg
    io.branch_pre_res_o := branch_pre_res_reg
    io.global_pht_idx_vec_o := global_pht_idx_vec_reg
    io.local_pht_idx_vec_o := local_pht_idx_vec_reg
    io.bht_idx_vec_o := bht_idx_vec_reg
    io.btb_hit_vec_o := btb_hit_vec_reg
    io.btb_pred_addr_o := btb_pred_addr_reg
    io.btb_idx_vec_o := btb_idx_vec_reg

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
        ~(stall) & ~inst_state,  
        io.inst_vec_i,
        inst_vec_stall_reg
    )
    /* 收到暂停信号，变化状态 */
    inst_state := ~(stall)
    /* 处于暂停状态,使用锁存的值,否则使用输入值 */
    inst_vec_used := Mux(inst_state, inst_vec_stall_reg, io.inst_vec_i)

    var decode_br_mask = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var decode_br_addr = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.ADDR_WIDTH.W))
    ))
    /* 根据opcode译码 */
    for(i <- 0 until base.FETCH_WIDTH){
        decode_br_mask(i) := false.B
        decode_br_addr(i) := 0.U
        when(inst_valid_mask_reg(i) & ~io.rat_flush_en){
            switch(inst_vec_used(i)(6, 0)){
                /* type I */
                is(
                    Opcode.ADDI,
                    // Opcode.SLLI
                    // Opcode.SRLI
                    // Opcode.SRAI
                    // Opcode.SLTIU
                    // Opcode.XORI
                    // Opcode.ORI
                    // Opcode.ANDI
                    Opcode.LW,
                    // Opcode.LB
                    // Opcode.LBU
                    // Opcode.LH
                    // Opcode.LHU
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
                    decode_br_mask(i)   := true.B
                    decode_br_addr(i)   := pc_vec_reg(i) + Imm.ImmUJ(inst_vec_used(i))
                }
                /* type S */
                is(
                    Opcode.SW,
                    // Opcode.SB,
                    // Opcode.SH,
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
                    //Opcode.SLL
                    //Opcode.SLT
                    //Opcode.SLTU
                    //Opcode.XOR
                    //Opcode.OR
                    //Opcode.SRL
                    //Opcode.SRA
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
                    // Opcode.BNE,
                    // Opcode.BLT,
                    // Opcode.BGE
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
    io.decode_br_mask := decode_br_mask
    io.decode_br_addr := decode_br_addr
}