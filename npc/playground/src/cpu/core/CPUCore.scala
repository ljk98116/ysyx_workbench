package cpu.core

import chisel3._
import chisel3.util._

import cpu.config._
import cpu.core.frontend._
import cpu.core.backend._
import cpu.memory.MultiPortSram
import chisel3.experimental.ChiselAnnotation
// recursion, quick-sort, min3
/* PC -> Fetch -> Decode -> Rename1 -> Rename2 -> Dispatch -> */
/* Issue -> Ex -> Mem1 -> Mem2 -> Mem3 -> Retire */
class CPUCore(memfile: String) extends Module
{

    val io = IO(new Bundle{
    })

    /* memory */
    val memory = Module(new MultiPortSram(base.FETCH_WIDTH + base.AGU_NUM, memfile, true))

    /* PC Reg */
    val pc_reg = Module(new PCReg)

    /* fetch stage */
    val fetch = Module(new Fetch)

    /* PHT */
    val pht = Module(new PHTReg(true))

    /* BTB */
    val btb = Module(new BTB(true))

    /* decode stage */
    val decode = Module(new Decode)

    /* free reg buffers */
    /* 4发射，每个指令单独持有一个freereg队列，保证ID不重复, 简化设计 */
    val freeregbuf_seq = Seq.tabulate(base.FETCH_WIDTH)(
        (i) => Module(new FreeRegBuffer(i))
    )

    /* rename stage1 */
    val rename1 = Module(new RenameStage1)

    /* rename stage2 */
    val rename2 = Module(new RenameStage2)

    /* rename RAT */
    val ReNameRAT = Module(new RenameRAT)

    /* ROB ID Buffers */
    val robidbuf_seq = Seq.tabulate(base.FETCH_WIDTH)(
        (i) => Module(new ROBIDBuffer(i))
    )

    /* ROB Buffer */
    var rob_buffer = Module(new ROB)

    /* dispatch stage */
    var dispatch = Module(new Dispatch)

    /* CDB */
    var cdb = Wire(new CDB)

    /* issue stage */
    var issue = Module(new IssueStage)

    /* regread stage */
    // var regread = Module(new RegReadStage)

    /* exec stage */
    var alu_vec = Seq.fill(base.ALU_NUM)(
        Module(new ALU)
    )
    var agu_vec = Seq.fill(base.AGU_NUM)(
        Module(new AGU)
    )

    /* Memstage1 */
    var memstage1 = Module(new MemStage1)

    /* MemStage2 */
    var memstage2 = Module(new MemStage2)

    /* MemStage3 */
    var memstage3 = Module(new MemStage3)

    /* storebuffer */
    var storebuffer = Module(new StoreBuffer(base.STORE_BUF_SZ))

    /* retire stage */
    var retire = Module(new RetireStage)

    /* PRF */
    var prf = Module(new PRF)

    /* retire RAT */
    var retireRAT = Module(new RetireRAT)

    /* connection */
    /* pc -> fetch */
    fetch.io.pc_i                   := pc_reg.io.pc_o
    fetch.io.inst_valid_mask_i      := pc_reg.io.inst_valid_mask_o
    fetch.io.inst_valid_cnt_i       := pc_reg.io.inst_valid_cnt_o
    fetch.io.global_pht_idx_vec_i   := pc_reg.io.global_pht_idx_vec_o
    fetch.io.local_pht_idx_vec_i    := pc_reg.io.local_pht_idx_vec_o
    fetch.io.bht_idx_vec_i          := pc_reg.io.bht_idx_vec_o

    /* fetch -> pc */
    pc_reg.io.branch_pred_en        := fetch.io.branch_en_pred
    pc_reg.io.branch_pred_addr      := fetch.io.branch_addr_pred

    /* fetch -> PHT */
    pht.io.global_pht_idx_vec_i     := fetch.io.global_pht_idx_vec_o
    pht.io.local_pht_idx_vec_i      := fetch.io.local_pht_idx_vec_o

    /* PHT -> fetch */
    fetch.io.branch_pre_res_i       := pht.io.branch_pre_res_o

    /* fetch -> BTB */
    btb.io.pc_i                     := fetch.io.pc_vec_o

    /* BTB -> fetch */
    fetch.io.btb_hit_vec_i          := btb.io.btb_hit_vec_o
    fetch.io.btb_pred_addr_i        := btb.io.btb_pred_addr_o

    /* fetch -> memory */
    for(i <- 0 until base.FETCH_WIDTH){
        memory.io.ren(i)            := fetch.io.inst_valid_mask_o(i)
        memory.io.raddr(i)          := fetch.io.pc_vec_o(i)
    }
    
    /* fetch -> decode */
    decode.io.pc_vec_i              := fetch.io.pc_vec_o
    decode.io.inst_valid_mask_i     := fetch.io.inst_valid_mask_o
    decode.io.inst_valid_cnt_i      := fetch.io.inst_valid_cnt_o

    decode.io.bht_idx_vec_i         := fetch.io.bht_idx_vec_o
    decode.io.global_pht_idx_vec_i  := fetch.io.global_pht_idx_vec_o
    decode.io.local_pht_idx_vec_i   := fetch.io.local_pht_idx_vec_o
    decode.io.gbranch_pre_res_i     := pht.io.gbranch_pre_res_o
    decode.io.lbranch_pre_res_i     := pht.io.lbranch_pre_res_o
    decode.io.branch_pre_res_i      := pht.io.branch_pre_res_o

    /* decode -> btb */
    btb.io.decode_br_mask_i         := decode.io.decode_br_mask
    btb.io.decode_pc_i              := decode.io.pc_vec_o
    btb.io.decode_br_addr           := decode.io.decode_br_addr

    /* btb -> decode */
    decode.io.btb_hit_vec_i         := btb.io.btb_hit_vec_o
    decode.io.btb_pred_addr_i       := btb.io.btb_pred_addr_o

    /* memory -> decode */
    for(i <- 0 until base.FETCH_WIDTH){
        decode.io.inst_vec_i(i)     := memory.io.rdata(i)
    }

    /* rename1 -> robidbuffer */
    for(i <- 0 until base.FETCH_WIDTH){
        robidbuf_seq(i).io.rat_write_en_rename := rename2.io.rob_item_o(i).valid
    }

    /* freeregbuffer -> renamestage1 */
    for(i <- 0 until base.FETCH_WIDTH){
        rename1.io.freereg_vec_i(i) := freeregbuf_seq(i).io.freereg_o
        rename1.io.freereg_rd_able(i) := freeregbuf_seq(i).io.rd_able
        decode.io.freereg_rd_able(i)  := freeregbuf_seq(i).io.rd_able
        fetch.io.freereg_rd_able(i) := freeregbuf_seq(i).io.rd_able
        pc_reg.io.freereg_rd_able(i) := freeregbuf_seq(i).io.rd_able
    }

    /* rename1 -> freeregbuffer */
    for(i <- 0 until base.FETCH_WIDTH){
        freeregbuf_seq(i).io.rat_write_en_rename := rename1.io.DecodeRes_o(i).HasRd
    }

    /* decode -> renamestage1 */
    rename1.io.pc_vec_i             := decode.io.pc_vec_o
    rename1.io.inst_valid_mask_i    := decode.io.inst_valid_mask_o
    rename1.io.DecodeRes_i          := decode.io.DecodeRes_o
    rename1.io.inst_valid_cnt_i     := decode.io.inst_valid_cnt_o

    rename1.io.bht_idx_vec_i        := decode.io.bht_idx_vec_o
    rename1.io.global_pht_idx_vec_i := decode.io.global_pht_idx_vec_o
    rename1.io.local_pht_idx_vec_i  := decode.io.local_pht_idx_vec_o
    rename1.io.gbranch_pre_res_i    := decode.io.gbranch_pre_res_o
    rename1.io.lbranch_pre_res_i    := decode.io.lbranch_pre_res_o
    rename1.io.branch_pre_res_i     := decode.io.branch_pre_res_o
    rename1.io.btb_hit_vec_i        := decode.io.btb_hit_vec_o
    rename1.io.btb_pred_addr_i      := decode.io.btb_pred_addr_o

    /* robidbuffer -> rename2 */
    for(i <- 0 until base.FETCH_WIDTH){
        rename2.io.rob_freeid_vec_i(i)  := robidbuf_seq(i).io.free_robid_o
    }
    
    /* rename1 -> rename2 */
    rename2.io.pc_vec_i             := rename1.io.pc_vec_o
    rename2.io.inst_valid_mask_i    := rename1.io.inst_valid_mask_o
    rename2.io.DecodeRes_i          := rename1.io.DecodeRes_o
    rename2.io.inst_valid_cnt_i     := rename1.io.inst_valid_cnt_o

    rename2.io.rat_ren_i            := rename1.io.rat_ren_o
    rename2.io.rat_raddr_i          := rename1.io.rat_raddr_o
    rename2.io.rat_wen_i            := rename1.io.rat_wen_o
    rename2.io.rat_waddr_i          := rename1.io.rat_waddr_o
    rename2.io.rat_wdata_i          := rename1.io.rat_wdata_o
    rename2.io.rs1_match            := rename1.io.rs1_match
    rename2.io.rs2_match            := rename1.io.rs2_match
    rename2.io.prf_valid_vec        := prf.io.prf_valid_vec

    rename2.io.bht_idx_vec_i        := rename1.io.bht_idx_vec_o
    rename2.io.global_pht_idx_vec_i := rename1.io.global_pht_idx_vec_o
    rename2.io.local_pht_idx_vec_i  := rename1.io.local_pht_idx_vec_o
    rename2.io.gbranch_pre_res_i    := rename1.io.gbranch_pre_res_o
    rename2.io.lbranch_pre_res_i    := rename1.io.lbranch_pre_res_o
    rename2.io.branch_pre_res_i     := rename1.io.branch_pre_res_o
    rename2.io.btb_hit_vec_i        := rename1.io.btb_hit_vec_o
    rename2.io.btb_pred_addr_i      := rename1.io.btb_pred_addr_o

    /* rename2 -> RenameRAT */
    ReNameRAT.io.rat_ren            := rename2.io.rat_ren_o
    ReNameRAT.io.rat_raddr          := rename2.io.rat_raddr_o
    ReNameRAT.io.rat_wen            := rename2.io.rat_wen_o
    ReNameRAT.io.rat_waddr          := rename2.io.rat_waddr_o
    ReNameRAT.io.rat_wdata          := rename2.io.rat_wdata_o
    
    /* ReNameRAT -> rename2 */
    rename2.io.rat_rdata_i          := ReNameRAT.io.rat_rdata

    /* rename2 -> dispatch */
    dispatch.io.rob_item_i          := rename2.io.rob_item_o
    // dispatch.io.rs1_match           := rename2.io.rs1_match_o
    // dispatch.io.rs2_match           := rename2.io.rs2_match_o

    dontTouch(dispatch.io.rob_item_i)
    dontTouch(dispatch.io.rob_item_o)

    dispatch.io.inst_valid_cnt_i    := rename2.io.inst_valid_cnt_o
    // dispatch.io.cdb_i               := cdb

    /* prf -> dispatch */
    // dispatch.io.prf_valid_rs1_rdata := prf.io.prf_valid_rs1_rdata
    // dispatch.io.prf_valid_rs2_rdata := prf.io.prf_valid_rs2_rdata

    /* dispatch -> ROB */
    rob_buffer.io.rob_item_i        := dispatch.io.rob_item_o
    dontTouch(rob_buffer.io.rob_item_i)

    /* dispatch -> prf */
    // prf.io.prf_valid_rs1_ren        := dispatch.io.prf_valid_rs1_ren
    // prf.io.prf_valid_rs1_raddr      := dispatch.io.prf_valid_rs1_raddr
    // prf.io.prf_valid_rs2_ren        := dispatch.io.prf_valid_rs2_ren
    // prf.io.prf_valid_rs2_raddr      := dispatch.io.prf_valid_rs2_raddr
    prf.io.prf_valid_rd_wen         := rename2.io.prf_valid_rd_wen
    prf.io.prf_valid_rd_waddr       := rename2.io.prf_valid_rd_waddr
    prf.io.prf_valid_rd_wdata       := rename2.io.prf_valid_rd_wdata

    /* dispatch -> IssueStage */
    // dontTouch(dispatch.io.alu_items_vec_o)
    // dontTouch(dispatch.io.agu_items_vec_o)
    issue.io.alu_items_vec_i        := dispatch.io.alu_items_vec_o
    issue.io.agu_items_vec_i        := dispatch.io.agu_items_vec_o
    issue.io.agu_items_cnt_i    := dispatch.io.agu_items_cnt_o

    /* dispatch -> StoreBuffer */
    storebuffer.io.store_buffer_write_en := dispatch.io.store_buffer_write_en
    storebuffer.io.store_buffer_item_i   := dispatch.io.store_buffer_item_o
    storebuffer.io.store_buffer_write_cnt := dispatch.io.store_buffer_write_cnt
    
    // issue.io.cdb_i                  := cdb

    /* IssueStage -> RegReadStage */
    // regread.io.alu_fu_items_i       := issue.io.alu_fu_items_o
    // regread.io.agu_fu_items_i       := issue.io.agu_fu_items_o

    /* IssueStage -> PRF */
    prf.io.prf_rs1_data_ren         := issue.io.prf_rs1_data_ren
    prf.io.prf_rs1_data_raddr       := issue.io.prf_rs1_data_raddr
    prf.io.prf_rs2_data_ren         := issue.io.prf_rs2_data_ren
    prf.io.prf_rs2_data_raddr       := issue.io.prf_rs2_data_raddr

    /* IssueStage -> 前面 */
    pc_reg.io.issue_wr_able         := issue.io.wr_able
    fetch.io.issue_wr_able         := issue.io.wr_able
    decode.io.issue_wr_able         := issue.io.wr_able
    rename1.io.issue_wr_able         := issue.io.wr_able
    rename2.io.issue_wr_able         := issue.io.wr_able
    dispatch.io.issue_wr_able         := issue.io.wr_able

    /* PRF -> IssueStage */
    issue.io.prf_rs1_data_rdata   := prf.io.prf_rs1_data_rdata
    issue.io.prf_rs2_data_rdata   := prf.io.prf_rs2_data_rdata
    issue.io.prf_valid_vec := prf.io.prf_valid_vec

    /* RegRead -> FU */
    for(i <- 0 until base.ALU_NUM){
        alu_vec(i).io.rob_item_i    := issue.io.alu_fu_items_o(i)
        alu_vec(i).io.rs1_data_i    := issue.io.alu_channel_rs1_rdata(i)
        alu_vec(i).io.rs2_data_i    := issue.io.alu_channel_rs2_rdata(i)
    }

    for(i <- 0 until base.AGU_NUM){
        agu_vec(i).io.rob_item_i    := issue.io.agu_fu_items_o(i)
        agu_vec(i).io.rs1_data_i    := issue.io.agu_channel_rs1_rdata(i)
        agu_vec(i).io.rs2_data_i    := issue.io.agu_channel_rs2_rdata(i)
    }

    /* FU -> CDB */
    for(i <- 0 until base.ALU_NUM){
        cdb.alu_channel(i).rob_id       := alu_vec(i).io.rob_id_o
        cdb.alu_channel(i).valid        := alu_vec(i).io.valid_o
        cdb.alu_channel(i).arch_reg_id  := alu_vec(i).io.areg_wr_addr
        cdb.alu_channel(i).phy_reg_id   := alu_vec(i).io.preg_wr_addr
        cdb.alu_channel(i).reg_wr_data  := alu_vec(i).io.result
        cdb.alu_channel(i).branch_target_addr := alu_vec(i).io.branch_target_addr
        cdb.alu_channel(i).has_exception := alu_vec(i).io.has_exception
        cdb.alu_channel(i).exception_type := alu_vec(i).io.exception_type
    }

    /* ALU -> btb */
    for(i <- 0 until base.ALU_NUM){
        btb.io.ex_br_mask_i(i) := alu_vec(i).io.branch_en
        btb.io.ex_pc_i(i) := alu_vec(i).io.pc_o
        btb.io.ex_br_addr(i) := alu_vec(i).io.branch_target_addr
    }

    /* CDB -> PRF */
    prf.io.cdb_i                        := cdb

    /* AGU -> MemStage1 */
    for(i <- 0 until base.AGU_NUM){
        memstage1.io.rob_item_i(i)      := agu_vec(i).io.rob_item_o
        memstage1.io.agu_result_i(i)    := agu_vec(i).io.result
        memstage1.io.agu_rw_mask_i(i)   := agu_vec(i).io.mem_rw_mask
        memstage1.io.agu_mem_wdata(i)   := agu_vec(i).io.mem_wr_data
        memstage1.io.agu_valid_i(i)     := agu_vec(i).io.valid
    }
    
    /* AGU -> StoreBuffer */
    for(i <- 0 until base.AGU_NUM){
        storebuffer.io.agu_valid(i) := agu_vec(i).io.valid
        storebuffer.io.agu_rob_id(i) := agu_vec(i).io.rob_item_o.id
        storebuffer.io.agu_result(i) := agu_vec(i).io.result
        storebuffer.io.agu_wdata(i) := agu_vec(i).io.mem_wr_data
        storebuffer.io.agu_wmask(i) := agu_vec(i).io.mem_rw_mask
    }

    /* AGU -> ROB */
    for(i <- 0 until base.AGU_NUM){
        rob_buffer.io.agu_valid(i) := agu_vec(i).io.valid
        rob_buffer.io.agu_rob_id(i) := agu_vec(i).io.rob_item_o.id
        rob_buffer.io.agu_result(i) := agu_vec(i).io.result
        rob_buffer.io.agu_wdata(i) := agu_vec(i).io.mem_wr_data
        rob_buffer.io.agu_wmask(i) := agu_vec(i).io.mem_rw_mask  
        rob_buffer.io.agu_ls_flag(i) := agu_vec(i).io.rob_item_o.isStore      
    }

    /* StoreBuffer -> MemStage1 */
    memstage1.io.storebuffer_head_item_i := storebuffer.io.store_buffer_item_o

    /* MemStage1 -> MemStage2 */
    memstage2.io.rob_item_i             := memstage1.io.rob_item_o
    memstage2.io.mem_read_en_i          := memstage1.io.mem_read_en_o
    memstage2.io.mem_read_addr_i        := memstage1.io.mem_read_addr_o
    memstage2.io.mem_read_mask_i        := memstage1.io.mem_read_mask_o
    memstage2.io.mem_write_en_i         := memstage1.io.mem_write_en_o
    memstage2.io.mem_write_addr_i       := memstage1.io.mem_write_addr_o
    memstage2.io.mem_write_mask_i       := memstage1.io.mem_write_wmask_o
    memstage2.io.mem_write_data_i       := memstage1.io.mem_write_data_o
    memstage2.io.storebuffer_ren_i      := memstage1.io.storebuffer_ren_o
    memstage2.io.storebuffer_raddr_i    := memstage1.io.storebuffer_raddr_o
    memstage2.io.storebuffer_rmask_i    := memstage1.io.storebuffer_rmask_o

    /* Memstage2 -> StoreBuffer, load forwarding/real steping */
    storebuffer.io.store_buffer_ren     := memstage2.io.storebuffer_ren_o
    storebuffer.io.store_buffer_raddr   := memstage2.io.storebuffer_raddr_o
    storebuffer.io.store_buffer_rmask   := memstage2.io.storebuffer_rmask_o
    storebuffer.io.mem_write_en         := memstage2.io.mem_write_en_o
    storebuffer.io.store_ids            := memstage2.io.store_ids_o

    /* MemStage2 -> Sram */
    memory.io.wen                       := memstage2.io.mem_write_en_o
    memory.io.waddr                     := memstage2.io.mem_write_addr_o
    memory.io.wmask                     := memstage2.io.mem_write_wmask_o
    memory.io.wdata                     := memstage2.io.mem_write_data_o
    for(i <- 0 until base.AGU_NUM){
        memory.io.ren(base.FETCH_WIDTH + i)     := memstage2.io.mem_read_en_o(i)
        memory.io.raddr(base.FETCH_WIDTH + i)   := memstage2.io.mem_read_addr_o(i)
    }

    /* MemStage2 -> MemStage3 */
    memstage3.io.rob_item_i             := memstage2.io.rob_item_o
    memstage3.io.mem_read_en_i          := memstage2.io.mem_read_en_o
    memstage3.io.mem_read_mask_i        := memstage2.io.mem_read_mask_o

    /* memory -> memstage3 */
    for(i <- 0 until base.AGU_NUM){
        memstage3.io.mem_read_data_i(i)        := memory.io.rdata(base.FETCH_WIDTH + i)
    }

    /* storebuffer -> memstage3 */
    memstage3.io.storebuffer_rdata      := storebuffer.io.store_buffer_rdata
    memstage3.io.storebuffer_rdata_valid := storebuffer.io.store_buffer_rdata_valid

    /* MemStage3 -> CDB */
    for(i <- 0 until base.AGU_NUM){
        cdb.agu_channel(i).arch_reg_id  := memstage3.io.rob_item_o(i).rd
        cdb.agu_channel(i).phy_reg_id   := memstage3.io.rob_item_o(i).pd
        cdb.agu_channel(i).valid        := memstage3.io.rob_item_o(i).valid
        cdb.agu_channel(i).rob_id       := memstage3.io.rob_item_o(i).id
        cdb.agu_channel(i).reg_wr_data  := memstage3.io.mem_read_data_o(i)
    }

    /* CDB -> ROB */
    rob_buffer.io.cdb_i                 := cdb

    /* ROB -> retire */
    dontTouch(rob_buffer.io.rob_item_o)
    dontTouch(retire.io.rob_items_i)
    retire.io.rob_items_i               := rob_buffer.io.rob_item_o

    /* retire -> ROB */
    rob_buffer.io.retire_rdy_mask       := retire.io.rob_item_rdy_mask
    rob_buffer.io.rat_flush_en          := retire.io.rat_flush_en

    /* retire ->storebuffer */
    storebuffer.io.commit_valid_mask   := retire.io.commit_valid_mask

    /* retire -> retireRAT */
    retireRAT.io.rat_wen                := retire.io.rat_write_en.asUInt
    retireRAT.io.rat_waddr              := retire.io.rat_write_addr
    retireRAT.io.rat_wdata              := retire.io.rat_write_data
    retireRAT.io.exception_mask_front_i := retire.io.exception_mask_front
    
    /* retireRAT/retire -> renameRAT */
    ReNameRAT.io.rat_flush_en           := retire.io.rat_flush_en
    ReNameRAT.io.rat_flush_data         := retireRAT.io.rat_all_data
    ReNameRAT.io.retire_rat_wen         := retire.io.rat_write_en.asUInt
    ReNameRAT.io.retire_rat_waddr       := retire.io.rat_write_addr
    ReNameRAT.io.retire_rat_wdata       := retire.io.rat_write_data
    ReNameRAT.io.exception_mask_front   := retire.io.exception_mask_front.asUInt

    /* retire -> PCReg */
    pc_reg.io.rat_flush_en              := retire.io.rat_flush_en
    pc_reg.io.rat_flush_pc              := retire.io.rat_flush_pc

    /* retire <-> free reg id buffer */
    for(i <- 0 until base.FETCH_WIDTH){
        freeregbuf_seq(i).io.rat_write_en_retire := retire.io.free_reg_id_valid(i)
        freeregbuf_seq(i).io.freereg_i           := retire.io.free_reg_id_wdata(i)
        freeregbuf_seq(i).io.rob_state           := rob_buffer.io.rob_state
        freeregbuf_seq(i).io.flush_freereg_valid := retire.io.flush_free_reg_valid(i)
    }
    
    /* retire <-> free rob id buffer */
    for(i <- 0 until base.FETCH_WIDTH){
        robidbuf_seq(i).io.rat_write_en_retire   := retire.io.free_rob_id_valid(i)
        robidbuf_seq(i).io.free_robid_i          := retire.io.free_rob_id_wdata(i)
        robidbuf_seq(i).io.rat_flush_en          := retire.io.rat_flush_en
    }

    /* retire -> fetch/decode/rename1/rename2/dispatch/issue/regread/alu/agu/mem1/mem2/mem3 */
    fetch.io.rat_flush_en                          := retire.io.rat_flush_en
    decode.io.rat_flush_en                         := retire.io.rat_flush_en
    rename1.io.rat_flush_en                        := retire.io.rat_flush_en
    rename2.io.rat_flush_en                        := retire.io.rat_flush_en
    rename2.io.last_store_idx_retire               := retire.io.last_store_idx

    issue.io.rat_flush_en                          := retire.io.rat_flush_en
    // regread.io.rat_flush_en                        := retire.io.rat_flush_en
    memstage1.io.rat_flush_en                      := retire.io.rat_flush_en
    memstage2.io.rat_flush_en                      := retire.io.rat_flush_en
    memstage3.io.rat_flush_en                      := retire.io.rat_flush_en
    prf.io.rat_flush_en                            := retire.io.rat_flush_en
    retireRAT.io.rat_flush_en                      := retire.io.rat_flush_en

    /* retire -> PHT */
    pht.io.retire_br_mask := retire.io.retire_br_mask_o
    pht.io.retire_br_taken_vec := retire.io.retire_br_taken_o
    pht.io.retire_gpht_idx := retire.io.global_pht_idx_vec_o
    pht.io.retire_lpht_idx := retire.io.local_pht_idx_vec_o
    pht.io.retire_gbranch_pre_res_i := retire.io.retire_gbranch_res
    pht.io.retire_lbranch_pre_res_i := retire.io.retire_lbranch_res

    /* retire -> pcreg */
    pc_reg.io.retire_bht_idx := retire.io.bht_idx_vec_o
    pc_reg.io.retire_br_mask := retire.io.retire_br_mask_o
    pc_reg.io.retire_br_taken_vec := retire.io.retire_br_taken_o
    

    /* rob_state -> pc/fetch/decode/rename1/rename2/dispatch/issue/regread/alu/agu/mem1/mem2/mem3 */
    pc_reg.io.rob_state                         := rob_buffer.io.rob_state
    fetch.io.rob_state                          := rob_buffer.io.rob_state
    decode.io.rob_state                         := rob_buffer.io.rob_state
    rename1.io.rob_state                        := rob_buffer.io.rob_state
    rename2.io.rob_state                        := rob_buffer.io.rob_state
    dispatch.io.rob_state                       := rob_buffer.io.rob_state
    issue.io.rob_state                          := rob_buffer.io.rob_state
    // regread.io.rob_state                        := rob_buffer.io.rob_state
    memstage1.io.rob_state                      := rob_buffer.io.rob_state
    memstage2.io.rob_state                      := rob_buffer.io.rob_state
    memstage3.io.rob_state                      := rob_buffer.io.rob_state
    retire.io.rob_state                         := rob_buffer.io.rob_state  
    pht.io.rob_state                            := rob_buffer.io.rob_state
    btb.io.rob_state                            := rob_buffer.io.rob_state
      
    for(i <- 0 until base.ALU_NUM){
        alu_vec(i).io.rat_flush_en := retire.io.rat_flush_en
        alu_vec(i).io.rob_state    := rob_buffer.io.rob_state
    }
    for(i <- 0 until base.AGU_NUM){
        agu_vec(i).io.rat_flush_en := retire.io.rat_flush_en
        agu_vec(i).io.rob_state    := rob_buffer.io.rob_state
    }

    /* rob_buffer -> storebuffer及前面 */
    storebuffer.io.rob_items_i                 := rob_buffer.io.rob_item_o
    dispatch.io.rob_wr_able                    := rob_buffer.io.robw_able
    rename2.io.rob_wr_able                    := rob_buffer.io.robw_able
    rename1.io.rob_wr_able                    := rob_buffer.io.robw_able
    decode.io.rob_wr_able                    := rob_buffer.io.robw_able
    fetch.io.rob_wr_able                    := rob_buffer.io.robw_able
    pc_reg.io.rob_wr_able                    := rob_buffer.io.robw_able

    /* retire -> storebuffer */
    storebuffer.io.rob_state                := retire.io.rob_state

    /* storebuffer -> dispatch及之前模块 */
    dispatch.io.store_buffer_wr_able := storebuffer.io.wr_able
    rename2.io.store_buffer_wr_able := storebuffer.io.wr_able
    rename1.io.store_buffer_wr_able := storebuffer.io.wr_able
    decode.io.store_buffer_wr_able := storebuffer.io.wr_able
    fetch.io.store_buffer_wr_able := storebuffer.io.wr_able
    pc_reg.io.store_buffer_wr_able := storebuffer.io.wr_able

    /* rob free id buffer -> rename2及之前模块 */
    for(i <- 0 until base.FETCH_WIDTH){
        rename2.io.rob_freeid_rd_able(i) := robidbuf_seq(i).io.rd_able
        rename1.io.rob_freeid_rd_able(i) := robidbuf_seq(i).io.rd_able
        decode.io.rob_freeid_rd_able(i) := robidbuf_seq(i).io.rd_able
        fetch.io.rob_freeid_rd_able(i) := robidbuf_seq(i).io.rd_able
        pc_reg.io.rob_freeid_rd_able(i) := robidbuf_seq(i).io.rd_able
    }
}