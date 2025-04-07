package chapter3_1

import chisel3._
import chisel3.util._

class AddSuber4InputBundle extends Bundle{
    var data1_i = UInt(4.W)
    var data2_i = UInt(4.W)
}

class AddSuber4OutputBundle extends Bundle{
    var res = UInt(4.W)
}

//subadd 0/1 -> Add/Sub
//-7 - (-8)
// 方法1：t_no_cin = 4'b1111 ^ 4'b1000 = 4'b0111
// carry, res = 4'b1001 + 4'b0111 + 1'b1 = 5'b10001
// carry = 1, res = 1, overflow = 0
// 方法2：(错误，实际不会溢出)
// t_no_cin = 4'b1111 ^ 4'b1000 + 1'b1 = 4'b1000
// carry, res = 4'b1001 + 4'b1000 = 5'b10001
// overflow = 1 & 1 = 1
class AddSuber4 extends Module{
    var io = IO(new Bundle{
        var subadd = Input(Bool())
        var data1_i = Input(UInt(4.W))
        var data2_i = Input(UInt(4.W))
        var carry = Output(Bool())
        var overflow = Output(Bool())
        var zero = Output(Bool())
        var res = Output(UInt(4.W))
    })

    var t_add_cin = WireInit((0.U)(4.W))
    var carry_result = WireInit((0.U)(5.W))
    
    t_add_cin := Fill(4, io.subadd) ^ io.data2_i
    carry_result := io.data1_i + t_add_cin + io.subadd
    
    io.carry := carry_result(4)
    io.res := carry_result(3, 0)
    io.overflow := ~(io.data1_i(3) ^ t_add_cin(3)) & (io.res(3) ^ io.data1_i(3))
    io.zero := ~io.res.orR
}