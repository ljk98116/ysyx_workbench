package cpu.core.backend

import chisel3._
import chisel3.util._

import cpu.config._
import cpu.config.base.ALU_NUM

class RegReadStage extends Module
{
    val io = IO(new Bundle {
        val alu_fu_items_i = Input(Vec(base.ALU_NUM, new ROBItem))
        val agu_fu_items_i = Input(Vec(base.AGU_NUM, new ROBItem))
        /* PRF 读使能 */
        val prf_rs1_data_ren = Output(Vec(base.ALU_NUM + base.AGU_NUM, Bool()))
        val prf_rs2_data_ren = Output(Vec(base.ALU_NUM + base.AGU_NUM, Bool()))
        val prf_rs1_data_raddr = Output(Vec(base.ALU_NUM + base.AGU_NUM, UInt(base.PREG_WIDTH.W)))
        val prf_rs2_data_raddr = Output(Vec(base.ALU_NUM + base.AGU_NUM, UInt(base.PREG_WIDTH.W)))
        val prf_rs1_data_rdata = Input(Vec(base.ALU_NUM + base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val prf_rs2_data_rdata = Input(Vec(base.ALU_NUM + base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        /* 输出对应channel的操作数 */
        val alu_channel_rs1_rdata = Output(Vec(base.ALU_NUM, UInt(base.DATA_WIDTH.W)))
        val alu_channel_rs2_rdata = Output(Vec(base.ALU_NUM, UInt(base.DATA_WIDTH.W)))
        val agu_channel_rs1_rdata = Output(Vec(base.ALU_NUM, UInt(base.DATA_WIDTH.W)))
        val agu_channel_rs2_rdata = Output(Vec(base.ALU_NUM, UInt(base.DATA_WIDTH.W))) 
        /* 输出对应ROB项 */
        val alu_fu_items_o = Output(Vec(base.ALU_NUM, new ROBItem))
        val agu_fu_items_o = Output(Vec(base.AGU_NUM, new ROBItem))        
    })

    /* pipeline */
    var alu_fu_items_reg = RegInit(VecInit(
        Seq.fill(base.ALU_NUM)((0.U).asTypeOf(new ROBItem))
    ))
    var agu_fu_items_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U).asTypeOf(new ROBItem))
    ))

    alu_fu_items_reg := io.alu_fu_items_i
    agu_fu_items_reg := io.agu_fu_items_i

    var prf_rs1_data_ren = WireInit(VecInit(
        Seq.fill(base.ALU_NUM + base.AGU_NUM)(false.B)
    ))
    var prf_rs2_data_ren = WireInit(VecInit(
        Seq.fill(base.ALU_NUM + base.AGU_NUM)(false.B)
    ))
    var prf_rs1_data_raddr = WireInit(VecInit(
        Seq.fill(base.ALU_NUM + base.AGU_NUM)((0.U)(base.PREG_WIDTH.W))
    ))
    var prf_rs2_data_raddr = WireInit(VecInit(
        Seq.fill(base.ALU_NUM + base.AGU_NUM)((0.U)(base.PREG_WIDTH.W))
    ))
    var alu_channel_rs1_rdata = WireInit(VecInit(
        Seq.fill(base.ALU_NUM)((0.U)(base.DATA_WIDTH.W))
    ))
    var alu_channel_rs2_rdata = WireInit(VecInit(
        Seq.fill(base.ALU_NUM)((0.U)(base.DATA_WIDTH.W))
    ))
    var agu_channel_rs1_rdata = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W))
    ))
    var agu_channel_rs2_rdata = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W))
    ))
    for(i <- 0 until base.ALU_NUM){
        prf_rs1_data_ren(i) := alu_fu_items_reg(i).HasRs1
        prf_rs2_data_ren(i) := alu_fu_items_reg(i).HasRs2
        prf_rs1_data_raddr(i) := alu_fu_items_reg(i).ps1
        prf_rs2_data_raddr(i) := alu_fu_items_reg(i).ps2
        alu_channel_rs1_rdata(i) := io.prf_rs1_data_rdata(i)
        alu_channel_rs2_rdata(i) := io.prf_rs2_data_rdata(i)
    }

    for(i <- 0 until base.AGU_NUM){
        prf_rs1_data_ren(i + base.ALU_NUM) := agu_fu_items_reg(i).HasRs1
        prf_rs2_data_ren(i + base.ALU_NUM) := agu_fu_items_reg(i).HasRs2
        prf_rs1_data_raddr(i + base.ALU_NUM) := agu_fu_items_reg(i).ps1
        prf_rs2_data_raddr(i + base.ALU_NUM) := agu_fu_items_reg(i).ps2
        agu_channel_rs1_rdata(i) := io.prf_rs1_data_rdata(i + base.ALU_NUM)
        agu_channel_rs2_rdata(i) := io.prf_rs2_data_rdata(i + base.ALU_NUM)
    }
    /* connect */
    io.prf_rs1_data_ren := prf_rs1_data_ren
    io.prf_rs2_data_ren := prf_rs2_data_ren
    io.prf_rs1_data_raddr := prf_rs1_data_raddr
    io.prf_rs2_data_raddr := prf_rs2_data_raddr
    io.alu_channel_rs1_rdata := alu_channel_rs1_rdata
    io.alu_channel_rs2_rdata := alu_channel_rs2_rdata
    io.agu_channel_rs1_rdata := agu_channel_rs1_rdata
    io.agu_channel_rs2_rdata := agu_channel_rs2_rdata
    io.alu_fu_items_o := alu_fu_items_reg
    io.agu_fu_items_o := agu_fu_items_reg
}