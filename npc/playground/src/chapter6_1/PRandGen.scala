package chapter6_1

import chisel3._
import chisel3.util._
import chapter2.Bcd7Seg

class PRandGen(seed : Int) extends Module{
    assert(seed > 0, "seed must be in 0~255")
    var rand_seed = seed.U(8.W)
    var io = IO(new Bundle{
        var btn = Input(Bool())
        var seg0_o = Output(UInt(8.W))
        var seg1_o = Output(UInt(8.W))
    })

    var LFSR = RegInit(rand_seed)

    LFSR := Mux(io.btn, Cat(LFSR(4) ^ LFSR(3) ^ LFSR(2) ^ LFSR(0), LFSR(7, 1)), LFSR)

    var bcd0 = Module(new Bcd7Seg)
    var bcd1 = Module(new Bcd7Seg)

    bcd0.io.b := LFSR(3, 0)
    bcd1.io.b := LFSR(7, 4)
    io.seg0_o := bcd0.io.h
    io.seg1_o := bcd1.io.h
}