package cpu.core.frontend

import chisel3._
import chisel3.util._

import cpu.config._
import upickle.default
import cpu.config.base.FETCH_WIDTH

/* 发送ROB项到ROB队列以及各个种类的发射保留站 */
/* 使用lut统计各种指令的数目, ALU固定4通道无需计算数量, 其他类型FU需要计算步长 */
/* 发射写入时需要判断写入哪个位置的结果 */
/* 更新新指令的ps1, ps2的ready情况，使用物理寄存器状态(历史)，总线状态(防止发射级需要读物理寄存器状态) */
/* 将对应pd的valid设置为false */
class Dispatch extends Module
{
    val agu_step = base.FETCH_WIDTH / base.AGU_NUM
    var io = IO(new Bundle {
        val rat_flush_en = Input(Bool())
        val rob_state = Input(Bool())
        val rob_item_i = Input(Vec(base.FETCH_WIDTH, new ROBItem))
        val inst_valid_cnt_i = Input(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))
        val alu_items_vec_o = Output(Vec(base.ALU_NUM, new ROBItem))
        val agu_items_vec_o = Output(
            Vec(base.AGU_NUM, Vec(agu_step, new ROBItem)))
        val agu_items_cnt_vec_o = Output(Vec(base.AGU_NUM, UInt((log2Ceil(agu_step) + 1).W)))
        /* 总线接口 */
        val cdb_i = Input(new CDB)
        /* PRF接口 */
        val prf_valid_rs1_ren = Output(Vec(base.FETCH_WIDTH, Bool()))
        val prf_valid_rs2_ren = Output(Vec(base.FETCH_WIDTH, Bool()))
        val prf_valid_rs1_raddr = Output(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))
        val prf_valid_rs2_raddr = Output(Vec(base.FETCH_WIDTH, UInt(base.PREG_WIDTH.W)))
        val prf_valid_rs1_rdata = Input(Vec(base.FETCH_WIDTH, Bool()))
        val prf_valid_rs2_rdata = Input(Vec(base.FETCH_WIDTH, Bool()))

        /* store buffer write */
        val store_buffer_write_en = Output(Bool())
        val store_buffer_item_o = Output(Vec(base.FETCH_WIDTH, new StoreBufferItem))
        val store_buffer_item_cnt = Output(UInt((log2Ceil(base.FETCH_WIDTH) + 1).W))

        val rob_item_o = Output(Vec(base.FETCH_WIDTH, new ROBItem))
        val inst_valid_cnt_o = Output(UInt(log2Ceil(base.FETCH_WIDTH + 1).W))
    })

    /* pipeline */
    var rob_item_reg = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new ROBItem))
    ))
    rob_item_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.rob_item_i, rob_item_reg), 
        VecInit(Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new ROBItem)))
    )

    var inst_valid_cnt_reg = RegInit((0.U)(log2Ceil(base.FETCH_WIDTH + 1).W))
    inst_valid_cnt_reg := Mux(
        ~io.rat_flush_en, 
        Mux(~io.rob_state, io.inst_valid_cnt_i, inst_valid_cnt_reg),
        0.U
    )

    /* 物理寄存器有效状态使能 */
    var prf_valid_rs1_ren = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var prf_valid_rs2_ren = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))    
    var prf_valid_rs1_raddr = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PREG_WIDTH.W))
    ))
    var prf_valid_rs2_raddr = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U)(base.PREG_WIDTH.W))
    ))

    for(i <- 0 until base.FETCH_WIDTH){
        prf_valid_rs1_ren(i) := rob_item_reg(i).HasRs1
        prf_valid_rs1_raddr(i) := rob_item_reg(i).ps1
        prf_valid_rs2_ren(i) := rob_item_reg(i).HasRs2
        prf_valid_rs2_raddr(i) := rob_item_reg(i).ps2
    }    

    /* 使用总线信号以及物理寄存器状态更新ROB项依赖状态 */
    var rob_items = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new ROBItem))
    ))
    var rob_items_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new ROBItem))
    ))
    rob_items := rob_item_reg
    rob_items_o := rob_item_reg

    var inst_valid_cnt_o = WireInit((0.U)((log2Ceil(base.FETCH_WIDTH) + 1).W))
    inst_valid_cnt_o := inst_valid_cnt_reg

    for(i <- 0 until base.FETCH_WIDTH){
        var rdy1_vec = WireInit(VecInit(
            Seq.fill(base.ALU_NUM + base.AGU_NUM + 1)(false.B)
        ))
        var rdy2_vec = WireInit(VecInit(
            Seq.fill(base.ALU_NUM + base.AGU_NUM + 1)(false.B)
        ))
        rdy1_vec(base.ALU_NUM + base.AGU_NUM) := io.prf_valid_rs1_rdata(i)
        rdy2_vec(base.ALU_NUM + base.AGU_NUM) := io.prf_valid_rs2_rdata(i)
        for(j <- 0 until base.ALU_NUM){
            rdy1_vec(j) := io.cdb_i.alu_channel(j).phy_reg_id === rob_item_reg(i).ps1
            rdy2_vec(j) := io.cdb_i.alu_channel(j).phy_reg_id === rob_item_reg(i).ps2
        }
        for(j <- 0 until base.AGU_NUM){
            rdy1_vec(j + base.ALU_NUM) := io.cdb_i.agu_channel(j).phy_reg_id === rob_item_reg(i).ps1
            rdy2_vec(j + base.ALU_NUM) := io.cdb_i.agu_channel(j).phy_reg_id === rob_item_reg(i).ps2
        }
        rob_items(i).rdy1 := rdy1_vec.asUInt.orR
        rob_items(i).rdy2 := rdy2_vec.asUInt.orR      
    }

    var is_alu_vec = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var is_agu_vec = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))

    for(i <- 0 until base.FETCH_WIDTH){
        is_alu_vec(i) := ~(rob_item_reg(i).Opcode === Opcode.SW)
        is_agu_vec(i) := rob_item_reg(i).Opcode === Opcode.SW
    }

    var alu_items_vec_o = WireInit(VecInit(
        Seq.fill(base.ALU_NUM)((0.U).asTypeOf(new ROBItem))
    ))
    
    for(i <- 0 until base.ALU_NUM){
        alu_items_vec_o(i) := Mux(is_alu_vec(i), rob_items(i), (0.U).asTypeOf(new ROBItem))
    }

    var agu_items_vec_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)(
            VecInit(Seq.fill(base.FETCH_WIDTH / base.AGU_NUM)((0.U).asTypeOf(new ROBItem)))
        )
    ))
    val agu_items_cnt_vec_o = WireInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)((log2Ceil(agu_step) + 1).W))
    ))
    for(i <- 0 until base.AGU_NUM){
        switch(is_agu_vec.asUInt(base.AGU_NUM * i + 1, base.AGU_NUM * i)){
            is("b00".U){}
            is("b01".U){
                agu_items_vec_o(i)(0) := rob_items(base.AGU_NUM * i)
                agu_items_cnt_vec_o(i) := 1.U
            }
            is("b10".U){
                agu_items_vec_o(i)(0) := rob_items(base.AGU_NUM * i + 1)
                agu_items_cnt_vec_o(i) := 1.U
            }
            is("b11".U){
                agu_items_vec_o(i)(0) := rob_items(base.AGU_NUM * i)
                agu_items_vec_o(i)(1) := rob_items(base.AGU_NUM * i + 1)
                agu_items_cnt_vec_o(i) := 2.U
            }
        }
    }

    /* store buffer */
    var store_flags = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))
    var store_buffer_item_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new StoreBufferItem))
    ))
    var store_buffer_item_cnt = WireInit((0.U)((log2Ceil(base.FETCH_WIDTH) + 1).W))
    var store_buffer_item_cnt_mid = WireInit(VecInit(
        Seq.fill(2)((0.U)(2.W))
    ))

    for(i <- 0 until 2){
        store_buffer_item_cnt_mid(i) := store_flags(2 * i) + store_flags(2 * i + 1) 
    }
    store_buffer_item_cnt := store_buffer_item_cnt_mid(0) + store_buffer_item_cnt_mid(1)

    for(i <- 0 until base.FETCH_WIDTH){
        store_flags(i) := 
            rob_item_reg(i).Opcode === Opcode.SW
    }

    dontTouch(store_buffer_item_o)
    switch(store_flags.asUInt){
        is("b0000".U){
        }
        is("b0001".U){
            store_buffer_item_o(0).rob_id := rob_item_reg(0).id
            store_buffer_item_o(0).valid  := rob_item_reg(0).valid
        }
        is("b0010".U){
            store_buffer_item_o(0).rob_id := rob_item_reg(1).id
            store_buffer_item_o(0).valid  := rob_item_reg(1).valid
        }
        is("b0100".U){
            store_buffer_item_o(0).rob_id := rob_item_reg(2).id
            store_buffer_item_o(0).valid  := rob_item_reg(2).valid
        }
        is("b1000".U){
            store_buffer_item_o(0).rob_id := rob_item_reg(3).id
            store_buffer_item_o(0).valid  := rob_item_reg(3).valid
        }
        is("b0011".U){
            store_buffer_item_o(0).rob_id := rob_item_reg(0).id
            store_buffer_item_o(0).valid  := rob_item_reg(0).valid
            store_buffer_item_o(1).rob_id := rob_item_reg(1).id
            store_buffer_item_o(1).valid  := rob_item_reg(1).valid            
        }
        is("b0101".U){
            store_buffer_item_o(0).rob_id := rob_item_reg(0).id
            store_buffer_item_o(0).valid  := rob_item_reg(0).valid
            store_buffer_item_o(1).rob_id := rob_item_reg(2).id
            store_buffer_item_o(1).valid  := rob_item_reg(2).valid            
        }   
        is("b1001".U){
            store_buffer_item_o(0).rob_id := rob_item_reg(0).id
            store_buffer_item_o(0).valid  := rob_item_reg(0).valid
            store_buffer_item_o(1).rob_id := rob_item_reg(3).id
            store_buffer_item_o(1).valid  := rob_item_reg(3).valid            
        } 
        is("b0110".U){
            store_buffer_item_o(0).rob_id := rob_item_reg(1).id
            store_buffer_item_o(0).valid  := rob_item_reg(1).valid
            store_buffer_item_o(1).rob_id := rob_item_reg(2).id
            store_buffer_item_o(1).valid  := rob_item_reg(2).valid            
        }   
        is("b1010".U){
            store_buffer_item_o(0).rob_id := rob_item_reg(1).id
            store_buffer_item_o(0).valid  := rob_item_reg(1).valid
            store_buffer_item_o(1).rob_id := rob_item_reg(3).id
            store_buffer_item_o(1).valid  := rob_item_reg(3).valid            
        }  
        is("b1100".U){
            store_buffer_item_o(0).rob_id := rob_item_reg(2).id
            store_buffer_item_o(0).valid  := rob_item_reg(2).valid
            store_buffer_item_o(1).rob_id := rob_item_reg(3).id
            store_buffer_item_o(1).valid  := rob_item_reg(3).valid            
        }   
        is("b0111".U){
            store_buffer_item_o(0).rob_id := rob_item_reg(0).id
            store_buffer_item_o(0).valid  := rob_item_reg(0).valid
            store_buffer_item_o(1).rob_id := rob_item_reg(1).id
            store_buffer_item_o(1).valid  := rob_item_reg(1).valid    
            store_buffer_item_o(2).rob_id := rob_item_reg(2).id
            store_buffer_item_o(2).valid  := rob_item_reg(2).valid         
        }
        is("b1011".U){
            store_buffer_item_o(0).rob_id := rob_item_reg(0).id
            store_buffer_item_o(0).valid  := rob_item_reg(0).valid
            store_buffer_item_o(1).rob_id := rob_item_reg(1).id
            store_buffer_item_o(1).valid  := rob_item_reg(1).valid    
            store_buffer_item_o(2).rob_id := rob_item_reg(3).id
            store_buffer_item_o(2).valid  := rob_item_reg(3).valid         
        }
        is("b1101".U){
            store_buffer_item_o(0).rob_id := rob_item_reg(0).id
            store_buffer_item_o(0).valid  := rob_item_reg(0).valid
            store_buffer_item_o(1).rob_id := rob_item_reg(2).id
            store_buffer_item_o(1).valid  := rob_item_reg(2).valid    
            store_buffer_item_o(2).rob_id := rob_item_reg(3).id
            store_buffer_item_o(2).valid  := rob_item_reg(3).valid         
        }
        is("b1110".U){
            store_buffer_item_o(0).rob_id := rob_item_reg(1).id
            store_buffer_item_o(0).valid  := rob_item_reg(1).valid
            store_buffer_item_o(1).rob_id := rob_item_reg(2).id
            store_buffer_item_o(1).valid  := rob_item_reg(2).valid    
            store_buffer_item_o(2).rob_id := rob_item_reg(3).id
            store_buffer_item_o(2).valid  := rob_item_reg(3).valid             
        }
        is("b1111".U){
            store_buffer_item_o(0).rob_id := rob_item_reg(0).id
            store_buffer_item_o(0).valid  := rob_item_reg(0).valid
            store_buffer_item_o(1).rob_id := rob_item_reg(1).id
            store_buffer_item_o(1).valid  := rob_item_reg(1).valid    
            store_buffer_item_o(2).rob_id := rob_item_reg(2).id
            store_buffer_item_o(2).valid  := rob_item_reg(2).valid
            store_buffer_item_o(3).rob_id := rob_item_reg(3).id
            store_buffer_item_o(3).valid  := rob_item_reg(3).valid             
        }
    }

    /* connect */
    io.agu_items_cnt_vec_o := agu_items_cnt_vec_o
    io.alu_items_vec_o := alu_items_vec_o
    io.agu_items_vec_o := agu_items_vec_o
    io.prf_valid_rs1_ren := prf_valid_rs1_ren
    io.prf_valid_rs1_raddr := prf_valid_rs1_raddr
    io.prf_valid_rs2_ren := prf_valid_rs2_ren
    io.prf_valid_rs2_raddr := prf_valid_rs2_raddr

    io.rob_item_o := rob_items_o
    io.inst_valid_cnt_o := inst_valid_cnt_o

    io.store_buffer_write_en := store_flags.asUInt.orR
    io.store_buffer_item_o   := store_buffer_item_o
    io.store_buffer_item_cnt := store_buffer_item_cnt
}