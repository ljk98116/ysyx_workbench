package cpu.core.frontend

import chisel3._
import chisel3.util._

import cpu.config._
import cpu.core.utils.PriorityDecoder

/* 制造读使能，准备读取RAT */
/* 分析相关性，准备下一周期修改RAT */
/* 决定是否增加FreeRegIdBuffer的tail */
class RenameStage1 extends Module
{
    val io = IO(new Bundle {
        val freereg_rd_able = Input(Vec(base.FETCH_WIDTH, Bool()))
        val rat_flush_en = Input(Bool())
        val rob_state = Input(Bool())
        val pc_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val inst_valid_mask_i = Input(UInt(base.FETCH_WIDTH.W))
        val DecodeRes_i = Input(Vec(base.FETCH_WIDTH, new DecodeRes))
        val freereg_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))
        val inst_valid_cnt_i = Input(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))

        val pc_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val inst_valid_mask_o = Output(UInt(base.FETCH_WIDTH.W))
        val DecodeRes_o = Output(Vec(base.FETCH_WIDTH, new DecodeRes))
        val inst_valid_cnt_o = Output(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))

        /* RAW相关性信息 */ 
        val rs1_match = Output(Vec(base.FETCH_WIDTH, UInt(base.FETCH_WIDTH.W)))
        val rs2_match = Output(Vec(base.FETCH_WIDTH, UInt(base.FETCH_WIDTH.W)))
        
        /* RAT读写使能 */
        val rat_wen_o = Output(UInt(base.FETCH_WIDTH.W))
        val rat_waddr_o = Output(Vec(base.FETCH_WIDTH, UInt(base.AREG_WIDTH.W)))
        val rat_wdata_o = Output(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))

        val rat_ren_o = Output(UInt((base.FETCH_WIDTH * 3).W))
        val rat_raddr_o = Output(Vec(base.FETCH_WIDTH * 3, UInt(base.AREG_WIDTH.W)))
    })

    var stall = WireInit(false.B)

    stall := ~io.rob_state & io.freereg_rd_able.asUInt.andR
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

    var inst_valid_cnt_reg = RegInit((0.U)(log2Ceil(base.FETCH_WIDTH + 1).W))

    pc_vec_reg := Mux(
        ~io.rat_flush_en, 
        Mux(stall, io.pc_vec_i,pc_vec_reg), 
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.ADDR_WIDTH.W)))
    )
    inst_valid_mask_reg := Mux(
        ~io.rat_flush_en, 
        Mux(stall, io.inst_valid_mask_i, inst_valid_mask_reg),
        0.U
    )
    DecodeRes_reg := Mux(
        ~io.rat_flush_en, 
        Mux(stall, io.DecodeRes_i, DecodeRes_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)(0.U.asTypeOf(new DecodeRes)))
    )
    inst_valid_cnt_reg := Mux(
        ~io.rat_flush_en, 
        Mux(stall, io.inst_valid_cnt_i, inst_valid_cnt_reg),
        0.U
    )

    /* rs1/rs2 是否哪一个最近的前置rd相等，给出掩码 */
    var rs1_match = WireInit(
        VecInit(
            Seq.fill(base.FETCH_WIDTH)(
                VecInit(
                    Seq.fill(base.FETCH_WIDTH)(false.B)
                )
            )
        )
    )

    var rs2_match = WireInit(
        VecInit(
            Seq.fill(base.FETCH_WIDTH)(
                VecInit(
                    Seq.fill(base.FETCH_WIDTH)(false.B)
                )
            )
        )
    )    
    /* RAW相关性 */
    for(i <- 0 until base.FETCH_WIDTH){
        for(j <- 0 until i){
            when(DecodeRes_reg(j).HasRd)
            {
                when(DecodeRes_reg(i).HasRs1)
                {
                    rs1_match(i)(j) := DecodeRes_reg(j).rd === DecodeRes_reg(i).rs1
                }
                when(DecodeRes_reg(i).HasRs2)
                {
                    rs2_match(i)(j) := DecodeRes_reg(j).rd === DecodeRes_reg(i).rs2
                }
            }
        }
    }

    /* WAW相关性 */
    var rat_wen = WireInit((0.U)(base.FETCH_WIDTH.W))
    var rat_waw_mask = WireInit(
        VecInit(
            Seq.fill(base.FETCH_WIDTH)(
                VecInit(
                    Seq.fill(base.FETCH_WIDTH)(false.B)
                )
            )
        )
    )
    var rat_waddr = WireInit(
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.AREG_WIDTH.W)))
    )
    var rat_wdata = WireInit(
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.PREG_WIDTH.W)))
    )

    for(i <- 0 until base.FETCH_WIDTH){
        for(j <- i + 1 until base.FETCH_WIDTH){
            when(DecodeRes_reg(i).HasRd & DecodeRes_reg(j).HasRd)
            {
                rat_waw_mask(i)(j) := DecodeRes_reg(i).rd === DecodeRes_reg(j).rd
            }.otherwise{
                rat_waw_mask(i)(j) := false.B
            }
        }
    }

    /* 存在WAW冲突，不写RAT */
    rat_wen := Cat(
        ~(rat_waw_mask(3).asUInt.orR) & DecodeRes_reg(3).HasRd,
        ~(rat_waw_mask(2).asUInt.orR) & DecodeRes_reg(2).HasRd, 
        ~(rat_waw_mask(1).asUInt.orR) & DecodeRes_reg(1).HasRd, 
        ~(rat_waw_mask(0).asUInt.orR) & DecodeRes_reg(0).HasRd
    )

    for(i <- 0 until base.FETCH_WIDTH)
    {
        rat_waddr(i) := DecodeRes_reg(i).rd
        rat_wdata(i) := io.freereg_vec_i(i)
    }

    /* 读使能 */
    var rat_ren = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH * 3)(false.B)
    ))
    var rat_raddr = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH * 3)((0.U)(base.AREG_WIDTH.W))
    ))

    for(i <- 0 until base.FETCH_WIDTH)
    {
        when(DecodeRes_reg(i).HasRs1){
            rat_ren(3 * i) := true.B
            rat_raddr(3 * i) := DecodeRes_reg(i).rs1
        }
        when(DecodeRes_reg(i).HasRs2){
            rat_ren(3 * i + 1) := true.B
            rat_raddr(3 * i + 1) := DecodeRes_reg(i).rs2
        }
        rat_ren(3 * i + 2) := true.B
        rat_raddr(3 * i + 2) := DecodeRes_reg(i).rd
        io.rs1_match(i) := rs1_match(i).asUInt
        io.rs2_match(i) := rs2_match(i).asUInt
    }

    /* connect */
    io.pc_vec_o := Mux(stall, pc_vec_reg, VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.ADDR_WIDTH.W))))
    io.inst_valid_mask_o := Mux(stall, inst_valid_mask_reg, (0.U)(base.FETCH_WIDTH.W))
    io.DecodeRes_o := Mux(stall, DecodeRes_reg, VecInit(Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new DecodeRes))))
    io.rat_wen_o := Mux(stall, rat_wen, (0.U)(base.FETCH_WIDTH.W))
    io.rat_waddr_o := Mux(stall, rat_waddr, VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.AREG_WIDTH.W))))
    io.rat_wdata_o := Mux(stall, rat_wdata, VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.PREG_WIDTH.W))))
    io.rat_ren_o := Mux(stall, rat_ren.asUInt, 0.U)
    io.rat_raddr_o := Mux(stall, rat_raddr, VecInit(
        Seq.fill(base.FETCH_WIDTH * 3)((0.U)(base.AREG_WIDTH.W))
    ))
    io.inst_valid_cnt_o := Mux(stall, inst_valid_cnt_reg, (0.U)(base.FETCH_WIDTH.W))
}