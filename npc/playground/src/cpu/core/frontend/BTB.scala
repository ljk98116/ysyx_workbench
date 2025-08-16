package cpu.core.frontend

import chisel3._
import chisel3.util._

import cpu.config._
import cpu.core.utils._

/* fetch段获取BTB值 */
/* decode段写入直接跳转的地址 */
/* EX段写入间接跳转的地址 */
class BTB(DEBUG: Boolean = false) extends Module{
    val io = IO(new Bundle{
        val rob_state = Input(UInt(2.W))
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

if(!DEBUG){
    val BTB_Mem = Mem(1 << 13, new BTBItem)
    var BTBItem_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new BTBItem))
    ))
    
    for(i <- 0 until base.FETCH_WIDTH){
        BTBItem_o(i) := BTB_Mem.read(io.pc_i(i)(9, 2))
    }

    for(i <- 0 until base.FETCH_WIDTH){
        io.btb_pred_addr_o(i) := Mux(
            (io.pc_i(i)(22, 15) === BTBItem_o(i).BIA) & BTBItem_o(i).V,
            BTBItem_o(i).BTA,
            io.pc_i(i) + 4.U
        )
        io.btb_hit_vec_o(i) := (io.pc_i(i)(17, 10) === BTBItem_o(i).BIA) & BTBItem_o(i).V
    }

    for(i <- 0 until base.FETCH_WIDTH){
        var btb_writeItem = WireInit((0.U).asTypeOf(new BTBItem))
        var index = WireInit((0.U)(13.W))
        /* 暂时性的地址检查 */
        when(io.ex_br_mask_i(i) & (io.rob_state =/= "b11".U) & (io.ex_br_addr(i)(31))){
            btb_writeItem.V := true.B
            btb_writeItem.BIA := io.ex_pc_i(i)(22, 15)
            btb_writeItem.BTA := io.ex_br_addr(i)
            index := io.ex_pc_i(i)(14, 2)
            BTB_Mem.write(index, btb_writeItem)
        }
    }

    for(i <- 0 until base.FETCH_WIDTH){
        var btb_writeItem = WireInit((0.U).asTypeOf(new BTBItem))
        var index = WireInit((0.U)(13.W))
        when(io.decode_br_mask_i(i) & (io.rob_state =/= "b11".U) & (io.decode_br_addr(i)(31))){
            btb_writeItem.V := true.B
            btb_writeItem.BIA := io.decode_pc_i(i)(22, 15)
            btb_writeItem.BTA := io.decode_br_addr(i)
            index := io.decode_pc_i(i)(14, 2)
            BTB_Mem.write(index, btb_writeItem)
        }
    }
}
else{
    var BTBItem_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new BTBItem))
    ))

    for(i <- 0 until base.FETCH_WIDTH){
        // BTBItem_o(i) := BTB_Mem.read(io.pc_i(i)(9, 2))
        var btb_read_v_api = Module(new BTBReadVAPI) 
        var btb_read_bia_api = Module(new BTBReadBIAAPI)
        var btb_read_bta_api = Module(new BTBReadBTAAPI)

        btb_read_v_api.io.rst := reset.asBool
        btb_read_bia_api.io.rst := reset.asBool
        btb_read_bta_api.io.rst := reset.asBool
        btb_read_v_api.io.ren := true.B
        btb_read_bia_api.io.ren := true.B
        btb_read_bta_api.io.ren := true.B

        btb_read_v_api.io.raddr := io.pc_i(i)(9, 2)
        btb_read_bia_api.io.raddr := io.pc_i(i)(9, 2)
        btb_read_bta_api.io.raddr := io.pc_i(i)(9, 2)

        BTBItem_o(i).V := btb_read_v_api.io.V
        BTBItem_o(i).BIA := btb_read_bia_api.io.BIA
        BTBItem_o(i).BTA := btb_read_bta_api.io.BTA
    }

    for(i <- 0 until base.FETCH_WIDTH){
        io.btb_pred_addr_o(i) := Mux(
            (io.pc_i(i)(22, 15) === BTBItem_o(i).BIA) & BTBItem_o(i).V,
            BTBItem_o(i).BTA,
            io.pc_i(i) + 4.U
        )
        io.btb_hit_vec_o(i) := (io.pc_i(i)(17, 10) === BTBItem_o(i).BIA) & BTBItem_o(i).V
    }

    for(i <- 0 until base.FETCH_WIDTH){
        // var btb_writeItem = WireInit((0.U).asTypeOf(new BTBItem))
        // var index = WireInit((0.U)(13.W))
        // when(io.ex_br_mask_i(i) & (io.rob_state =/= "b11".U)){
        //     btb_writeItem.V := true.B
        //     btb_writeItem.BIA := io.ex_pc_i(i)(22, 15)
        //     btb_writeItem.BTA := io.ex_br_addr(i)
        //     index := io.ex_pc_i(i)(14, 2)
        //     BTB_Mem.write(index, btb_writeItem)
        // }
        
        var btb_write_api = Module(new BTBWriteAPI) 
        btb_write_api.io.clk := clock.asBool
        btb_write_api.io.rst := reset.asBool
        btb_write_api.io.wen := io.ex_br_mask_i(i) & (io.rob_state === "b00".U) & (io.ex_br_addr(i)(31))
        btb_write_api.io.waddr := io.ex_pc_i(i)(14, 2)
        btb_write_api.io.V := true.B
        btb_write_api.io.BIA := io.ex_pc_i(i)(22, 15)
        btb_write_api.io.BTA := io.ex_br_addr(i)
    }      

    for(i <- 0 until base.FETCH_WIDTH){
        // var btb_writeItem = WireInit((0.U).asTypeOf(new BTBItem))
        // var index = WireInit((0.U)(13.W))
        // when(io.ex_br_mask_i(i) & (io.rob_state =/= "b11".U)){
        //     btb_writeItem.V := true.B
        //     btb_writeItem.BIA := io.ex_pc_i(i)(22, 15)
        //     btb_writeItem.BTA := io.ex_br_addr(i)
        //     index := io.ex_pc_i(i)(14, 2)
        //     BTB_Mem.write(index, btb_writeItem)
        // }
        
        var btb_write_api = Module(new BTBWriteAPI) 
        btb_write_api.io.clk := clock.asBool
        btb_write_api.io.rst := reset.asBool
        btb_write_api.io.wen := io.decode_br_mask_i(i) & (io.rob_state === "b00".U) & (io.decode_br_addr(i)(31))
        btb_write_api.io.waddr := io.decode_pc_i(i)(14, 2)
        btb_write_api.io.V := true.B
        btb_write_api.io.BIA := io.decode_pc_i(i)(22, 15)
        btb_write_api.io.BTA := io.decode_br_addr(i)
    }

}

}