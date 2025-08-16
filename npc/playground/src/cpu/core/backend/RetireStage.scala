package cpu.core.backend

import cpu.config._
import chisel3._
import chisel3.util._
import cpu.core.utils._

/* retire阶段写storebuffer */
class RetireStage extends Module
{
    val io = IO(new Bundle{
        val rob_state = Input(UInt(2.W))
        /* 当前头部ROB项 */
        val rob_items_i = Input(Vec(base.FETCH_WIDTH, new ROBItem))
        /* 是否可以提交 */
        val rob_item_rdy_mask = Output(UInt(base.FETCH_WIDTH.W))

        /* retire RAT */
        val rat_write_en = Output(Vec(base.FETCH_WIDTH, Bool()))
        val rat_write_addr = Output(Vec(base.FETCH_WIDTH, UInt(base.AREG_WIDTH.W)))
        val rat_write_data = Output(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))

        /* free reg id buffer */
        val free_reg_id_valid = Output(Vec(base.FETCH_WIDTH, Bool()))
        val free_reg_id_wdata = Output(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))
        val flush_free_reg_valid = Output(Vec(base.FETCH_WIDTH, Bool()))

        /* free rob id buffer */
        val free_rob_id_valid = Output(Vec(base.FETCH_WIDTH, Bool()))
        val free_rob_id_wdata = Output(Vec(base.FETCH_WIDTH, UInt(base.ROBID_WIDTH.W)))

        /* target branch addr */
        val global_pht_idx_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val local_pht_idx_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.PHTID_WIDTH.W)))
        val bht_idx_vec_o = Output(Vec(base.FETCH_WIDTH, UInt(base.BHTID_WIDTH.W)))
        /* 是否为本周期内有效的分支指令 */
        val retire_br_mask_o = Output(Vec(base.FETCH_WIDTH, Bool()))
        val retire_br_taken_o = Output(Vec(base.FETCH_WIDTH, Bool()))
        val retire_gbranch_res = Output(Vec(base.FETCH_WIDTH, Bool()))
        val retire_lbranch_res = Output(Vec(base.FETCH_WIDTH, Bool()))
        val rat_flush_pc = Output(UInt(base.ADDR_WIDTH.W))

        /* 是否覆盖前端RAT */
        val rat_flush_en = Output(Bool())
        val exception_mask_front = Output(Vec(base.FETCH_WIDTH, Bool()))
        val commit_valid_mask = Output(UInt(base.FETCH_WIDTH.W))

        /* 最后一个store指令的ROBID */
        val last_store_idx = Output(UInt((base.ROBID_WIDTH + 1).W))
    })

    var last_store_idx_reg = RegInit((1 << base.ROBID_WIDTH).U((base.ROBID_WIDTH + 1).W))
    /* 屏蔽位 */
    var store_mask_mid = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))

    var exception_mask_mid = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))

    var branch_mask_mid = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))

    var valid_mask_mid = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))

    /* mask为false，则对应指令为store指令或者存在异常,后面的指令不能提交 */
    for(i <- 0 until base.FETCH_WIDTH){
        store_mask_mid(i) := io.rob_items_i(i).isStore
        exception_mask_mid(i) := io.rob_items_i(i).hasException
        branch_mask_mid(i) := io.rob_items_i(i).isBranch &
            (io.rob_items_i(i).branch_pred_addr =/= (io.rob_items_i(i).pc + 4.U))
        valid_mask_mid(i) := io.rob_items_i(i).valid
    }

    /* 前面的指令有异常，后面的指令不能写RAT */
    /* store指令必须在ROB顶端才可以处理(其中该指令前面的指令都可以提交) */
    /* 准备好的非访存指令可以提交, 访存指令在前置指令就绪时可以提交 */
    /* 前置指令无异常时才可提交 */

    /* ROB buffer的head是否可以增加 */
    var rob_item_rdy_mask = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var commit_item_rdy_mask = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    /* 提交条件/写寄存器条件/增加ROB head条件 */
    /* 
    提交条件：
    1.ROB有效且rdy且非store，或者store指令且有效
    2.该位置无效但前置位置可提交
    3.该位置前面有异常
    */
    rob_item_rdy_mask(0) := io.rob_items_i(0).valid & io.rob_items_i(0).rdy 
    commit_item_rdy_mask(0) := io.rob_items_i(0).valid & io.rob_items_i(0).rdy
    rob_item_rdy_mask(1) := 
        (
            (io.rob_items_i(0).hasException | ~io.rob_items_i(1).valid | branch_mask_mid(0)) & 
            rob_item_rdy_mask(0)
        ) |
        (        
            (~io.rob_items_i(0).hasException & io.rob_items_i(1).valid & io.rob_items_i(1).rdy)
        )
    commit_item_rdy_mask(1) := 
        (io.rob_items_i(1).valid & io.rob_items_i(1).rdy) | 
        (~io.rob_items_i(1).valid & commit_item_rdy_mask(0)) |
        (io.rob_items_i(1).valid & (io.rob_items_i(0).hasException))
        
    io.commit_valid_mask := Mux(
        commit_item_rdy_mask.asUInt.andR & (io.rob_state === 0.U),
        Cat(
            io.rob_items_i(3).valid & (io.rob_state === 0.U) & ~(exception_mask_mid.asUInt(2, 0).orR | branch_mask_mid.asUInt(2, 0).orR),
            io.rob_items_i(2).valid & (io.rob_state === 0.U) & ~(exception_mask_mid.asUInt(1, 0).orR | branch_mask_mid.asUInt(1, 0).orR),
            io.rob_items_i(1).valid & (io.rob_state === 0.U) & ~(exception_mask_mid.asUInt(0).orR | branch_mask_mid.asUInt(0).orR),
            io.rob_items_i(0).valid & (io.rob_state === 0.U)
        ),
        0.U
    )

    rob_item_rdy_mask(2) := 
        (
            (
                ~io.rob_items_i(2).valid | 
                io.rob_items_i(1).hasException | 
                io.rob_items_i(0).hasException |
                branch_mask_mid(1) | 
                branch_mask_mid(0)
            ) & rob_item_rdy_mask(1)
        ) |
        (
            ~io.rob_items_i(1).hasException & 
            ~io.rob_items_i(0).hasException & 
            io.rob_items_i(2).valid & io.rob_items_i(2).rdy
        )
    commit_item_rdy_mask(2) := 
        (io.rob_items_i(2).valid & io.rob_items_i(2).rdy) |
        (~io.rob_items_i(2).valid & commit_item_rdy_mask(1)) |
        (io.rob_items_i(2).valid & (io.rob_items_i(1).hasException | io.rob_items_i(0).hasException))
    rob_item_rdy_mask(3) := 
        (
            (
                ~io.rob_items_i(3).valid | 
                io.rob_items_i(2).hasException | 
                io.rob_items_i(1).hasException | 
                io.rob_items_i(0).hasException |
                branch_mask_mid(2) |
                branch_mask_mid(1) | 
                branch_mask_mid(0)
            ) & 
            rob_item_rdy_mask(2)
        ) |
        (
            ~io.rob_items_i(2).hasException & 
            ~io.rob_items_i(1).hasException & 
            ~io.rob_items_i(0).hasException & 
            io.rob_items_i(3).valid & 
            io.rob_items_i(3).rdy
        )
        
    commit_item_rdy_mask(3) := 
        (io.rob_items_i(3).valid & io.rob_items_i(3).rdy) | 
        (~io.rob_items_i(3).valid & commit_item_rdy_mask(2)) |
        (io.rob_items_i(3).valid & (io.rob_items_i(2).hasException | io.rob_items_i(1).hasException | io.rob_items_i(0).hasException))
    /* 存在异常刷新流水线 */
    var rat_flush_pc = WireInit((0.U)(base.ADDR_WIDTH.W))
    /* 存在异常且异常的前置指令可提交时置位 */
    var rat_flush_en = WireInit(false.B)
    var encoder = Module(new PriorityEncoder(base.FETCH_WIDTH))
    encoder.io.val_i := Cat(
        io.rob_items_i(3).hasException,
        io.rob_items_i(2).hasException,
        io.rob_items_i(1).hasException,
        io.rob_items_i(0).hasException,
    )
    rat_flush_pc := io.rob_items_i(encoder.io.idx_o).targetBrAddr
    rat_flush_en := 
        ((io.rob_items_i(0).hasException & io.rob_items_i(0).rdy) |
        (io.rob_items_i(1).hasException & io.rob_items_i(1).rdy & rob_item_rdy_mask(0)) |
        (io.rob_items_i(2).hasException & io.rob_items_i(2).rdy & rob_item_rdy_mask(1) & rob_item_rdy_mask(0)) |
        (io.rob_items_i(3).hasException & io.rob_items_i(3).rdy & rob_item_rdy_mask(2) & rob_item_rdy_mask(1) & rob_item_rdy_mask(0))) &
        (io.rob_state === 0.U)

    /* retire RAT */
    var rat_write_en = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var rat_write_addr = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.AREG_WIDTH.W))
    ))
    var rat_write_data = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PREG_WIDTH.W))
    ))
    /* 写入retireRAT */
    for(i <- 0 until base.FETCH_WIDTH){
        when(rob_item_rdy_mask.asUInt.andR){
            rat_write_en(i) := 
                (io.rob_state === 0.U) & 
                io.rob_items_i(i).HasRd & 
                ~(exception_mask_mid.asUInt(i-1, 0).orR | branch_mask_mid.asUInt(i-1, 0).orR)

            rat_write_addr(i) := io.rob_items_i(i).rd
            rat_write_data(i) := io.rob_items_i(i).pd
        }.otherwise{
            rat_write_en(i) := false.B
            rat_write_addr(i) := 0.U
            rat_write_data(i) := 0.U           
        }
    }

    /* Free reg id buffer */
    /* normal状态下提交寄存器 */
    /* 前置无异常，看oldpd是否有效，有异常则不用 */
    var free_reg_id_valid = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var free_reg_id_wdata = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PREG_WIDTH.W))
    ))
    /* flush状态下提交寄存器 */
    var flush_free_reg_valid = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    /* 前置无异常 */
    for(i <- 0 until base.FETCH_WIDTH){
        if(i > 0){
            free_reg_id_valid(i) := 
                rob_item_rdy_mask.asUInt.andR & 
                (io.rob_state === 0.U) & io.rob_items_i(i).HasRd & 
                (
                    (~(exception_mask_mid.asUInt(i-1, 0).orR) & ~io.rob_items_i(i).oldpd(base.PREG_WIDTH)) |
                    exception_mask_mid.asUInt(i-1, 0).orR
                )
            flush_free_reg_valid(i) := 
                (io.rob_state === "b11".U | ((io.rob_state === "b00".U) & exception_mask_mid.asUInt(i-1, 0).orR)) & 
                io.rob_items_i(i).valid & io.rob_items_i(i).HasRd
                
            free_reg_id_wdata(i) := Mux(
                (io.rob_state === 0.U) & ~(exception_mask_mid.asUInt(i-1, 0).orR), 
                io.rob_items_i(i).oldpd, 
                io.rob_items_i(i).pd
            )
        }
        else{
            free_reg_id_valid(i) := commit_item_rdy_mask.asUInt.andR & (io.rob_state === 0.U) & io.rob_items_i(i).HasRd & 
                ~io.rob_items_i(i).oldpd(base.PREG_WIDTH)
            flush_free_reg_valid(i) := (io.rob_state === "b11".U) & io.rob_items_i(i).valid & io.rob_items_i(i).HasRd
            free_reg_id_wdata(i) := Mux((io.rob_state === 0.U), io.rob_items_i(i).oldpd, io.rob_items_i(i).pd)
        }
    }

    /* Free rob id buffer */
    var free_rob_id_valid = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var free_rob_id_wdata = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.ROBID_WIDTH.W))
    ))

    for(i <- 0 until base.FETCH_WIDTH){
        free_rob_id_valid(i) := 
            (commit_item_rdy_mask.asUInt.andR & io.rob_items_i(i).valid & (io.rob_state === 0.U)) | 
            ((io.rob_state === "b11".U) & io.rob_items_i(i).valid)
        free_rob_id_wdata(i) := io.rob_items_i(i).id
    }    

    /* debug */
    var commit = Module(new CommitAPI)
    commit.io.rst := reset.asBool
    /* 前置分支指令是否有效且跳转 */
    commit.io.rat_write_en := 
        Cat(
            io.rob_items_i(3).valid & (io.rob_state === 0.U) & ~(exception_mask_mid.asUInt(2, 0).orR | branch_mask_mid.asUInt(2, 0).orR),
            io.rob_items_i(2).valid & (io.rob_state === 0.U) & ~(exception_mask_mid.asUInt(1, 0).orR | branch_mask_mid.asUInt(1, 0).orR),
            io.rob_items_i(1).valid & (io.rob_state === 0.U) & ~(exception_mask_mid.asUInt(0).orR | branch_mask_mid.asUInt(0).orR),
            io.rob_items_i(0).valid & (io.rob_state === 0.U)
        )
    
    commit.io.valid := commit_item_rdy_mask.asUInt.andR & (io.rob_state === 0.U)
    commit.io.rat_write_addr_0 := rat_write_addr(0)
    commit.io.rat_write_addr_1 := rat_write_addr(1)
    commit.io.rat_write_addr_2 := rat_write_addr(2)
    commit.io.rat_write_addr_3 := rat_write_addr(3)
    commit.io.rat_write_data_0 := rat_write_data(0)
    commit.io.rat_write_data_1 := rat_write_data(1)
    commit.io.rat_write_data_2 := rat_write_data(2)
    commit.io.rat_write_data_3 := rat_write_data(3)
    commit.io.reg_write_data_0 := io.rob_items_i(0).reg_wb_data
    commit.io.reg_write_data_1 := io.rob_items_i(1).reg_wb_data
    commit.io.reg_write_data_2 := io.rob_items_i(2).reg_wb_data
    commit.io.reg_write_data_3 := io.rob_items_i(3).reg_wb_data
    commit.io.pc0              := io.rob_items_i(0).pc
    commit.io.pc1              := io.rob_items_i(1).pc
    commit.io.pc2              := io.rob_items_i(2).pc
    commit.io.pc3              := io.rob_items_i(3).pc

    var branch_pred_api = Module(new BranchPredAPI)
    branch_pred_api.io.is_branch := Mux(commit.io.valid =/= 0.U, 
        ( 
            Cat(io.rob_items_i(3).isBranch, io.rob_items_i(2).isBranch, io.rob_items_i(1).isBranch, io.rob_items_i(0).isBranch) & commit.io.rat_write_en
        ).asTypeOf(UInt(8.W)), 0.U)
    branch_pred_api.io.branch_pred_err := Cat(
        (io.rob_items_i(3).ExceptionType === ExceptionType.BRANCH_PREDICTION_ERROR.U) & commit.io.rat_write_en(3),
        (io.rob_items_i(2).ExceptionType === ExceptionType.BRANCH_PREDICTION_ERROR.U) & commit.io.rat_write_en(2),
        (io.rob_items_i(1).ExceptionType === ExceptionType.BRANCH_PREDICTION_ERROR.U) & commit.io.rat_write_en(1),
        (io.rob_items_i(0).ExceptionType === ExceptionType.BRANCH_PREDICTION_ERROR.U) & commit.io.rat_write_en(0)
    ).asTypeOf(UInt(8.W))

    /* connect */
    io.rob_item_rdy_mask := Mux((io.rob_state === 0.U), rob_item_rdy_mask.asUInt, 0.U)
    io.rat_write_en := rat_write_en
    io.rat_write_addr := rat_write_addr
    io.rat_write_data := rat_write_data

    io.rat_flush_en := rat_flush_en
    io.rat_flush_pc := rat_flush_pc

    io.free_reg_id_valid := free_reg_id_valid
    io.free_reg_id_wdata := free_reg_id_wdata
    io.flush_free_reg_valid := flush_free_reg_valid

    io.free_rob_id_valid := free_rob_id_valid
    io.free_rob_id_wdata := free_rob_id_wdata
    for(i <- 0 until base.FETCH_WIDTH){
        if(i > 0){
            io.exception_mask_front(i) := exception_mask_mid.asUInt(i-1, 0).orR
        }else{
            io.exception_mask_front(i) := false.B
        }
    }

    /* 分支预测 */
    /* 分支指令且前置无异常 */
    for(i <- 0 until base.FETCH_WIDTH){
        io.global_pht_idx_vec_o(i) := io.rob_items_i(i).global_pht_idx
        io.local_pht_idx_vec_o(i) := io.rob_items_i(i).local_pht_idx
        io.bht_idx_vec_o(i) := io.rob_items_i(i).bht_idx
        io.retire_br_taken_o(i) := (io.rob_items_i(i).targetBrAddr =/= (io.rob_items_i(i).pc + 4.U))
        io.retire_gbranch_res(i) := io.rob_items_i(i).gbranch_res
        io.retire_lbranch_res(i) := io.rob_items_i(i).lbranch_res
        if(i > 0){
            io.retire_br_mask_o(i) := 
                io.rob_items_i(i).isBranch & 
                ~exception_mask_mid.asUInt(i-1, 0).orR &
                ~branch_mask_mid.asUInt(i-1, 0).orR
        }
        else{
            io.retire_br_mask_o(i) := 
                io.rob_items_i(i).isBranch
        }
        
    }

    /* 没有异常的最后一条指令的storeIdx */
    var prio_dec1 = Module(new PriorityDecoder(4))
    var prio_dec2 = Module(new PriorityDecoder(4))
    var last_exception_idx = WireInit((4.U)(3.W))
    var last_valid_idx = WireInit((4.U)(3.W))

    prio_dec1.io.in := exception_mask_mid.asUInt
    last_exception_idx := Mux(exception_mask_mid.asUInt.orR, prio_dec1.io.out, 4.U)

    prio_dec2.io.in := valid_mask_mid.asUInt
    last_valid_idx := Mux(valid_mask_mid.asUInt.orR, prio_dec2.io.out, 4.U)

    io.last_store_idx := Mux(
        last_exception_idx(2), 
        Mux(
            last_valid_idx(2),
            last_store_idx_reg,
            io.rob_items_i(last_valid_idx(1,0)).storeIdx
        ),
        io.rob_items_i(last_exception_idx(1,0)).storeIdx
    )

    last_store_idx_reg := io.last_store_idx
}