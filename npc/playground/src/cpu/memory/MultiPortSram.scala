package cpu.memory

import cpu.config._
import chisel3._
import chisel3.util._

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
    var xwen_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)(false.B)
    ))
    var xwaddr_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.ADDR_WIDTH.W))
    ))

    var xwdata_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(base.DATA_WIDTH.W))
    ))
    var xwmask_reg = RegInit(VecInit(
        Seq.fill(base.AGU_NUM)((0.U)(8.W))
    ))
    var xraddr_reg = RegInit(VecInit(
        Seq.fill(ReadPorts)((0.U)(base.ADDR_WIDTH.W))
    ))
    for(i <- 0 until base.AGU_NUM){
        xwen_reg(i) := io.wen(i)
        xwaddr_reg(i) := io.waddr(i)
        xwdata_reg(i) := io.wdata(i)
        xwmask_reg(i) := io.wmask(i)
    }
    for(i <- 0 until ReadPorts){
        xraddr_reg(i) := io.raddr(i)
        if(i < base.FETCH_WIDTH){
            rdata(i) := read_apis(i).io.rdata
        }else{
            rdata(i) := Mux(
                xwen_reg(1) & (xwaddr_reg(1) === xraddr_reg(i)), 
                xwdata_reg(1), 
                Mux(
                    xwen_reg(0) & (xwaddr_reg(0) === xraddr_reg(i)), 
                    xwdata_reg(0),
                    read_apis(i).io.rdata                     
                )
            )
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