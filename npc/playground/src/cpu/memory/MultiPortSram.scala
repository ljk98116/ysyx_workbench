package cpu.memory

import cpu.config._
import chisel3._
import chisel3.util._
import chisel3.experimental._
import scopt.Read
import chisel3.util.experimental.loadMemoryFromFile

/* 多读一写 */
class MultiPortSram(ReadPorts : Int, memfile : String, DEBUG: Boolean = false) extends Module{
    val io = IO(new Bundle{
        val ren = Input(Vec(ReadPorts, Bool()))
        val raddr = Input(Vec(ReadPorts, UInt(base.ADDR_WIDTH.W)))
        val rdata = Output(Vec(ReadPorts, UInt(base.DATA_WIDTH.W)))
        val wen = Input(Vec(base.AGU_NUM, Bool()))
        val wmask = Input(Vec(base.AGU_NUM, UInt(8.W)))
        val waddr = Input(Vec(base.AGU_NUM, UInt(base.ADDR_WIDTH.W)))
        val wdata = Input(Vec(base.AGU_NUM, UInt(base.DATA_WIDTH.W)))
    })

    var rdata = WireInit(VecInit(
        Seq.fill(ReadPorts)((0.U)(base.DATA_WIDTH.W))
    ))

if(DEBUG){
    var read_apis = Seq.fill(ReadPorts)(
        Module(new MemReadAPI)
    )
    var write_apis = Seq.fill(base.AGU_NUM)(Module(new MemWriteAPI))
    for(i <- 0 until ReadPorts){
        if(i < base.FETCH_WIDTH + 1){
            rdata(i) := read_apis(i).io.rdata
        }else{
            rdata(i) := Mux(io.wen(0) & (io.waddr(0) === io.raddr(base.FETCH_WIDTH + 1)), io.wdata(0), read_apis(i).io.rdata)
        }
        read_apis(i).io.clk := clock
        read_apis(i).io.rst := reset.asBool
        read_apis(i).io.raddr := Mux(io.ren(i), io.raddr(i), 0.U)
    }
    write_apis(0).io.clk := clock
    write_apis(0).io.rst := reset.asBool
    write_apis(0).io.waddr := Mux(io.wen(0) & (io.waddr(0) =/= io.waddr(1)), io.waddr(0), 0.U)
    write_apis(0).io.wdata := Mux(io.wen(0) & (io.waddr(0) =/= io.waddr(1)), io.wdata(0), 0.U)
    write_apis(0).io.wmask := Mux(io.wen(0) & (io.waddr(0) =/= io.waddr(1)), io.wmask(0), 0.U)
    write_apis(1).io.clk := clock
    write_apis(1).io.rst := reset.asBool
    write_apis(1).io.waddr := Mux(io.wen(1), io.waddr(1), 0.U)
    write_apis(1).io.wdata := Mux(io.wen(1), io.wdata(1), 0.U)
    write_apis(1).io.wmask := Mux(io.wen(1), io.wmask(1), 0.U)
}
else{
} 
    io.rdata := rdata
}