package cpu.core

import chisel3._
import chisel3.util._

import cpu.config._
import cpu.core.frontend._

class CPUCore extends Module
{
    val io = IO(new Bundle{
    })

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
    
    /* regread stage */

    /* ROB ID Buffers */
    val robidbuf_seq = Seq.tabulate(base.FETCH_WIDTH)(
        (i) => Module(new ROBIDBuffer(i))
    )

    /* dispatch stage */

    /* issue stage */

    /* exec stage */

    /* load/store stage */

    /* retire stage */
    

    /* connection */
    /* pc -> fetch */
    fetch.io.pc_i                   := pc_reg.io.pc_o
    fetch.io.inst_valid_mask_i      := pc_reg.io.inst_valid_mask_o

    /* fetch -> decode */
    decode.io.pc_vec_i              := fetch.io.pc_vec_o
    decode.io.inst_valid_mask_i     := fetch.io.inst_valid_mask_o
    decode.io.inst_vec_i            := fetch.io.inst_vec_o

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


}