package cpu.core

import chisel3._
import chisel3.util._

import cpu.config._
import cpu.core.frontend._
import cpu.core.backend._
import cpu.memory.MultiPortSram

/* PC -> Fetch -> Decode -> Rename1 -> Rename2 -> Dispatch -> */
/* Issue -> RegRead -> Ex -> Mem -> Retire */

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
    
    /* exec stage */

    /* load/store stage */

    /* retire stage */
    
    /* connection */
    /* pc -> fetch */
    fetch.io.pc_i                   := pc_reg.io.pc_o
    fetch.io.inst_valid_mask_i      := pc_reg.io.inst_valid_mask_o
    fetch.io.inst_valid_cnt_i       := pc_reg.io.inst_valid_cnt_o

    /* fetch -> memory */
    for(i <- 0 until base.FETCH_WIDTH){
        memory.io.ren(i) := fetch.io.inst_valid_mask_o(i)
        memory.io.raddr(i) := fetch.io.inst_valid_mask_o(i)
    }
    
    /* fetch -> decode */
    decode.io.pc_vec_i              := fetch.io.pc_vec_o
    decode.io.inst_valid_mask_i     := fetch.io.inst_valid_mask_o
    decode.io.inst_valid_cnt_i      := fetch.io.inst_valid_cnt_o

    /* memory -> decode */
    for(i <- 0 until base.FETCH_WIDTH){
        decode.io.inst_vec_i(i) := memory.io.rdata(i)
    }

    /* decode -> freeregbuffer */
    for(i <- 0 until base.FETCH_WIDTH){
        freeregbuf_seq(i).io.inst_valid_decode := decode.io.inst_valid_mask_o(i)
    }

    /* rename1 -> robidbuffer */
    for(i <- 0 until base.FETCH_WIDTH){
        robidbuf_seq(i).io.inst_valid_rename1 := rename1.io.inst_valid_mask_o(i)
    }

    /* freeregbuffer -> renamestage1 */
    for(i <- 0 until base.FETCH_WIDTH){
        rename1.io.freereg_vec_i(i) := freeregbuf_seq(i).io.freereg_o
    }

    /* decode -> renamestage1 */
    rename1.io.pc_vec_i             := decode.io.pc_vec_o
    rename1.io.inst_valid_mask_i    := decode.io.inst_valid_mask_o
    rename1.io.DecodeRes_i          := decode.io.DecodeRes_o
    rename1.io.inst_valid_cnt_i     := decode.io.inst_valid_cnt_o

    /* robidbuffer -> rename2 */
    for(i <- 0 until base.FETCH_WIDTH){
        rename2.io.rob_freeid_vec_i(i)  := robidbuf_seq(i).io.freeid_o
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

    /* rename2 -> RenameRAT */
    ReNameRAT.io.rat_ren            := rename2.io.rat_ren_o
    ReNameRAT.io.rat_raddr          := rename2.io.rat_raddr_o
    ReNameRAT.io.rat_wen            := rename2.io.rat_wen_o
    ReNameRAT.io.rat_waddr          := rename2.io.rat_waddr_o
    ReNameRAT.io.rat_wdata          := rename2.io.rat_wdata_o
    
    /* ReNameRAT -> rename2 */
    rename2.io.rat_rdata_i          := ReNameRAT.io.rat_rdata

    /* rename2 -> ROB */
    rob_buffer.io.rob_item_i        := rename2.io.rob_item_o
    rob_buffer.io.inst_valid_cnt_i  := rename2.io.inst_valid_cnt_o

    /* rename2 -> dispatch */
    dispatch.io.rob_item_i          := rename2.io.rob_item_o

}