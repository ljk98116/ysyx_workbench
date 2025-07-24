package cpu.core

import chisel3._
import chisel3.util._
import cpu.config._

/* rob_state为1则ROB正在刷掉流水线 */
/* 刷新流水线时的恢复,需要将异常指令后ROB内所有指令的占用的物理寄存器释放 */
/* 不可读时需要暂停rename1以及之前的流水级 */
class FreeRegBuffer(id : Int) extends Module
{
    val io = IO(new Bundle{
        /* retire stage */
        val rob_state = Input(Bool())
        val rat_write_en_retire = Input(Bool())
        val freereg_i = Input(UInt(base.PREG_WIDTH.W))
        val flush_freereg_valid = Input(Bool())

        /* output */
        val rat_write_en_rename = Input(Bool())
        val freereg_o = Output(UInt(base.PREG_WIDTH.W))
        val rd_able = Output(Bool())
        val wr_able = Output(Bool())
    })

    var regnum = 1 << base.AREG_WIDTH
    if(id == 0) regnum = regnum - 1

    var head = RegInit((0.U)(base.AREG_WIDTH.W))
    var tail = RegInit(((regnum - 1).U)(base.AREG_WIDTH.W))

    var rd_able = WireInit(true.B)
    var wr_able = WireInit(false.B)
    
    var freereg_o = WireInit((0.U)(base.PREG_WIDTH.W))
    if(id == 0){
        val FreeRegIds = RegInit(VecInit(
            Seq.tabulate(regnum)((i) => (((id << base.AREG_WIDTH) + i + 1).U)(base.PREG_WIDTH.W))
        ))
        for(i <- 0 until regnum){
            when(io.rob_state & (i.U === (head - 1.U)) & wr_able & io.flush_freereg_valid){
                FreeRegIds(i) := io.freereg_i
            }.elsewhen(io.rat_write_en_retire & wr_able & (i.U === tail)){
                FreeRegIds(i) := io.freereg_i
            }.elsewhen(rd_able & io.rat_write_en_rename & ~io.rob_state & (i.U === head)){
                FreeRegIds(i) := 0.U
            }
        }

        freereg_o := Mux(rd_able, FreeRegIds(head), 0.U)
    }else{
        val FreeRegIds = RegInit(VecInit(
            Seq.tabulate(regnum)((i) => (((id << base.AREG_WIDTH) + i).U)(base.PREG_WIDTH.W))
        ))
        for(i <- 0 until regnum){
            when(io.rob_state & (i.U === (head - 1.U)) & wr_able & io.flush_freereg_valid){
                FreeRegIds(i) := io.freereg_i
            }.elsewhen(io.rat_write_en_retire & wr_able & (i.U === tail)){
                FreeRegIds(i) := io.freereg_i
            }.elsewhen(rd_able & io.rat_write_en_rename & ~io.rob_state & (i.U === head)){
                FreeRegIds(i) := 0.U
            }
        }

        freereg_o := Mux(rd_able, FreeRegIds(head), 0.U)        
    }

    rd_able := head =/= tail
    wr_able := (tail + 1.U) =/= head

    head := Mux(
        rd_able & io.rat_write_en_rename & ~io.rob_state,
        head + 1.U,
        Mux(io.rob_state & wr_able & io.flush_freereg_valid, head - 1.U, head) 
    )

    tail := Mux(
        wr_able & io.rat_write_en_retire & ~io.rob_state,
        tail + 1.U,
        tail
    )
    /* connect */
    io.freereg_o := freereg_o
    io.rd_able  := rd_able
    io.wr_able := wr_able
}