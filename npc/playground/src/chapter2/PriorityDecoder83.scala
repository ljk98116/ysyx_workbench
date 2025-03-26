package chapter2

import chisel3._

class PriorityDecoder83 extends Module{
    var io = IO(new Bundle{
        var in = Input(UInt(8.W))
        var en = Input(Bool())
        var res = Output(UInt(3.W))
        var hex = Output(UInt(8.W))
        var hasOne = Output(Bool())
    })

    io.hasOne := Mux(io.en, io.in.orR, false.B)
    /* 高位是否有1 */
    var ValidVec = WireInit(VecInit(
        Seq.fill(8)(false.B)
    ))
    var Valid = WireInit((0.U)(8.W))
    for(i <- 0 until 7){
        ValidVec(i) := ~io.in(7, i+1).orR & io.in(i) & io.en
    }
    ValidVec(7) := io.in(7) & io.en

    var resVec = WireInit(VecInit(
        Seq.fill(3)(false.B)
    ))

    /* onehot转二进制 */
    Valid := ValidVec.asUInt
    resVec(2) := Valid(7, 4).orR
    resVec(1) := Valid(7, 6).orR | Valid(3, 2).orR
    resVec(0) := Valid(7) | Valid(5) | Valid(3) | Valid(1)

    io.res := resVec.asUInt

    val bcd = Module(new Bcd7Seg)
    bcd.io.b := io.res
    io.hex := bcd.io.h
}