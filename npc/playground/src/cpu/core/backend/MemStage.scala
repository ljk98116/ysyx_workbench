package cpu.core.backend

import cpu.config._
import chisel3._
import chisel3.util._

/* 访问存储器 */
class MemStage extends Module
{
    val io = IO(new Bundle{
        val rob_item_i = Input(Vec(base.AGU_NUM, new ROBItem))
        val agu_result_i = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val agu_rw_mask_i = Input(Vec(base.AGU_NUM, UInt(4.W)))
        val agu_mem_wdata = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        /* 输出到总线 */
        val mem_rdata_o = Output(UInt(base.DATA_WIDTH.W))
        val rob_id_o = Output(UInt(base.ROBID_WIDTH.W))
        val areg_wr_addr = Output(UInt(base.AREG_WIDTH.W))
        val preg_wr_addr = Output(UInt(base.PREG_WIDTH.W))
        val valid_o = Output(Bool())
    })

    /* pipeline(LSQ) */
    val lsq = Module(new LSQ(32))
    lsq.io.rob_item_i := io.rob_item_i
    lsq.io.agu_result_i := io.agu_result_i
    lsq.io.agu_rw_mask_i := io.agu_rw_mask_i
    lsq.io.agu_mem_wdata := io.agu_mem_wdata
    var lsq_item_o = WireInit((0.U).asTypeOf(new LSQ_Item))
    lsq_item_o := lsq.io.lsq_item_o

    /* DataSram */
    
}