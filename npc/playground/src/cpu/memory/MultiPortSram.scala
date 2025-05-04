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
        val wen = Input(Bool())
        val wmask = Input(UInt(8.W))
        val waddr = Input(UInt(base.ADDR_WIDTH.W))
        val wdata = Input(UInt(base.DATA_WIDTH.W))
    })

    var rdata = WireInit(VecInit(
        Seq.fill(ReadPorts)((0.U)(base.DATA_WIDTH.W))
    ))

if(DEBUG){
    var read_apis = Seq.fill(ReadPorts)(
        Module(new MemReadAPI)
    )
    var write_api = Module(new MemWriteAPI)
    for(i <- 0 until ReadPorts){
        read_apis(i).io.clk := clock
        read_apis(i).io.rst := reset.asBool
        read_apis(i).io.raddr := Mux(io.ren(i) & ~(io.wen & io.waddr === io.raddr(i) & io.wmask === "b1111".U), io.raddr(i), 0.U)
        rdata(i) := Mux(io.wen & io.waddr === io.raddr(i) & io.wmask === "b1111".U, io.wdata, read_apis(i).io.rdata)
    }
    write_api.io.clk := clock
    write_api.io.rst := reset.asBool
    write_api.io.waddr := io.waddr
    write_api.io.wdata := io.wdata
    write_api.io.wmask := io.wmask
}
else{
    val mems = Seq.fill(ReadPorts)(SyncReadMem(256, UInt(base.DATA_WIDTH.W)))
    if(memfile != ""){
        for(i <- 0 until ReadPorts){
            loadMemoryFromFile(mems(i), memfile)
        }
    }
    for(i <- 0 until ReadPorts){
        rdata(i) := Mux(io.wen & io.waddr === io.raddr(i) & io.wmask === "b1111".U, io.wdata, mems(i).read(io.raddr(i), io.ren(i)))
    }
    when(io.wen) {
        for(i <- 0 until ReadPorts){
            mems(i).write(io.waddr, io.wdata)
        }
    }
} 
    io.rdata := rdata
}