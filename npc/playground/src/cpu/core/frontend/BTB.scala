package cpu.core.frontend

import chisel3._
import chisel3.util._

import cpu.config._

/* fetch段获取BTB值 */
/* decode段写入直接跳转的地址 */
/* EX段写入间接跳转的地址 */
class BTB extends Module{
    val io = IO(new Bundle{
        val rob_state = Input(Bool())
        val pc_i = Input(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val decode_br_mask_i = Input(Vec(base.FETCH_WIDTH, Bool()))
        val decode_pc_i = Input(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val decode_br_addr = Input(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val ex_br_mask_i = Input(Vec(base.FETCH_WIDTH, Bool()))
        val ex_pc_i = Input(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val ex_br_addr = Input(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
        val btb_hit_vec_o = Output(Vec(base.FETCH_WIDTH, Bool()))
        val btb_pred_addr_o = Output(Vec(base.FETCH_WIDTH, UInt(base.ADDR_WIDTH.W)))
    })

    val BTB_Mem = Mem(1 << 13, new BTBItem)
    var BTBItem_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new BTBItem))
    ))
    
    for(i <- 0 until base.FETCH_WIDTH){
        BTBItem_o(i) := BTB_Mem.read(io.pc_i(i)(9, 2))
    }

    for(i <- 0 until base.FETCH_WIDTH){
        io.btb_pred_addr_o(i) := Mux(
            (io.pc_i(i)(17, 10) === BTBItem_o(i).BIA) & BTBItem_o(i).V,
            BTBItem_o(i).BTA,
            io.pc_i(i) + 4.U
        )
        io.btb_hit_vec_o(i) := (io.pc_i(i)(17, 10) === BTBItem_o(i).BIA) & BTBItem_o(i).V
    }

    for(i <- 0 until base.FETCH_WIDTH){
        var btb_writeItem = WireInit((0.U).asTypeOf(new BTBItem))
        var index = WireInit((0.U)(8.W))
        when(io.ex_br_mask_i(i) & ~io.rob_state){
            btb_writeItem.V := true.B
            btb_writeItem.BIA := io.ex_pc_i(i)(17, 10)
            btb_writeItem.BTA := io.ex_br_addr(i)
            index := io.ex_pc_i(i)(9, 2)
        }
        BTB_Mem.write(index, btb_writeItem)
    }

    for(i <- 0 until base.FETCH_WIDTH){
        var btb_writeItem = WireInit((0.U).asTypeOf(new BTBItem))
        var index = WireInit((0.U)(8.W))
        when(io.decode_br_mask_i(i) & ~io.rob_state){
            btb_writeItem.V := true.B
            btb_writeItem.BIA := io.decode_pc_i(i)(17, 10)
            btb_writeItem.BTA := io.decode_br_addr(i)
            index := io.decode_pc_i(i)(9, 2)
        }
        BTB_Mem.write(index, btb_writeItem)
    }
}