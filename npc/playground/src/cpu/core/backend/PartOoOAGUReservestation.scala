package cpu.core.backend

import chisel3._
import chisel3.util._

import cpu.config._
import cpu.core.utils._

class AGUReserveFreeIdBuffer(size: Int) extends Module{
    var width = log2Ceil(size)
    val io = IO(new Bundle{
        /* issue stage */
        val valid_cnt = Input(UInt((log2Ceil(base.FETCH_WIDTH) + 1).W))
        val rat_flush_en = Input(Bool())
        val issued_i = Input(UInt(base.AGU_NUM.W))
        val issued_id_i = Input(Vec(base.AGU_NUM, UInt(width.W)))
        /* output */
        val free_id_o = Output(Vec(base.FETCH_WIDTH, UInt((width + 1).W))) 
        val rd_able = Output(Bool())
        val wr_able = Output(Bool())        
    })

    var IDRegFile = RegInit(VecInit(
        Seq.tabulate(size)((i) => {i.U})
    ))

    var head = RegInit((0.U)(width.W))
    var tail = RegInit(((size - 1).U)(width.W))

    io.rd_able := 
        (head =/= tail) & 
        (head + 1.U =/= tail) &
        (head + 2.U =/= tail) &
        (head + 3.U =/= tail)
    
    io.wr_able := 
        ((tail + 1.U) =/= head) &
        ((tail + 2.U) =/= head)

    for(i <- 0 until size){
        when(~io.rat_flush_en & (i.U === tail) & io.issued_i(0)){
            IDRegFile(i) := io.issued_id_i(0)
        }.elsewhen(~io.rat_flush_en & (i.U === (tail + 1.U)) & io.issued_i(1)){
            IDRegFile(i) := io.issued_id_i(1)
        }.elsewhen(io.rat_flush_en){
            IDRegFile(i) := i.U
        }
    }  

    var free_id_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((size.U)((width + 1).W))
    ))
    for(i <- 0 until base.FETCH_WIDTH){
        free_id_o(i) := IDRegFile(head + i.U)
    }


    head := Mux(
        io.rd_able, 
        Mux(
            ~io.rat_flush_en, 
            head + io.valid_cnt, 
            0.U
        ), 
        head
    )
    tail := Mux(
        io.wr_able & ~io.rat_flush_en, 
        tail + Mux(io.issued_i.andR, base.AGU_NUM.U, io.issued_i.orR), 
        Mux(io.rat_flush_en, (size - 1).U, tail)
    )

    /* connect */
    io.free_id_o := free_id_o  
}

class PartOoOAGUReservestation(size : Int) extends Module
{
    val width = log2Ceil(size)
    val io = IO(new Bundle{
        val rat_flush_en = Input(Bool())
        val flush_store_idx = Input(UInt((base.ROBID_WIDTH + 1).W))
        val rob_item_i = Input(Vec(base.FETCH_WIDTH, new ROBItem))
        val valid_cnt_i = Input(UInt((log2Ceil(base.FETCH_WIDTH) + 1).W))
        /* 总线状态 */
        val cdb_i = Input(new CDB)
        val rob_item_o = Output(Vec(base.AGU_NUM, new ROBItem))
        val read_able = Output(Bool())
        val write_able = Output(Bool())
    })

    var rob_item_reg = RegInit(VecInit(
        Seq.fill(size)((0.U).asTypeOf(new ROBItem))
    ))   

    val freeIdBuffer = Module(new AGUReserveFreeIdBuffer(size))

    io.read_able := freeIdBuffer.io.rd_able
    io.write_able := freeIdBuffer.io.wr_able

    /* age matrix */
    var age_mat = RegInit(VecInit(
        Seq.fill(size)(
            VecInit(Seq.fill(size)(false.B))
        )
    ))

    /* 已经发射的Store指令的ROBID */
    var store_robIdx = RegInit(((1 << base.ROBID_WIDTH).U)((base.ROBID_WIDTH + 1).W))

    /* search issue able insts */
    /* 需要rs1且rs1 ready，需要rs2且rs2 ready */
    var issue_able_vec = WireInit(VecInit(
        Seq.fill(size)(false.B)
    ))

    /* 如果是load指令,上一条store指令必须已经发射而且操作数rdy */
    /* 如果是store指令，操作数rdy即可发射 */
    for(i <- 0 until size){
        issue_able_vec(i) := ~(
            (rob_item_reg(i).HasRs1 & ~rob_item_reg(i).rdy1) | 
            (rob_item_reg(i).HasRs2 & ~rob_item_reg(i).rdy2)
        ) & (
            (
                (
                    (
                        (store_robIdx === rob_item_reg(i).storeIdx) & 
                        ~store_robIdx(base.ROBID_WIDTH)
                    ) 
                    | store_robIdx(base.ROBID_WIDTH)
                ) & 
                rob_item_reg(i).isLoad
            ) |
            rob_item_reg(i).isStore
        )
    }

    /* 能发射且比其他能发射的矩阵年长 */
    var issue_oh_vec0 = WireInit(VecInit(
        Seq.fill(size)(false.B)
    ))
    for(i <- 0 until size){
        var issue_loc_mask0 = WireInit(VecInit(
            Seq.fill(size)(true.B)
        ))
        /* 对于每个能发射的位置i必须比j年长或者j位置无效，要么不能发射 */
        /* 不能发射同样满足条件 */
        for(j <- 0 until size){
            if(j != i){
                issue_loc_mask0(j) := ~issue_able_vec(j) | (issue_able_vec(j) & age_mat(i)(j))
            }
        }
        issue_loc_mask0(i) := true.B
        issue_oh_vec0(i) := issue_able_vec(i) & issue_loc_mask0.asUInt.andR
    }
    
    var issue_idx0 = WireInit((size.U)((log2Ceil(size) + 1).W))
    issue_idx0 := Mux(~issue_oh_vec0.asUInt.orR, size.U, OHToUInt(issue_oh_vec0.asUInt))
    io.rob_item_o(0) := Mux(issue_idx0 =/= size.U, rob_item_reg(issue_idx0(log2Ceil(size) - 1, 0)), (0.U).asTypeOf(new ROBItem))    

    /* 能发射且比其他能发射的矩阵年长 */
    var issue_oh_vec1 = WireInit(VecInit(
        Seq.fill(size)(false.B)
    ))
    for(i <- 0 until size){
        when(i.U =/= issue_idx0){
            var issue_loc_mask1 = WireInit(VecInit(
                Seq.fill(size)(true.B)
            ))
            /* 对于每个能发射的位置i必须比j年长或者j位置无效，要么不能发射 */
            /* 不能发射同样满足条件 */
            for(j <- 0 until size){
                if(j != i){
                    issue_loc_mask1(j) := ~issue_able_vec(j) | (issue_able_vec(j) & age_mat(i)(j))
                }
            }
            issue_loc_mask1(i) := true.B
            issue_oh_vec1(i) := issue_able_vec(i) & issue_loc_mask1.asUInt.andR
        }.otherwise{
            issue_oh_vec1(i) := false.B
        }
    }
    
    var issue_idx1 = WireInit((size.U)((log2Ceil(size) + 1).W))
    issue_idx1 := Mux(~issue_oh_vec1.asUInt.orR, size.U, OHToUInt(issue_oh_vec1.asUInt))
    io.rob_item_o(1) := Mux(issue_idx1 =/= size.U, rob_item_reg(issue_idx1(log2Ceil(size) - 1, 0)), (0.U).asTypeOf(new ROBItem))       

    /* 回收issue_idx */
    freeIdBuffer.io.rat_flush_en := io.rat_flush_en
    freeIdBuffer.io.issued_i := 
        Cat(
            issue_oh_vec1.asUInt.orR & ~io.rat_flush_en, 
            issue_oh_vec0.asUInt.orR & ~io.rat_flush_en
        )
    freeIdBuffer.io.issued_id_i(0) := issue_idx0(log2Ceil(size) - 1, 0)
    freeIdBuffer.io.issued_id_i(1) := issue_idx1(log2Ceil(size) - 1, 0)      
    freeIdBuffer.io.valid_cnt := io.valid_cnt_i

    /* 使用总线消息更新发射队列进入项 */
    var rob_item_i_update = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new ROBItem))
    ))
    /* 2 stage logic */
    rob_item_i_update := io.rob_item_i
    for(j <- 0 until base.FETCH_WIDTH){
        var rdy1_vec = WireInit(VecInit(
            Seq.fill(base.ALU_NUM + base.AGU_NUM)(false.B)
        ))
        var rdy2_vec = WireInit(VecInit(
            Seq.fill(base.ALU_NUM + base.AGU_NUM)(false.B)
        ))
        for(i <- 0 until base.ALU_NUM){
            rdy1_vec(i) := (io.cdb_i.alu_channel(i).phy_reg_id === io.rob_item_i(j).ps1) & io.cdb_i.alu_channel(i).valid
            rdy2_vec(i) := (io.cdb_i.alu_channel(i).phy_reg_id === io.rob_item_i(j).ps2) & io.cdb_i.alu_channel(i).valid
        }

        for(i <- 0 until base.AGU_NUM){
            rdy1_vec(i + base.ALU_NUM) := (io.cdb_i.agu_channel(i).phy_reg_id === io.rob_item_i(j).ps1) & io.cdb_i.agu_channel(i).valid
            rdy2_vec(i + base.ALU_NUM) := (io.cdb_i.agu_channel(i).phy_reg_id === io.rob_item_i(j).ps2) & io.cdb_i.agu_channel(i).valid 
        }    
        rob_item_i_update(j).rdy1 := rdy1_vec.asUInt.orR | io.rob_item_i(j).rdy1
        rob_item_i_update(j).rdy2 := rdy2_vec.asUInt.orR | io.rob_item_i(j).rdy2        
    }

    /* update issue rob regs */
    /* update age matrix, age[i]为0表示当前ID比其他位置都年轻*/
    /* 新分配的reg更新年龄矩阵 */
    /* 效率较低，需要后期优化时序 */

    for(i <- 0 until size){
        var free_mask = WireInit(VecInit(
            Seq.fill(base.FETCH_WIDTH)(false.B)
        ))
        dontTouch(free_mask)
        for(j <- 0 until base.FETCH_WIDTH){
            free_mask(j) := 
                (i.U.asTypeOf(UInt((width + 1).W)) === freeIdBuffer.io.free_id_o(j)) & 
                io.rob_item_i(j).valid
        }
        var wr_idx = WireInit((base.FETCH_WIDTH.U)((log2Ceil(base.FETCH_WIDTH) + 1).W))
        wr_idx := Mux(free_mask.asUInt.orR, OHToUInt(free_mask.asUInt), base.FETCH_WIDTH.U)
        when(~io.rat_flush_en & free_mask.asUInt.orR){
            var rob_item_i_update_mid = WireInit((0.U).asTypeOf(new ROBItem))
            rob_item_i_update_mid := Mux(
                    wr_idx(log2Ceil(base.FETCH_WIDTH) - 1, 0)(1),
                    Mux(
                        wr_idx(log2Ceil(base.FETCH_WIDTH) - 1, 0)(0),
                        rob_item_i_update(3),
                        rob_item_i_update(2)
                    ),
                    Mux(
                        wr_idx(log2Ceil(base.FETCH_WIDTH) - 1, 0)(0),
                        rob_item_i_update(1),
                        rob_item_i_update(0)
                    )                    
                )
            rob_item_reg(i) := rob_item_i_update_mid
            /* 有效的项比新写入的项要老 */
            for(j <- 0 until size){
                if(i != j){
                    age_mat(j)(i) := rob_item_i_update_mid.valid
                }
                age_mat(i)(j) := false.B
            }
        }.elsewhen(
            ~io.rat_flush_en & 
            (~free_mask.asUInt.orR) & 
            rob_item_reg(i).valid & 
            (i.U.asTypeOf(UInt((width + 1).W)) =/= issue_idx0) &
            (i.U.asTypeOf(UInt((width + 1).W)) =/= issue_idx1)
        ){
            var rob_item = WireInit((0.U).asTypeOf(new ROBItem))
            var rdy1_vec = WireInit(VecInit(
                Seq.fill(base.ALU_NUM + base.AGU_NUM)(false.B)
            ))
            var rdy2_vec = WireInit(VecInit(
                Seq.fill(base.ALU_NUM + base.AGU_NUM)(false.B)
            ))
            for(j <- 0 until base.ALU_NUM){
                rdy1_vec(j) := (io.cdb_i.alu_channel(j).phy_reg_id === rob_item_reg(i).ps1) & io.cdb_i.alu_channel(j).valid
                rdy2_vec(j) := (io.cdb_i.alu_channel(j).phy_reg_id === rob_item_reg(i).ps2) & io.cdb_i.alu_channel(j).valid
            }
            for(j <- 0 until base.AGU_NUM){
                rdy1_vec(j + base.ALU_NUM) := (io.cdb_i.agu_channel(j).phy_reg_id === rob_item_reg(i).ps1) & io.cdb_i.agu_channel(j).valid
                rdy2_vec(j + base.ALU_NUM) := (io.cdb_i.agu_channel(j).phy_reg_id === rob_item_reg(i).ps2) & io.cdb_i.agu_channel(j).valid
            }
            rob_item_reg(i).rdy1 := rdy1_vec.asUInt.orR | rob_item_reg(i).rdy1
            rob_item_reg(i).rdy2 := rdy2_vec.asUInt.orR | rob_item_reg(i).rdy2
        }.elsewhen(io.rat_flush_en){
            rob_item_reg(i) := 0.U.asTypeOf(new ROBItem)
            for(j <- 0 until size){
                age_mat(i)(j) := false.B
            }
        }
    }    

    store_robIdx := Mux(
        io.rat_flush_en, 
        io.flush_store_idx,
        Mux(
            issue_oh_vec1.asUInt.orR & io.rob_item_o(1).isStore, 
            io.rob_item_o(1).id,
            Mux(
                issue_oh_vec0.asUInt.orR & io.rob_item_o(0).isStore,
                io.rob_item_o(0).id,
                store_robIdx
            )
        )
    )


    when(~issue_idx0(width)){
        for(j <- 0 until size){
            age_mat(issue_idx0(log2Ceil(size) - 1, 0))(j) := false.B
            age_mat(j)(issue_idx0(log2Ceil(size) - 1, 0)) := rob_item_reg(j).valid
        }
        rob_item_reg(issue_idx0(log2Ceil(size) - 1, 0)) := 0.U.asTypeOf(new ROBItem)
    }

    when(~issue_idx1(width)){
        for(j <- 0 until size){
            age_mat(issue_idx1(log2Ceil(size) - 1, 0))(j) := false.B
            age_mat(j)(issue_idx1(log2Ceil(size) - 1, 0)) := rob_item_reg(j).valid
        }
        rob_item_reg(issue_idx1(log2Ceil(size) - 1, 0)) := 0.U.asTypeOf(new ROBItem)
    }

}