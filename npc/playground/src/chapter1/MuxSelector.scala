package chapter1

import chisel3._

class MuxSelector extends Module{
    var io = IO(new Bundle{
        var X0 = Input(UInt(2.W))
        var X1 = Input(UInt(2.W))
        var X2 = Input(UInt(2.W))
        var X3 = Input(UInt(2.W))
        var Y = Input(UInt(2.W))
        var f = Output(UInt(2.W))
    })

    var mid0 = WireInit((0.U)(2.W))
    var mid1 = WireInit((0.U)(2.W))
    
    mid0 := Mux((io.Y & 1.U) === 0.U, io.X0, io.X1)
    mid1 := Mux((io.Y & 1.U) === 0.U, io.X2, io.X3)
    io.f := Mux((io.Y & 2.U) === 0.U, mid0, mid1)
}