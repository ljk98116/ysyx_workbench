package chapter3_2

import chisel3._
import chisel3.util._

class ALU4 extends Module{
    var io = IO(new Bundle{
        var func = Input(UInt(3.W))
        var data1_i = Input(UInt(4.W))
        var data2_i = Input(UInt(4.W))
        var result = Output(UInt(4.W))
        var carry = Output(Bool())
        var overflow = Output(Bool())
    })

    var t_add_cin = WireInit((0.U)(4.W))
    var carry_result = WireInit((0.U)(5.W))
    var result = WireInit((0.U)(4.W))
    var carry = WireInit(false.B)
    var overflow = WireInit(false.B)
    var is_addsub = WireInit(false.B)
    var signed_data1 = WireInit((0.S)(4.W))
    var signed_data2 = WireInit((0.S)(4.W))

    is_addsub := (~io.func(2)) & (~io.func(1)) 
    signed_data1 := io.data1_i.asSInt
    signed_data2 := io.data2_i.asSInt

    t_add_cin := Fill(4, io.func(0)) ^ io.data2_i
    carry_result := io.data1_i + t_add_cin + io.func(0)
    switch(io.func){
        is("b000".U(3.W), "b001".U(3.W)){
            result := carry_result(3, 0)
            carry := carry_result(4)
            overflow := ~(io.data1_i(3) ^ t_add_cin(3)) & (result(3) ^ io.data1_i(3))
        }
        is("b010".U(3.W)){
            result := (~signed_data1).asUInt
            carry := false.B
            overflow := false.B
        }
        is("b011".U(3.W)){
            result := (signed_data1 & signed_data2).asUInt
            carry := false.B
            overflow := false.B            
        }
        is("b100".U(3.W)){
            result := (signed_data1 | signed_data2).asUInt
            carry := false.B
            overflow := false.B            
        }
        is("b101".U(3.W)){
            result := (signed_data1 ^ signed_data2).asUInt
            carry := false.B
            overflow := false.B             
        }
        is("b110".U(3.W)){
            result := Mux(signed_data1 < signed_data2, 1.U, 0.U)
            carry := false.B
            overflow := false.B             
        }
        is("b111".U(3.W)){
            result := Mux(signed_data1 === signed_data2, 1.U, 0.U)
            carry := false.B
            overflow := false.B             
        }
    }

    io.carry := carry
    io.result := result
    io.overflow := overflow
}