package cpu.core.backend

import cpu.config._
import chisel3._
import chisel3.util._
import cpu.core.utils

/* retire阶段写storebuffer */
class RetireStage extends Module
{
    val io = IO(new Bundle{
        /* 当前头部ROB项 */
        val rob_items_i = Input(Vec(base.FETCH_WIDTH, new ROBItem))
        /* 是否可以退出ROB */
        val rob_item_rdy_mask = Output(UInt(base.FETCH_WIDTH.W))
        val rob_item_commit_cnt = Output(UInt((log2Ceil(base.FETCH_WIDTH) + 1).W))

        /* retire RAT */
        val rat_write_en = Output(Vec(base.FETCH_WIDTH, Bool()))
        val rat_write_addr = Output(Vec(base.FETCH_WIDTH, UInt(base.AREG_WIDTH.W)))
        val rat_write_data = Output(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))

        /* free reg id buffer */
        val free_reg_id_valid = Output(Vec(base.FETCH_WIDTH, Bool()))
        val free_reg_id_wdata = Output(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))

        /* free rob id buffer */
        val free_rob_id_valid = Output(Vec(base.FETCH_WIDTH, Bool()))
        val free_rob_id_wdata = Output(Vec(base.FETCH_WIDTH, UInt(base.ROBID_WIDTH.W)))

        /* target branch addr */
        val rat_flush_pc = Output(UInt(base.ADDR_WIDTH.W))

        /* 是否覆盖前端RAT */
        val rat_flush_en = Output(Bool())
    })

    /* 屏蔽位 */
    var store_mask_mid = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var load_mask_mid = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var branch_mask_mid = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))

    var store_mask = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var load_mask = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var branch_mask = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))

    /* mask为false，则对应指令为store/load/branch指令预测出错,后面的指令不能提交 */
    for(i <- 0 until base.FETCH_WIDTH){
        store_mask_mid(i) := ~(io.rob_items_i(i).isStore)
        load_mask_mid(i) := ~(io.rob_items_i(i).isLoad)
        branch_mask_mid(i) := ~(io.rob_items_i(i).isBranch & io.rob_items_i(i).ExceptionType === ExceptionType.BRANCH_PREDICTION_ERROR.U)
    }

    /* 屏蔽高位重复1 */
    switch(store_mask_mid.asUInt){
        is("b1100".U, "b1000".U, "b0100".U, "b0000".U){
            store_mask(0) := true.B
            store_mask(1) := false.B
            store_mask(2) := false.B
            store_mask(3) := false.B
        }
        is(
            "b1010".U, "b0010".U,
            "b0001".U, "b1001".U
        ){
            store_mask(0) := true.B
            store_mask(1) := true.B
            store_mask(2) := false.B
            store_mask(3) := false.B
        }
        is(
            "b0110".U,
            "b0011".U, 
            "b0101".U
        ){
            store_mask(0) := true.B
            store_mask(1) := true.B
            store_mask(2) := true.B
            store_mask(3) := false.B           
        }
        is(
            "b1110".U,
            "b1101".U,
            "b1011".U,
            "b0111".U,
            "b1111".U
        ){
            store_mask(0) := true.B
            store_mask(1) := true.B
            store_mask(2) := true.B
            store_mask(3) := true.B            
        }
    }

    switch(branch_mask_mid.asUInt){
        is(
            "b0000".U, 
            "b0010".U, "b0100".U, "b1000".U,
            "b0110".U, "b1100".U, "b1010".U,
            "b1110".U
        ){
            branch_mask(0) := true.B
            branch_mask(1) := false.B
            branch_mask(2) := false.B
            branch_mask(3) := false.B
        }
        is(
            "b0001".U, 
            "b0101".U, "b1001".U,
            "b1101".U
        ){
            branch_mask(0) := true.B
            branch_mask(1) := true.B
            branch_mask(2) := false.B
            branch_mask(3) := false.B
        }
        is(
            "b0011".U, "b1011".U
        ){
            branch_mask(0) := true.B
            branch_mask(1) := true.B
            branch_mask(2) := true.B
            branch_mask(3) := false.B            
        }
        is(
            "b1111".U, "b0111".U
        ){
            branch_mask(0) := true.B
            branch_mask(1) := true.B
            branch_mask(2) := true.B
            branch_mask(3) := true.B            
        }
    }

    switch(load_mask_mid.asUInt){
        is("b1100".U, "b1000".U, "b0100".U, "b0000".U){
            load_mask(0) := true.B
            load_mask(1) := false.B
            load_mask(2) := false.B
            load_mask(3) := false.B 
        }
        is(
            "b1010".U, "b0010".U,
            "b0001".U, "b1001".U
        ){
            load_mask(0) := true.B
            load_mask(1) := true.B
            load_mask(2) := false.B
            load_mask(3) := false.B 
        }
        is(
            "b0110".U,
            "b0011".U, 
            "b0101".U
        ){
            load_mask(0) := true.B
            load_mask(1) := true.B
            load_mask(2) := true.B
            load_mask(3) := false.B            
        }
        is(
            "b1110".U,
            "b1101".U,
            "b1011".U,
            "b0111".U,
            "b1111".U
        ){
            load_mask(0) := true.B
            load_mask(1) := true.B
            load_mask(2) := true.B
            load_mask(3) := true.B            
        }
    }    

    var rob_item_rdy_mask_mid = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var rob_item_rdy_mask = WireInit((0.U)(base.FETCH_WIDTH.W))
    for(i <- 0 until base.FETCH_WIDTH){
        rob_item_rdy_mask_mid(i) := 
            load_mask(i) & branch_mask(i) & store_mask(i) & io.rob_items_i(i).rdy
    }
    var rob_item_commit_cnt = WireInit((0.U)((log2Ceil(base.FETCH_WIDTH) + 1).W))
    rob_item_commit_cnt := 0.U
    rob_item_rdy_mask := 0.U
    switch(rob_item_rdy_mask_mid.asUInt){
        is("b0001".U, "b0101".U, "b1001".U, "b1101".U){
            rob_item_rdy_mask := "b0001".U
            rob_item_commit_cnt := 1.U
        }
        is("b0011".U, "b1011".U){
            rob_item_rdy_mask := "b0011".U
            rob_item_commit_cnt := 2.U
        }
        is("b0111".U){
            rob_item_rdy_mask := "b0111".U
            rob_item_commit_cnt := 3.U            
        }
        is("b1111".U){
            rob_item_rdy_mask := "b1111".U
            rob_item_commit_cnt := 4.U
        }
    }

    /* 存在分支预测错误刷新流水线 */
    var rat_flush_pc = WireInit((0.U)(base.ADDR_WIDTH.W))
    var encoder = Module(new utils.PriorityEncoder(base.FETCH_WIDTH))
    encoder.io.val_i := ~(branch_mask_mid.asUInt)
    rat_flush_pc := io.rob_items_i(encoder.io.idx_o).targetBrAddr

    var rat_flush_en = WireInit(false.B)
    rat_flush_en := rob_item_rdy_mask(encoder.io.idx_o) & ~(branch_mask_mid.asUInt) =/= 0.U 

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
    for(i <- 0 until base.FETCH_WIDTH){
        when(rob_item_rdy_mask(i)){
            rat_write_en(i) := true.B
            rat_write_addr(i) := io.rob_items_i(i).rd
            rat_write_data(i) := io.rob_items_i(i).pd
        }.otherwise{
            rat_write_en(i) := false.B
            rat_write_addr(i) := 0.U
            rat_write_data(i) := 0.U           
        }
    }

    /* Free reg id buffer */
    var free_reg_id_valid = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var free_reg_id_wdata = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PREG_WIDTH.W))
    ))

    for(i <- 0 until base.FETCH_WIDTH){
        free_reg_id_valid(i) := rob_item_rdy_mask(i) 
        free_reg_id_wdata(i) := io.rob_items_i(i).oldpd
    }

    /* Free rob id buffer */
    var free_rob_id_valid = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var free_rob_id_wdata = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.ROBID_WIDTH.W))
    ))

    for(i <- 0 until base.FETCH_WIDTH){
        free_rob_id_valid(i) := rob_item_rdy_mask(i) 
        free_rob_id_wdata(i) := io.rob_items_i(i).id
    }    

    /* connect */
    io.rob_item_rdy_mask := rob_item_rdy_mask | Cat(~io.rob_items_i(3).valid, ~io.rob_items_i(2).valid, ~io.rob_items_i(1).valid, ~io.rob_items_i(0).valid)
    io.rob_item_commit_cnt := rob_item_commit_cnt
    io.rat_write_en := rat_write_en
    io.rat_write_addr := rat_write_addr
    io.rat_write_data := rat_write_data

    io.rat_flush_en := rat_flush_en
    io.rat_flush_pc := rat_flush_pc

    io.free_reg_id_valid := free_reg_id_valid
    io.free_reg_id_wdata := free_reg_id_wdata

    io.free_rob_id_valid := free_rob_id_valid
    io.free_rob_id_wdata := free_rob_id_wdata
}