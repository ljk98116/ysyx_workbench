package cpu.core.frontend

import chisel3._
import chisel3.util._

import cpu.config._

/* use stage2 to read/write RAT */
/* deal read result and construct ROB Item */
class RenameStage2 extends Module
{
    val io = IO(new Bundle{
        val pc_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val inst_valid_mask_i = Input(UInt(base.FETCH_WIDTH.W))
        val inst_valid_cnt_i = Input(UInt(log2Ceil(base.FETCH_WIDTH).W))
        val DecodeRes_i = Input(Vec(base.FETCH_WIDTH, new DecodeRes))

        /* RAT读写使能 */
        val rat_wen_i = Input(UInt(base.FETCH_WIDTH.W))
        val rat_waddr_i = Input(Vec(base.FETCH_WIDTH, UInt(base.AREG_WIDTH.W)))
        val rat_wdata_i = Input(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))

        val rat_ren_i = Input(UInt((base.FETCH_WIDTH * 2).W))
        val rat_raddr_i = Input(Vec(base.FETCH_WIDTH * 2, UInt(base.AREG_WIDTH.W)))
        val rat_rdata_i = Output(Vec(base.FETCH_WIDTH * 2, UInt(base.AREG_WIDTH.W)))

        /* RAT读写使能 */
        val rat_wen_o = Output(UInt(base.FETCH_WIDTH.W))
        val rat_waddr_o = Output(Vec(base.FETCH_WIDTH, UInt(base.AREG_WIDTH.W)))
        val rat_wdata_o = Output(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))

        val rat_ren_o = Output(UInt((base.FETCH_WIDTH * 2).W))
        val rat_raddr_o = Output(Vec(base.FETCH_WIDTH * 2, UInt(base.AREG_WIDTH.W)))

        /* RAW相关性信息 */
        val rs1_match = Input(Vec(base.FETCH_WIDTH, UInt(base.FETCH_WIDTH.W)))
        val rs2_match = Input(Vec(base.FETCH_WIDTH, UInt(base.FETCH_WIDTH.W)))

        /* ROB free buffer */
        val rob_freeid_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.ROBID_WIDTH.W)))
        val rob_item_o = Output(Vec(base.FETCH_WIDTH, new ROBItem))
        val inst_valid_cnt_o = Output(UInt(log2Ceil(base.FETCH_WIDTH).W))
    })

    /* pipeline */
    var pc_vec_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.ADDR_WIDTH.W))
    ))

    var inst_valid_mask_reg = RegInit(
        (0.U)(base.FETCH_WIDTH.W)
    )

    var DecodeRes_reg = RegInit(
        VecInit(Seq.fill(base.FETCH_WIDTH)(new DecodeRes))
    )

    var rat_wen_reg = RegInit((0.U)(base.FETCH_WIDTH.W))
    var rat_waddr_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.AREG_WIDTH.W))
    ))
    var rat_wdata_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PREG_WIDTH.W))
    ))
    var rat_ren_reg = RegInit((0.U)((base.FETCH_WIDTH * 2).W))
    var rat_raddr_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH * 2)((0.U)(base.AREG_WIDTH.W))
    ))
    var rs1_match_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.FETCH_WIDTH.W))
    ))
    var rs2_match_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.FETCH_WIDTH.W))
    ))

    var inst_valid_cnt_reg = RegInit((0.U)(log2Ceil(base.FETCH_WIDTH).W))

    pc_vec_reg := io.pc_vec_i
    inst_valid_mask_reg := io.inst_valid_mask_i
    DecodeRes_reg := io.DecodeRes_i
    rat_wen_reg := io.rat_wen_i
    rat_waddr_reg := io.rat_waddr_i
    rat_wdata_reg := io.rat_wdata_i
    rat_ren_reg := io.rat_ren_i
    rat_raddr_reg := io.rat_raddr_i
    rs1_match_reg := io.rs1_match
    rs2_match_reg := io.rs2_match
    inst_valid_cnt_reg := io.inst_valid_cnt_i

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

    for(i <- 0 until base.FETCH_WIDTH)
    {
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
        rob_item_o(i).pd := rat_wdata_reg(i)
        when(DecodeRes_reg(i).HasRs1){
            rob_item_o(i).ps1 := Mux(rs1_match_reg(i)(2), 
                rat_wdata_reg(2),
                Mux(
                    rs1_match_reg(i)(1),
                    rat_wdata_reg(1),
                    Mux(rs1_match_reg(i)(0),
                        rat_wdata_reg(0),
                        io.rat_rdata_i(2 * i)
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
                        io.rat_rdata_i(2 * i + 1)
                    )
                )
            )
        }
    }
    /* connect */
    io.rat_ren_o := rat_ren_reg
    io.rat_raddr_o := rat_raddr_reg
    io.rat_wen_o := rat_wen_reg
    io.rat_waddr_o := rat_waddr_reg
    io.rat_wdata_o := rat_wdata_reg
    io.rob_item_o := rob_item_o
    io.inst_valid_cnt_o := inst_valid_cnt_reg
}