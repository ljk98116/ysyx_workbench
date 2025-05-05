package cpu.core.backend

import cpu.config._
import chisel3._
import chisel3.util._

/* 处理访存结果(2读1写)，驱动sram/dcache */
/* 扩展wmask的位宽到8 */
class MemStage2 extends Module{
    val io = IO(new Bundle {
        val mem_read_en_i = Input(Vec(base.AGU_NUM, Bool()))
        val mem_read_addr_i = Input(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val mem_write_en_i = Input(Bool())
        val mem_write_addr_i = Input(UInt(base.ADDR_WIDTH.W))
        val mem_write_mask_i = Input(UInt(4.W))
        val mem_write_data_i = Input(UInt(base.DATA_WIDTH.W))
        val mem_read_en_o = Output(Vec(base.AGU_NUM, Bool()))
        val mem_read_addr_o = Output(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val mem_write_en_o = Output(Bool())
        val mem_write_addr_o = Output(UInt(base.ADDR_WIDTH.W))
        val mem_write_mask_o = Output(UInt(8.W))
        val mem_write_data_o = Output(UInt(base.DATA_WIDTH.W))
    })
    /* pipeline */
    var mem_read_en_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))
    var mem_read_addr_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.ADDR_WIDTH.W))
    ))
    var mem_write_en_reg = RegInit(false.B)
    var mem_write_addr_reg = RegInit((0.U)(base.ADDR_WIDTH.W))
    var mem_write_mask_reg = RegInit((0.U)(4.W))
    var mem_write_data_reg = RegInit((0.U)(base.DATA_WIDTH.W))

    mem_read_en_reg := io.mem_read_en_i
    mem_read_addr_reg := io.mem_read_addr_i
    mem_write_en_reg := io.mem_write_en_i
    mem_write_addr_reg := io.mem_write_addr_i
    mem_write_mask_reg := io.mem_write_mask_i
    mem_write_data_reg := io.mem_write_data_i

    var mem_write_en_o = WireInit(false.B)
    var mem_write_addr_o = WireInit((0.U)(base.ADDR_WIDTH.W))
    var mem_write_mask_o = WireInit((0.U)(8.W))
    var mem_write_data_o = WireInit((0.U)(base.DATA_WIDTH.W))
    var mem_read_en_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))
    var mem_read_addr_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.ADDR_WIDTH.W))
    ))    
    mem_read_en_o := mem_read_en_reg
    mem_read_addr_o := mem_read_addr_reg
    mem_write_en_o := mem_write_en_reg
    mem_write_addr_o := mem_write_addr_reg
    mem_write_mask_o := mem_write_mask_reg
    mem_write_data_o := mem_write_data_reg

    /* connect */
    io.mem_read_en_o := mem_read_en_o
    io.mem_read_addr_o := mem_read_addr_o
    io.mem_write_en_o := mem_write_en_o
    io.mem_write_addr_o := mem_write_addr_o
    io.mem_write_mask_o := mem_write_mask_o
    io.mem_write_data_o := mem_write_data_o
}