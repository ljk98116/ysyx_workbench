package cpu.core.frontend

import chisel3._
import chisel3.util._

import cpu.config._

/* use stage2 to read/write RAT */
/* deal read result and construct ROB Item */
class RenameStage2 extends Module
{
    val io = IO(new Bundle{
        val rat_flush_en = Input(Bool())
        val rob_state = Input(Bool())
        val pc_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val inst_valid_mask_i = Input(UInt(base.FETCH_WIDTH.W))
        val inst_valid_cnt_i = Input(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))
        val DecodeRes_i = Input(Vec(base.FETCH_WIDTH, new DecodeRes))

        /* RAT读写使能 */
        val rat_wen_i = Input(UInt(base.FETCH_WIDTH.W))
        val rat_waddr_i = Input(Vec(base.FETCH_WIDTH, UInt(base.AREG_WIDTH.W)))
        val rat_wdata_i = Input(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))

        val rat_ren_i = Input(UInt((base.FETCH_WIDTH * 3).W))
        val rat_raddr_i = Input(Vec(base.FETCH_WIDTH * 3, UInt(base.AREG_WIDTH.W)))
        val rat_rdata_i = Input(Vec(base.FETCH_WIDTH * 3, UInt(base.PREG_WIDTH.W)))

        /* RAT读写使能 */
        val rat_wen_o = Output(UInt(base.FETCH_WIDTH.W))
        val rat_waddr_o = Output(Vec(base.FETCH_WIDTH, UInt(base.AREG_WIDTH.W)))
        val rat_wdata_o = Output(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))

        val rat_ren_o = Output(UInt((base.FETCH_WIDTH * 3).W))
        val rat_raddr_o = Output(Vec(base.FETCH_WIDTH * 3, UInt(base.AREG_WIDTH.W)))

        /* PRF寄存器状态设置 */
        val prf_valid_rd_wen = Output(Vec(base.FETCH_WIDTH, Bool()))
        val prf_valid_rd_waddr = Output(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))
        val prf_valid_rd_wdata = Output(Vec(base.FETCH_WIDTH, Bool()))

        /* RAW相关性信息 */
        val rs1_match = Input(Vec(base.FETCH_WIDTH, UInt(base.FETCH_WIDTH.W)))
        val rs2_match = Input(Vec(base.FETCH_WIDTH, UInt(base.FETCH_WIDTH.W)))

        /* ROB free buffer */
        val rob_freeid_vec_i = Input(Vec(base.FETCH_WIDTH, UInt(base.ROBID_WIDTH.W)))
        val rob_item_o = Output(Vec(base.FETCH_WIDTH, new ROBItem))
        val inst_valid_cnt_o = Output(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))
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

    pc_vec_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.pc_vec_i, pc_vec_reg), 
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.ADDR_WIDTH.W)))
    )
    inst_valid_mask_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.inst_valid_mask_i, inst_valid_mask_reg), 
        0.U
    )
    DecodeRes_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.DecodeRes_i, DecodeRes_reg), 
        VecInit(Seq.fill(base.FETCH_WIDTH)(0.U.asTypeOf(new DecodeRes)))
    )
    rat_wen_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.rat_wen_i, rat_ren_reg), 
        0.U
    )
    rat_waddr_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.rat_waddr_i, rat_waddr_reg), 
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.AREG_WIDTH.W)))
    )
    rat_wdata_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.rat_wdata_i, rat_wdata_reg), 
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.PREG_WIDTH.W)))
    )
    rat_ren_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.rat_ren_i, rat_ren_reg),
        0.U
    )
    rat_raddr_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.rat_raddr_i, rat_raddr_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH * 3)((0.U)(base.AREG_WIDTH.W)))
    )
    rs1_match_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.rs1_match, rs1_match_reg), 
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.FETCH_WIDTH.W)))
    )
    rs2_match_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.rs2_match, rs2_match_reg),
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U)(base.FETCH_WIDTH.W)))
    )
    inst_valid_cnt_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.inst_valid_cnt_i, inst_valid_cnt_reg), 
        0.U
    )

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
        rob_item_o(i).pd := Mux(DecodeRes_reg(i).HasRd, rat_wdata_reg(i), 0.U)
        rob_item_o(i).isBranch := DecodeRes_reg(i).IsBranch
        rob_item_o(i).isLoad   := DecodeRes_reg(i).IsLoad
        rob_item_o(i).isStore  := DecodeRes_reg(i).IsStore
        rob_item_o(i).rs1      := DecodeRes_reg(i).rs1
        rob_item_o(i).rs2      := DecodeRes_reg(i).rs2
        rob_item_o(i).rd       := DecodeRes_reg(i).rd
        rob_item_o(i).rdy      := false.B
        rob_item_o(i).rdy2     := false.B
        rob_item_o(i).rdy1     := false.B
        /* 暂时所有分支指令均冲刷流水线 */
        rob_item_o(i).hasException := false.B
        rob_item_o(i).ExceptionType := ExceptionType.NORMAL.U
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
                        io.rat_rdata_i(3 * i)
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
                        io.rat_rdata_i(3 * i + 1)
                    )
                )
            )
        }
    }

    var prf_valid_rd_wen = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var prf_valid_rd_waddr = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PREG_WIDTH.W))
    ))
    var prf_valid_rd_wdata = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))

    for(i <- 0 until base.FETCH_WIDTH){
        prf_valid_rd_wen(i) := DecodeRes_reg(i).HasRd
        prf_valid_rd_waddr(i) := rat_wdata_reg(i)
        prf_valid_rd_wdata(i) := false.B
    }
    /* connect */
    io.rat_ren_o := rat_ren_reg
    io.rat_raddr_o := rat_raddr_reg
    io.rat_wen_o := rat_wen_reg
    io.rat_waddr_o := rat_waddr_reg
    io.rat_wdata_o := rat_wdata_reg

    io.prf_valid_rd_wen := prf_valid_rd_wen
    io.prf_valid_rd_waddr := prf_valid_rd_waddr
    io.prf_valid_rd_wdata := prf_valid_rd_wdata

    io.rob_item_o := rob_item_o
    io.inst_valid_cnt_o := inst_valid_cnt_reg
}