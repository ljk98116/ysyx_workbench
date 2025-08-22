package cpu.core

import chisel3._
import chisel3.util._

import cpu.config._
import cpu.core.frontend._
import cpu.core.utils._

/* 监听总线，看是否就绪 */
/* 状态机刷新掉head->tail之间的部分,必须是状态机来恢复freereg状态，避免与RAT撞车 */
/* flush过程需要暂停整个CPU */
class ROB (DEBUG : Boolean = false) extends Module
{
    val io = IO(new Bundle {
        var rat_flush_en = Input(Bool())
        var retire_rdy_mask = Input(UInt(base.FETCH_WIDTH.W))
        var rob_state = Output(UInt(2.W))
        val rob_item_i = Input(Vec(base.FETCH_WIDTH, new ROBItem))
        /* 输出头部的4条指令,用来提交或者恢复现场 */
        val rob_item_o = Output(Vec(base.FETCH_WIDTH, new ROBItem))
        /* 核内总线消息 */
        val cdb_i = Input(new CDB)
        /* store消息 */
        val agu_valid = Input(Vec(base.AGU_NUM, Bool()))
        val agu_rob_id = Input(Vec(base.AGU_NUM, UInt(base.ROBID_WIDTH.W)))
        val agu_result = Input(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val agu_wdata = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
        val agu_wmask = Input(Vec(base.AGU_NUM, UInt(8.W)))
        val agu_ls_flag = Input(Vec(base.AGU_NUM, Bool()))
        /* rob容量信息 */
        val robw_able = Output(Bool())
        val robr_able = Output(Bool())
    })

    val bankcap = (1 << base.ROBID_WIDTH) >> 2
    val bankwidth = log2Ceil(bankcap)
    val normal::wait1::wait2::flush::Nil = Enum(4)
    var rob_state = RegInit(normal)
    var next_rob_state = WireInit(normal)

    var head = RegInit((0.U)(bankwidth.W))
    var tail = RegInit((0.U)(bankwidth.W))

    var rob_item_o = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)((0.U).asTypeOf(new ROBItem))
    ))
    io.robr_able := head =/= tail
    io.robw_able := (tail + 1.U) =/= head

    var rob_input_valid = WireInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(false.B)
    ))



    /* Retire出现异常，normal->flush */
    /* head + 1.U == tail, flush->normal */
    when((rob_state === normal) & io.rat_flush_en){
        next_rob_state := wait1
    }.elsewhen(rob_state === wait1){
        next_rob_state := wait2
    }.elsewhen(rob_state === wait2){
        next_rob_state := flush
    }.elsewhen((rob_state === flush) & ((head + 1.U) === tail) | (head === tail)){
        next_rob_state := normal
    }.otherwise{
        next_rob_state := rob_state
    }

    rob_state := next_rob_state

    when(io.robw_able & rob_input_valid.asUInt.orR & (rob_state =/= flush)){
        tail := tail + 1.U
    }.elsewhen((io.robr_able & (rob_state === flush) & (head =/= tail))){
        tail := tail - 1.U
    }

    when(
        (io.robr_able & io.retire_rdy_mask.andR & ~(rob_state === flush))
    ){
        head := head + 1.U
    }

    /* connect */
    io.rob_item_o := rob_item_o
    io.rob_state  := rob_state
if(!DEBUG){
    var ROBBankRegs = RegInit(VecInit(
        Seq.fill(base.FETCH_WIDTH)(
            VecInit(Seq.fill(bankcap)(0.U.asTypeOf(new ROBItem)))
        ))
    )
    var ROBIDLocMem = RegInit(VecInit(
        Seq.fill(1 << base.ROBID_WIDTH)(((1 << bankwidth).U)((bankwidth + 1).W))
    ))
    dontTouch(ROBBankRegs)
    for(i <- 0 until base.FETCH_WIDTH){
        when(io.rob_state === flush){
            ROBBankRegs(i)(tail - 1.U) := 0.U.asTypeOf(new ROBItem)
            ROBIDLocMem(ROBBankRegs(i)(tail - 1.U).id) := (1 << bankwidth).U 
        }
    }

    for(i <- 0 until base.FETCH_WIDTH){
        when(io.robr_able & io.retire_rdy_mask.andR & ~(rob_state === flush) & ROBBankRegs(i)(head).valid){
            ROBBankRegs(i)(head) := 0.U.asTypeOf(new ROBItem)
            ROBIDLocMem(ROBBankRegs(i)(head).id) := (1 << bankwidth).U             
        }
    }

    for(i <- 0 until base.FETCH_WIDTH){
        rob_input_valid(i) := io.rob_item_i(i).valid
        rob_item_o(i) := Mux(
            io.robr_able & (rob_state =/= flush), 
            ROBBankRegs(i)(head), 
            Mux(
                io.robr_able & (rob_state === flush),
                ROBBankRegs(i)(tail - 1.U),
                0.U.asTypeOf(new ROBItem)
            )
        )
    }

    for(i <- 0 until base.ALU_NUM){
        when(io.cdb_i.alu_channel(i).valid & (io.rob_state =/= "b11".U) & ~io.rat_flush_en & ~ROBIDLocMem(io.cdb_i.alu_channel(i).rob_id)(bankwidth)){
            ROBBankRegs(io.cdb_i.alu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.alu_channel(i).rob_id)(bankwidth - 1, 0)).rdy := true.B
            ROBBankRegs(io.cdb_i.alu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.alu_channel(i).rob_id)(bankwidth - 1, 0)).reg_wb_data := io.cdb_i.alu_channel(i).reg_wr_data
            ROBBankRegs(io.cdb_i.alu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.alu_channel(i).rob_id)(bankwidth - 1, 0)).targetBrAddr := io.cdb_i.alu_channel(i).branch_target_addr
            ROBBankRegs(io.cdb_i.alu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.alu_channel(i).rob_id)(bankwidth - 1, 0)).hasException := io.cdb_i.alu_channel(i).has_exception
            ROBBankRegs(io.cdb_i.alu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.alu_channel(i).rob_id)(bankwidth - 1, 0)).ExceptionType := io.cdb_i.alu_channel(i).exception_type
        }
    }
    for(i <- 0 until base.AGU_NUM){
        when(io.cdb_i.agu_channel(i).valid & (io.rob_state =/= "b11".U) & ~io.rat_flush_en & ~ROBIDLocMem(io.cdb_i.agu_channel(i).rob_id)(bankwidth)){
            ROBBankRegs(io.cdb_i.agu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.agu_channel(i).rob_id)(bankwidth - 1, 0)).rdy := true.B
            ROBBankRegs(io.cdb_i.agu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.agu_channel(i).rob_id)(bankwidth - 1, 0)).reg_wb_data := io.cdb_i.agu_channel(i).reg_wr_data
        }
        when(io.agu_valid(i) & io.agu_ls_flag(i) & (io.rob_state =/= "b11".U) & ~io.rat_flush_en & ~ROBIDLocMem(io.agu_rob_id(i))(bankwidth)){
            ROBBankRegs(io.agu_rob_id(i)(bankwidth + 1, bankwidth))(ROBIDLocMem(io.agu_rob_id(i))(bankwidth - 1, 0)).rdy := true.B          
        }
    }

    for(i <- 0 until base.FETCH_WIDTH){
        when(io.robw_able & io.rob_item_i(i).valid){
            ROBBankRegs(i)(tail) := io.rob_item_i(i)
            ROBIDLocMem(io.rob_item_i(i).id) := tail
        }
    }
}else{
    val rob_elems = Seq(
        ("pc", 32), 
        ("valid", 1), 
        ("Imm", 32),
        ("Opcode", 7),
        ("rs1", 5),
        ("rs2", 5),
        ("rd", 5),
        ("funct3", 5),
        ("funct7", 7),
        ("shamt", 5),
        ("Type", 3),
        ("HasRs1", 1),
        ("HasRs2", 1),
        ("HasRd", 1),
        ("id", 7),
        ("ps1", 8),
        ("ps2", 8),
        ("pd", 7),
        ("oldpd", 8),
        ("rdy1", 1),
        ("rdy2", 1),
        ("rdy", 1),
        ("isBranch", 1),
        ("isStore", 1),
        ("isLoad", 1),
        ("hasException", 1),
        ("ExceptionType", 8),
        ("isTaken", 1),
        ("targetBrAddr", 32),
        ("reg_wb_data", 32),
        ("storeIdx", 8),
        ("gbranch_res", 1),
        ("lbranch_res", 1),    
        ("branch_res", 1),
        ("global_pht_idx", 13),
        ("local_pht_idx", 13),
        ("bht_idx", 8),
        ("branch_pred_addr", 32)        
    )
    val rob_elems_map = rob_elems.toMap
    def rob_read_elem(elem : String, index: UInt, bankid: UInt, rst : Bool = reset.asBool): UInt = {
        val read_api = Module(new ROBReadAPI(elem, rob_elems_map(elem)))
        read_api.io.rst := rst
        read_api.io.index := index
        read_api.io.bankid := bankid
        read_api.io.rob_rdata
    }
    def rob_write_elem(elem : String, index: UInt, data: UInt, bankid: UInt, clk: Bool = clock.asBool, rst : Bool = reset.asBool): Unit = {
        val write_api = Module(new ROBWriteAPI(elem, rob_elems_map(elem)))
        write_api.io.clk := clk
        write_api.io.rst := rst
        write_api.io.index := index
        write_api.io.bankid := bankid
        write_api.io.rob_wdata := data
    }

    def rob_id_loc_mem_write(index: UInt, data: UInt, clk: Bool = clock.asBool, rst : Bool = reset.asBool) : Unit = {
        val write_api = Module(new ROBIdLocMemWriteAPI) 
        write_api.io.clk := clk
        write_api.io.rst := rst
        write_api.io.index := index
        write_api.io.data := data
    }

    def rob_id_loc_mem_read(index: UInt, rst : Bool = reset.asBool): UInt = {
        val read_api = Module(new ROBIdLocMemReadAPI) 
        read_api.io.rst := rst
        read_api.io.index := index
        read_api.io.data
    }

    for(i <- 0 until base.FETCH_WIDTH){
        when(io.rob_state === flush){
            // ROBBankRegs(i)(tail - 1.U) := 0.U.asTypeOf(new ROBItem)
            // ROBIDLocMem(ROBBankRegs(i)(tail - 1.U).id) := (1 << bankwidth).U 
            for((elem, width) <- rob_elems){
                rob_write_elem(elem, tail - 1.U, 0.U, i.U(2.W))
            }
            rob_id_loc_mem_write(
                rob_read_elem("id", tail - 1.U, i.U),
                ((1 << bankwidth).U)(8.W)
            )
        }
    }

    for(i <- 0 until base.FETCH_WIDTH){
        var valid = WireInit(false.B)
        valid := rob_read_elem("valid", head, i.U(2.W))
        when(io.robr_able & io.retire_rdy_mask.andR & ~(rob_state === flush) & valid){
            // ROBBankRegs(i)(head) := 0.U.asTypeOf(new ROBItem)
            // ROBIDLocMem(ROBBankRegs(i)(head).id) := (1 << bankwidth).U     
            for((elem, width) <- rob_elems){
                rob_write_elem(elem, head, 0.U, i.U(2.W))
            }
            rob_id_loc_mem_write(
                rob_read_elem("id", head, i.U),
                ((1 << bankwidth).U)(8.W)
            )                                
        }
    }

    for(i <- 0 until base.FETCH_WIDTH){
        rob_input_valid(i) := io.rob_item_i(i).valid
        var rob_head_item = WireInit((0.U).asTypeOf(new ROBItem))
        var rob_tail_item = WireInit((0.U).asTypeOf(new ROBItem))
        rob_head_item.elements.foreach{ 
            case(name, data) => data := rob_read_elem(name, head, i.U(2.W))
        }
        rob_tail_item.elements.foreach{
            case(name, data) => data := rob_read_elem(name, tail - 1.U, i.U(2.W))
        }
        rob_item_o(i) := Mux(
            io.robr_able & (rob_state =/= flush), 
            rob_head_item, 
            Mux(
                io.robr_able & (rob_state === flush),
                rob_tail_item,
                0.U.asTypeOf(new ROBItem)
            )
        )
    } 

    for(i <- 0 until base.ALU_NUM){
        var loc = WireInit((0.U)((bankwidth + 1).W))
        loc := rob_id_loc_mem_read(io.cdb_i.alu_channel(i).rob_id)(bankwidth - 1, 0)
        var bankid = WireInit((0.U)(2.W))
        bankid := io.cdb_i.alu_channel(i).rob_id(bankwidth + 1, bankwidth)
        when(io.cdb_i.alu_channel(i).valid & (io.rob_state =/= "b11".U) & ~io.rat_flush_en & ~loc(bankwidth)){
            // ROBBankRegs(io.cdb_i.alu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.alu_channel(i).rob_id)(bankwidth - 1, 0)).rdy := true.B
            // ROBBankRegs(io.cdb_i.alu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.alu_channel(i).rob_id)(bankwidth - 1, 0)).reg_wb_data := io.cdb_i.alu_channel(i).reg_wr_data
            // ROBBankRegs(io.cdb_i.alu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.alu_channel(i).rob_id)(bankwidth - 1, 0)).targetBrAddr := io.cdb_i.alu_channel(i).branch_target_addr
            // ROBBankRegs(io.cdb_i.alu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.alu_channel(i).rob_id)(bankwidth - 1, 0)).hasException := io.cdb_i.alu_channel(i).has_exception
            // ROBBankRegs(io.cdb_i.alu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.alu_channel(i).rob_id)(bankwidth - 1, 0)).ExceptionType := io.cdb_i.alu_channel(i).exception_type

            rob_write_elem("rdy", loc, true.B.asUInt, bankid)
            rob_write_elem("reg_wb_data", loc, io.cdb_i.alu_channel(i).reg_wr_data, bankid)
            rob_write_elem("targetBrAddr", loc, io.cdb_i.alu_channel(i).branch_target_addr, bankid)
            rob_write_elem("hasException", loc, io.cdb_i.alu_channel(i).has_exception, bankid)
            rob_write_elem("ExceptionType", loc, io.cdb_i.alu_channel(i).exception_type, bankid)
        }
    }
    for(i <- 0 until base.AGU_NUM){
        var loc = WireInit((0.U)((bankwidth + 1).W))
        loc := rob_id_loc_mem_read(io.cdb_i.agu_channel(i).rob_id)
        var bankid = WireInit((0.U)(2.W))
        bankid := io.cdb_i.agu_channel(i).rob_id(bankwidth + 1, bankwidth)
        var agu_loc = WireInit((0.U)((bankwidth + 1).W))
        agu_loc := rob_id_loc_mem_read(io.agu_rob_id(i))
        when(io.cdb_i.agu_channel(i).valid & (io.rob_state =/= "b11".U) & ~io.rat_flush_en & ~loc(bankwidth)){
            // ROBBankRegs(io.cdb_i.agu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.agu_channel(i).rob_id)(bankwidth - 1, 0)).rdy := true.B
            // ROBBankRegs(io.cdb_i.agu_channel(i).rob_id(bankwidth + 1, bankwidth))(ROBIDLocMem(io.cdb_i.agu_channel(i).rob_id)(bankwidth - 1, 0)).reg_wb_data := io.cdb_i.agu_channel(i).reg_wr_data
            rob_write_elem("rdy", loc, true.B.asUInt, bankid)
            rob_write_elem("reg_wb_data", loc, io.cdb_i.agu_channel(i).reg_wr_data, bankid)
        }
        when(io.agu_valid(i) & io.agu_ls_flag(i) & (io.rob_state =/= "b11".U) & ~io.rat_flush_en & ~agu_loc(bankwidth)){
            // ROBBankRegs(io.agu_rob_id(i)(bankwidth + 1, bankwidth))(ROBIDLocMem(io.agu_rob_id(i))(bankwidth - 1, 0)).rdy := true.B     
            rob_write_elem("rdy", agu_loc, true.B.asUInt, bankid)     
        }
    }

    for(i <- 0 until base.FETCH_WIDTH){
        when(io.robw_able & io.rob_item_i(i).valid){
            // ROBBankRegs(i)(tail) := io.rob_item_i(i)
            // ROBIDLocMem(io.rob_item_i(i).id) := tail
            for((name, data) <- io.rob_item_i(i).elements){
                rob_write_elem(name, tail, data.asUInt, i.U(2.W))
            }
            rob_id_loc_mem_write(io.rob_item_i(i).id, tail)
        }
    }
}   
}