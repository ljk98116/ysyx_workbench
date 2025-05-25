package cpu.core

import chisel3._
import chisel3.util._
import cpu.config._

/* 物理寄存器堆，维护操作数以及ready状态 */
class PRF extends Module
{
    val io = IO(new Bundle {
        /* PRF 读使能 */
        val prf_rs1_data_ren = Input(Vec(base.ALU_NUM + base.AGU_NUM, Bool()))
        val prf_rs2_data_ren = Input(Vec(base.ALU_NUM + base.AGU_NUM, Bool()))
        val prf_rs1_data_raddr = Input(Vec(base.ALU_NUM + base.AGU_NUM, UInt(base.PREG_WIDTH.W)))
        val prf_rs2_data_raddr = Input(Vec(base.ALU_NUM + base.AGU_NUM, UInt(base.PREG_WIDTH.W)))
        /* cdb消息, 写入PRF */
        val cdb_i = Input(new CDB)
        val rat_flush_en = Input(Bool())
        /* 读数据结果 */
        val prf_rs1_data_rdata = Output(Vec(base.ALU_NUM + base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val prf_rs2_data_rdata = Output(Vec(base.ALU_NUM + base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        /* PRF valid 接口 */
        val prf_valid_rs1_ren = Input(Vec(base.FETCH_WIDTH, Bool()))
        val prf_valid_rs2_ren = Input(Vec(base.FETCH_WIDTH, Bool()))
        val prf_valid_rs1_raddr = Input(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))
        val prf_valid_rs2_raddr = Input(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))
        val prf_valid_rs1_rdata = Output(Vec(base.FETCH_WIDTH, Bool()))
        val prf_valid_rs2_rdata = Output(Vec(base.FETCH_WIDTH, Bool()))

        val prf_valid_rd_wen = Input(Vec(base.FETCH_WIDTH, Bool()))
        val prf_valid_rd_waddr = Input(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))
        val prf_valid_rd_wdata = Input(Vec(base.FETCH_WIDTH, Bool()))
    })

    var prf_regs = RegInit(VecInit(
        Seq.fill(1 << base.PREG_WIDTH)((0.U)(base.DATA_WIDTH.W))
    ))

    var prf_valid_regs = RegInit(VecInit(
        Seq.fill(1 << base.PREG_WIDTH)(true.B)
    ))

    var prf_rs1_data_rdata = WireInit(VecInit(
        Seq.fill(base.ALU_NUM + base.AGU_NUM)((0.U)(base.DATA_WIDTH.W))
    ))
    var prf_rs2_data_rdata = WireInit(VecInit(
        Seq.fill(base.ALU_NUM + base.AGU_NUM)((0.U)(base.DATA_WIDTH.W))
    ))

    var prf_valid_rs1_rdata = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))

    var prf_valid_rs2_rdata = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))

    for(i <- 0 until base.ALU_NUM + base.AGU_NUM){
        prf_rs1_data_rdata(i) := Mux(io.prf_rs1_data_ren(i), prf_regs(io.prf_rs1_data_raddr(i)), 0.U)
        prf_rs2_data_rdata(i) := Mux(io.prf_rs2_data_ren(i), prf_regs(io.prf_rs2_data_raddr(i)), 0.U)
    }

    for(i <- 0 until (1 << base.PREG_WIDTH)){
        for(j <- 0 until base.ALU_NUM){
            when(io.cdb_i.alu_channel(j).phy_reg_id === i.U & ~io.rat_flush_en){
                prf_valid_regs(i) := 
                    Mux(io.cdb_i.alu_channel(j).valid,
                        true.B,
                        prf_valid_regs(i)
                    )
            }.elsewhen(io.rat_flush_en){
                prf_valid_regs(i) := true.B
            }
        }
        for(j <- 0 until base.AGU_NUM){
            when(io.cdb_i.agu_channel(j).phy_reg_id === i.U & ~io.rat_flush_en){
                prf_valid_regs(i) := 
                    Mux(io.cdb_i.agu_channel(j).valid,
                        true.B,
                        prf_valid_regs(i)
                    )                
            }.elsewhen(io.rat_flush_en){
                prf_valid_regs(i) := true.B
            }
        }
    }

    for(i <- 0 until base.ALU_NUM){
        prf_regs(io.cdb_i.alu_channel(i).phy_reg_id) := 
            Mux(io.cdb_i.alu_channel(i).valid, 
                io.cdb_i.alu_channel(i).reg_wr_data, 
                prf_regs(io.cdb_i.alu_channel(i).phy_reg_id)
            )
    }

    for(i <- 0 until base.AGU_NUM){
        prf_regs(io.cdb_i.agu_channel(i).phy_reg_id) := 
            Mux(io.cdb_i.agu_channel(i).valid, 
                io.cdb_i.agu_channel(i).reg_wr_data, 
                prf_regs(io.cdb_i.agu_channel(i).phy_reg_id)
            )
    }    

    var valid_en = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))

    for(i <- 0 until base.FETCH_WIDTH){
        var validvec = WireInit(VecInit(
            Seq.fill(base.ALU_NUM + base.AGU_NUM)(false.B)
        ))
        for(j <- 0 until base.ALU_NUM){
            validvec(j) := io.prf_valid_rd_waddr(i) =/= io.cdb_i.alu_channel(j).phy_reg_id
        }
        for(j <- 0 until base.AGU_NUM){
            validvec(j + base.ALU_NUM) := io.prf_valid_rd_waddr(i) =/= io.cdb_i.agu_channel(j).phy_reg_id
        }
        valid_en(i) := validvec.asUInt.andR & io.prf_valid_rd_wen(i)
    }

    for(i <- 0 until base.FETCH_WIDTH){
        when(valid_en(i)){
            prf_valid_regs(io.prf_valid_rd_waddr(i)) := io.prf_valid_rd_wdata(i)
        }
    }

    for(i <- 0 until base.FETCH_WIDTH){
        prf_valid_rs1_rdata(i) := 
            Mux(io.prf_valid_rs1_ren(i), 
                prf_valid_regs(io.prf_valid_rs1_raddr(i)),
                false.B
            )
        prf_valid_rs2_rdata(i) := 
            Mux(io.prf_valid_rs2_ren(i), 
                prf_valid_regs(io.prf_valid_rs2_raddr(i)),
                false.B
            )
    }

    io.prf_rs1_data_rdata := prf_rs1_data_rdata
    io.prf_rs2_data_rdata := prf_rs2_data_rdata
    io.prf_valid_rs1_rdata := prf_valid_rs1_rdata
    io.prf_valid_rs2_rdata := prf_valid_rs2_rdata
}