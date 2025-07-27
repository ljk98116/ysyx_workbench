package cpu.core.frontend

import chisel3._
import chisel3.util._

import cpu.config._
import cpu.core.utils._

/* use stage2 to read/write RAT */
/* deal read result and construct ROB Item */
/* load hold last store idx to judge load forwarding */
class RenameStage2 extends Module
{
    val io = IO(new Bundle{
        val rob_state = Input(UInt(2.W))
        val pc_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val inst_valid_mask_i = Input(UInt(base.FETCH_WIDTH.W))
        val inst_valid_cnt_i = Input(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))
        val DecodeRes_i = Input(Vec(base.FETCH_WIDTH, new DecodeRes))
        /* 分支预测结果 */
        /* 使用全局/局部历史预测 */
        val gbranch_pre_res_i = Input(Vec(base.FETCH_WIDTH, Bool()))
        val lbranch_pre_res_i = Input(Vec(base.FETCH_WIDTH, Bool()))
        /* 分支预测方向 */
        val branch_pre_res_i = Input(Vec(base.FETCH_WIDTH, Bool()))

        /* 当前全局、局部历史PHT索引/BHT索引 */
        val global_pht_idx_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val local_pht_idx_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val bht_idx_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.BHTID_WIDTH.W)))

        val btb_hit_vec_i = Input(Vec(base.FETCH_WIDTH, Bool()))
        val btb_pred_addr_i = Input(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))

        /* RAT读写使能 */
        val rat_wen_i = Input(UInt(base.FETCH_WIDTH.W))
        val rat_waddr_i = Input(Vec(base.FETCH_WIDTH, UInt(base.AREG_WIDTH.W)))
        val rat_wdata_i = Input(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))

        val rat_ren_i = Input(UInt((base.FETCH_WIDTH * 3).W))
        val rat_raddr_i = Input(Vec(base.FETCH_WIDTH * 3, UInt(base.AREG_WIDTH.W)))
        val rat_rdata_i = Input(Vec(base.FETCH_WIDTH * 3, UInt((base.PREG_WIDTH + 1).W)))

        /* RAT读写使能 */
        val rat_wen_o = Output(UInt(base.FETCH_WIDTH.W))
        val rat_waddr_o = Output(Vec(base.FETCH_WIDTH, UInt(base.AREG_WIDTH.W)))
        val rat_wdata_o = Output(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))

        val rat_ren_o = Output(UInt((base.FETCH_WIDTH * 3).W))
        val rat_raddr_o = Output(Vec(base.FETCH_WIDTH * 3, UInt(base.AREG_WIDTH.W)))

        /* RAW相关性信息 */
        val rs1_match = Input(Vec(base.FETCH_WIDTH, UInt(base.FETCH_WIDTH.W)))
        val rs2_match = Input(Vec(base.FETCH_WIDTH, UInt(base.FETCH_WIDTH.W)))

        /* ROB free buffer */
        val rob_freeid_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.ROBID_WIDTH.W)))
        val rob_item_o = Output(Vec(base.FETCH_WIDTH, new ROBItem))
        val inst_valid_cnt_o = Output(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))

        val rs1_match_o = Output(Vec(base.FETCH_WIDTH, UInt(base.FETCH_WIDTH.W)))
        val rs2_match_o = Output(Vec(base.FETCH_WIDTH, UInt(base.FETCH_WIDTH.W)))
    })

    /* pipeline */
    var pc_vec_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.ADDR_WIDTH.W))
    ))

    var inst_valid_mask_reg = RegInit(
        (0.U)(base.FETCH_WIDTH.W)
    )

    var DecodeRes_reg = RegInit(
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new DecodeRes)))
    )

    var rat_wen_reg = RegInit((0.U)(base.FETCH_WIDTH.W))
    var rat_waddr_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.AREG_WIDTH.W))
    ))
    var rat_wdata_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PREG_WIDTH.W))
    ))
    var rat_ren_reg = RegInit((0.U)((base.FETCH_WIDTH * 3).W))
    var rat_raddr_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH * 3)((0.U)(base.AREG_WIDTH.W))
    ))
    var rs1_match_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.FETCH_WIDTH.W))
    ))
    var rs2_match_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.FETCH_WIDTH.W))
    ))

    var inst_valid_cnt_reg = RegInit((0.U)(log2Ceil(base.FETCH_WIDTH + 1).W))
    /* 缓存上一个store指令的ROBID */
    var last_store_idx = RegInit((1 << base.ROBID_WIDTH).U((base.ROBID_WIDTH + 1).W))
    var btb_hit_vec_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))

    var btb_pred_addr_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.ADDR_WIDTH.W))
    ))

    pc_vec_reg := Mux((io.rob_state =/= "b11".U), io.pc_vec_i, pc_vec_reg)
    inst_valid_mask_reg := Mux((io.rob_state =/= "b11".U), io.inst_valid_mask_i, inst_valid_mask_reg)
    DecodeRes_reg := Mux((io.rob_state =/= "b11".U), io.DecodeRes_i, DecodeRes_reg)
    rat_wen_reg := Mux((io.rob_state =/= "b11".U), io.rat_wen_i, rat_ren_reg)
    rat_waddr_reg := Mux((io.rob_state =/= "b11".U), io.rat_waddr_i, rat_waddr_reg)
    rat_wdata_reg := Mux((io.rob_state =/= "b11".U), io.rat_wdata_i, rat_wdata_reg)
    rat_ren_reg := Mux((io.rob_state =/= "b11".U), io.rat_ren_i, rat_ren_reg)
    rat_raddr_reg := Mux((io.rob_state =/= "b11".U), io.rat_raddr_i, rat_raddr_reg)
    rs1_match_reg := Mux((io.rob_state =/= "b11".U), io.rs1_match, rs1_match_reg)
    rs2_match_reg := Mux((io.rob_state =/= "b11".U), io.rs2_match, rs2_match_reg)
    inst_valid_cnt_reg := Mux((io.rob_state =/= "b11".U), io.inst_valid_cnt_i, inst_valid_cnt_reg)
    /* 分支预测结果 */
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
    /* 分支预测结果 */
    gbranch_pre_res_reg := Mux((io.rob_state =/= "b11".U), io.gbranch_pre_res_i, gbranch_pre_res_reg)
    lbranch_pre_res_reg := Mux((io.rob_state =/= "b11".U), io.lbranch_pre_res_i, lbranch_pre_res_reg)
    branch_pre_res_reg := Mux((io.rob_state =/= "b11".U), io.branch_pre_res_i, branch_pre_res_reg)
    global_pht_idx_vec_reg := Mux((io.rob_state =/= "b11".U), io.global_pht_idx_vec_i, global_pht_idx_vec_reg)
    local_pht_idx_vec_reg := Mux((io.rob_state =/= "b11".U), io.local_pht_idx_vec_i, local_pht_idx_vec_reg)
    bht_idx_vec_reg := Mux((io.rob_state =/= "b11".U), io.bht_idx_vec_i, bht_idx_vec_reg)  
    btb_hit_vec_reg := Mux((io.rob_state =/= "b11".U), io.btb_hit_vec_i, btb_hit_vec_reg)

    btb_pred_addr_reg := Mux((io.rob_state =/= "b11".U), io.btb_pred_addr_i, btb_pred_addr_reg)    

    /* wires */
    var rob_item_o = WireInit(
        VecInit(
            Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new ROBItem))
        )
    )
    var rs1_match_id = WireInit(
        VecInit(
            Seq.fill(base.FETCH_WIDTH)((0.U)(log2Ceil(base.FETCH_WIDTH).W))
        )
    )
    var rs2_match_id = WireInit(
        VecInit(
            Seq.fill(base.FETCH_WIDTH)((0.U)(log2Ceil(base.FETCH_WIDTH).W))
        )
    )

    /* 找上一个store指令的robid */
    var store_mask = WireInit((0.U)(base.FETCH_WIDTH.W))
    store_mask := Cat(
        DecodeRes_reg(3).IsStore,
        DecodeRes_reg(2).IsStore,
        DecodeRes_reg(1).IsStore,
        DecodeRes_reg(0).IsStore
    )
    var StoreIdxs = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((base.FETCH_WIDTH.U)((log2Ceil(base.FETCH_WIDTH) + 1).W))
    ))
    for(i <- 0 until base.FETCH_WIDTH)
    {
        var prio_dec = Module(new PriorityDecoder(base.FETCH_WIDTH))
        prio_dec.io.in := store_mask(i, 0)
        StoreIdxs(i) := Mux(store_mask(i, 0).orR & inst_valid_mask_reg(i), prio_dec.io.out, base.FETCH_WIDTH.U)
        rob_item_o(i).pc := pc_vec_reg(i)
        rob_item_o(i).valid := inst_valid_mask_reg(i)
        rob_item_o(i).HasRd := DecodeRes_reg(i).HasRd
        rob_item_o(i).HasRs1 := DecodeRes_reg(i).HasRs1
        rob_item_o(i).HasRs2 := DecodeRes_reg(i).HasRs2
        rob_item_o(i).Imm := DecodeRes_reg(i).Imm
        rob_item_o(i).Opcode := DecodeRes_reg(i).Opcode
        rob_item_o(i).Type := DecodeRes_reg(i).Type
        rob_item_o(i).funct3 := DecodeRes_reg(i).funct3
        rob_item_o(i).funct7 := DecodeRes_reg(i).funct7
        rob_item_o(i).shamt := DecodeRes_reg(i).shamt
        rob_item_o(i).id := io.rob_freeid_vec_i(i)
        rob_item_o(i).pd := Mux(DecodeRes_reg(i).HasRd, rat_wdata_reg(i), 0.U)
        rob_item_o(i).isBranch := DecodeRes_reg(i).IsBranch
        rob_item_o(i).isLoad   := DecodeRes_reg(i).IsLoad
        rob_item_o(i).isStore  := DecodeRes_reg(i).IsStore
        rob_item_o(i).rs1      := DecodeRes_reg(i).rs1
        rob_item_o(i).rs2      := DecodeRes_reg(i).rs2
        rob_item_o(i).rd       := DecodeRes_reg(i).rd
        rob_item_o(i).rdy      := false.B
        rob_item_o(i).rdy1     := DecodeRes_reg(i).rs1 === 0.U
        rob_item_o(i).rdy2     := DecodeRes_reg(i).rs2 === 0.U
        /* 暂时所有分支指令均冲刷流水线 */
        rob_item_o(i).hasException := false.B
        rob_item_o(i).ExceptionType := ExceptionType.NORMAL.U
        rob_item_o(i).storeIdx := Mux(
            inst_valid_mask_reg(i) & (StoreIdxs(i) =/= base.FETCH_WIDTH.U), 
            io.rob_freeid_vec_i(StoreIdxs(i)(log2Ceil(base.FETCH_WIDTH) - 1, 0)), 
            last_store_idx
        )
        rob_item_o(i).gbranch_res := gbranch_pre_res_reg(i)
        rob_item_o(i).lbranch_res := lbranch_pre_res_reg(i)
        rob_item_o(i).branch_res := branch_pre_res_reg(i)
        rob_item_o(i).global_pht_idx := global_pht_idx_vec_reg(i)
        rob_item_o(i).local_pht_idx := local_pht_idx_vec_reg(i)
        rob_item_o(i).bht_idx := bht_idx_vec_reg(i)
        rob_item_o(i).branch_pred_addr := Mux(
            btb_hit_vec_reg(i) & branch_pre_res_reg(i),
            btb_pred_addr_reg(i),
            pc_vec_reg(i) + 4.U
        )
        /* 前置最近指令是否有相同的rd，用前置指令的pd */
        /* 找最近指令的pd */
        var waw_mask = WireInit(VecInit(Seq.fill(base.FETCH_WIDTH)(false.B)))
        var target_idx = WireInit((base.FETCH_WIDTH.U)((log2Ceil(base.FETCH_WIDTH) + 1).W))
        for(j <- 0 until i){
            waw_mask(j) := DecodeRes_reg(i).rd === DecodeRes_reg(j).rd
        }
        val prio_decoder = Module(new cpu.core.utils.PriorityDecoder(4))
        prio_decoder.io.in := waw_mask.asUInt
        target_idx := prio_decoder.io.out
        rob_item_o(i).oldpd := Mux(waw_mask.asUInt.orR, rat_wdata_reg(target_idx(log2Ceil(base.FETCH_WIDTH) - 1, 0)), io.rat_rdata_i(3 * i + 2))
        rob_item_o(i).reg_wb_data := 0.U
        when(DecodeRes_reg(i).HasRs1){
            rob_item_o(i).ps1 := Mux(rs1_match_reg(i)(2), 
                rat_wdata_reg(2),
                Mux(
                    rs1_match_reg(i)(1),
                    rat_wdata_reg(1),
                    Mux(rs1_match_reg(i)(0),
                        rat_wdata_reg(0),
                        io.rat_rdata_i(3 * i)(base.PREG_WIDTH - 1, 0)
                    )
                )
            )
        }
        when(DecodeRes_reg(i).HasRs2){
            rob_item_o(i).ps2 := Mux(rs2_match_reg(i)(2), 
                rat_wdata_reg(2),
                Mux(
                    rs2_match_reg(i)(1),
                    rat_wdata_reg(1),
                    Mux(rs2_match_reg(i)(0),
                        rat_wdata_reg(0),
                        io.rat_rdata_i(3 * i + 1)(base.PREG_WIDTH - 1, 0)
                    )
                )
            )
        }
    }

    /* 获取最后一个有效的store指令ROBID */
    var last_store_idx_mid = WireInit(VecInit(
        Seq.fill(
            log2Ceil(base.FETCH_WIDTH)
        )(
            ((1 << base.ROBID_WIDTH).U)((base.ROBID_WIDTH + 1).W)
        )
    ))
    last_store_idx_mid(0) := Mux(
        rob_item_o(1).storeIdx =/= (1 << base.ROBID_WIDTH).U, 
        rob_item_o(1).storeIdx, 
        rob_item_o(0).storeIdx
    )
    last_store_idx_mid(1) := Mux(
        rob_item_o(3).storeIdx =/= (1 << base.ROBID_WIDTH).U, 
        rob_item_o(3).storeIdx, 
        rob_item_o(2).storeIdx
    )
    last_store_idx := Mux(
        last_store_idx_mid(1) =/= (1 << base.ROBID_WIDTH).U, 
        last_store_idx_mid(1), 
        Mux(last_store_idx_mid(0) =/= (1 << base.ROBID_WIDTH).U, last_store_idx_mid(0), last_store_idx)
    )

    /* connect */
    io.rat_ren_o := rat_ren_reg
    io.rat_raddr_o := rat_raddr_reg
    io.rat_wen_o := Mux(io.rob_state === 0.U, rat_wen_reg, 0.U)
    io.rat_waddr_o := rat_waddr_reg
    io.rat_wdata_o := rat_wdata_reg

    io.rob_item_o := rob_item_o
    io.inst_valid_cnt_o := inst_valid_cnt_reg

    io.rs1_match_o := rs1_match_reg
    io.rs2_match_o := rs2_match_reg
}