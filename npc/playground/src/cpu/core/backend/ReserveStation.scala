package cpu.core.backend

import chisel3._
import chisel3.util._

import cpu.config._

/* 指定step长度和队列大小 */
/* 需要根据ROBID找到对应的指令位置, 使用额外的Mem */
class ReserveStation(stepsize : Int, size: Int) extends Module {
    val width = log2Ceil(size)
    val stepwidth = log2Ceil(stepsize)
    val io = IO(new Bundle {
        var rob_item_i = Input(Vec(stepsize, new ROBItem))
        var valid_cnt_i = Input(UInt((log2Ceil(stepsize) + 1).W))
        var cdb_i = Input(new CDB)
        var rob_item_o = Output(new ROBItem)
        var write_able = Bool()
        var read_able = Bool()
    })

    /* 记录保留站每个位置的指令能否发射 */
    var issue_able_vec = WireInit(VecInit(
        Seq.fill(size)(false.B)
    ))

    var ROBItemMem = RegInit(VecInit(
        Seq.fill(size)((0.U).asTypeOf(new ROBItem))
    ))
    var ROBID2LocMem = RegInit(VecInit(
        Seq.fill(1 << base.ROBID_WIDTH)((0.U)(width.W))
    ))

    var head = RegInit((0.U)(width.W))
    var tail = RegInit((0.U)(width.W))

    io.read_able := head + 1.U <= tail
    io.write_able := tail + (stepsize + 1).U < head

    head := Mux(io.read_able & issue_able_vec.asUInt.orR, head + 1.U, head)
    tail := Mux(io.write_able, tail + io.valid_cnt_i, tail)

    var rob_valid_vec = WireInit(VecInit(
        Seq.fill(stepsize)(false.B)
    ))

    for(i <- 0 until stepsize){
        rob_valid_vec(i) := io.rob_item_i(i).valid
    }

    /* 根据valid标志位维护查找表 */
    /* 计算有效、无效项对应的尾部插入的偏移量 */
    var valid_idx_mapping = VecInit(
        Seq.tabulate(1 << stepsize)((i) => {
            var init = Array.fill(stepsize)(stepsize)
            var cnt = 0
            var idx = 0
            var n = i
            while(n > 0){
                if((n & 1) != 0) {
                    init(cnt) = idx
                    idx += 1
                }
                cnt += 1
                n = n >>> 1
            }
            var ret = VecInit(Seq.tabulate(stepsize)((j) => {
                (init(j).U)((stepwidth + 1).W)
            }))
            ret
        })
    )
    /* 通过查找表得到当前进入保留站的所有指令对应的尾部插入的偏移 */
    var insert_off_vec = WireInit(VecInit(
        Seq.fill(stepsize)(stepsize.U((stepwidth + 1).W))
    ))
    insert_off_vec := valid_idx_mapping(rob_valid_vec.asUInt)
    
    /* 加入保留站队列 */
    for(i <- 0 until stepsize){
        when(io.rob_item_i(i).valid){
            ROBItemMem(tail + insert_off_vec(i)) := io.rob_item_i(i) //时序逻辑
            ROBID2LocMem(io.rob_item_i(i).id) := tail + insert_off_vec(i)
        }
    }

    /* 检查可以发射的指令 */
    var rob_item_o = WireInit((0.U).asTypeOf(new ROBItem))


    /* 逐个指令检查，获取是否可发射, 并更新状态 */
    /* 1. 找到该发射项对应的有效的channel */
    /* 2. 更新该发射项的值，更新发射项 */
    /* 如何取出发射项? 目前优先考虑无效化该位置ROB项 */
    var rob_items = WireInit(VecInit(
        Seq.fill(size)((0.U).asTypeOf(new ROBItem))
    ))
    var rob_item_valid_vec = WireInit(VecInit(
        Seq.fill(size)(false.B)
    ))
    /* 每一项4级逻辑 */
    for(i <- 0 until size){
        var target_channel_mask_rs1 = WireInit(VecInit(
            Seq.fill(base.ALU_NUM + base.AGU_NUM)(false.B)
        ))
        var target_channel_mask_rs2 = WireInit(VecInit(
            Seq.fill(base.ALU_NUM + base.AGU_NUM)(false.B)
        ))
        /* 1st logic */
        rob_item_valid_vec(i) := Mux(head < tail, i.U >= head & i.U <= tail, i.U >= head | i.U <= tail)
        rob_items(i) := ROBItemMem(i)
        /* 1st, 2nd logic */
        for(j <- 0 until base.ALU_NUM){
            target_channel_mask_rs1(j) := 
                io.cdb_i.alu_channel(j).valid &
                (io.cdb_i.alu_channel(j).arch_reg_id === ROBItemMem(i).rs1) &
                (io.cdb_i.alu_channel(j).phy_reg_id === ROBItemMem(i).ps1) &
                (io.cdb_i.alu_channel(j).rob_id === ROBItemMem(i).id) & ROBItemMem(i).HasRs1
            target_channel_mask_rs2(j) := 
                io.cdb_i.alu_channel(j).valid &
                (io.cdb_i.alu_channel(j).arch_reg_id === ROBItemMem(i).rs2) &
                (io.cdb_i.alu_channel(j).phy_reg_id === ROBItemMem(i).ps2) &
                (io.cdb_i.alu_channel(j).rob_id === ROBItemMem(i).id) & ROBItemMem(i).HasRs2
        }

        for(j <- 0 until base.AGU_NUM){
            target_channel_mask_rs1(j + base.ALU_NUM) := 
                io.cdb_i.agu_channel(j).valid &
                (io.cdb_i.agu_channel(j).arch_reg_id === ROBItemMem(i).rs1) &
                (io.cdb_i.agu_channel(j).phy_reg_id === ROBItemMem(i).ps1) &
                (io.cdb_i.agu_channel(j).rob_id === ROBItemMem(i).id) & ROBItemMem(i).HasRs1
            target_channel_mask_rs2(j + base.ALU_NUM) := 
                io.cdb_i.agu_channel(j).valid &
                (io.cdb_i.agu_channel(j).arch_reg_id === ROBItemMem(i).rs2) &
                (io.cdb_i.agu_channel(j).phy_reg_id === ROBItemMem(i).ps2) &
                (io.cdb_i.agu_channel(j).rob_id === ROBItemMem(i).id) & ROBItemMem(i).HasRs2
        }
        /* 3rd logic */
        rob_items(i).rdy1 := target_channel_mask_rs1.asUInt.orR
        rob_items(i).rdy2 := target_channel_mask_rs2.asUInt.orR

        /* 4, 5th logic */
        /* 是否可以发射该项 */
        issue_able_vec(i) := ~((ROBItemMem(i).HasRs1 & ~rob_items(i).rdy1) | (ROBItemMem(i).HasRs2 & ~rob_items(i).rdy2))
    }
    /* 根据head，tail大小关系决定如何发射 */
    var target_issue_idx = WireInit((0.U)(width.W))
    /* chisel PriorityEncoder效率极低，需要自行实现 */
    // target_issue_idx := PriorityEncoder(issue_able_vec)

    io.rob_item_o := rob_items(target_issue_idx)
    rob_items(target_issue_idx).valid := false.B

    /* 更新所有ROBItem状态 */
    for(i <- 0 until size){
        when(rob_item_valid_vec(i)){
            ROBItemMem(i) := rob_items(i)
        }
    }

}