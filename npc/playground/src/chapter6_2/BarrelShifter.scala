package chapter6_2

import chisel3._
import chisel3.util._

class BarrelShifter extends Module{
    var io = IO(new Bundle{
        var din = Input(UInt(8.W))
        var shamt = Input(UInt(3.W))
        var LR = Input(Bool()) //0: right shift, 1: left shift
        var AL = Input(Bool()) //1: Algorithm, 0: logic
        var dout = Output(UInt(8.W))
    })
    io.dout := 0.U
    when(io.AL){
        switch(io.shamt){
            is(0.U){
                io.dout := io.din
            }
            is(1.U){
                io.dout := Mux(io.LR, 
                    Cat(io.din(6, 0), 0.U(1.W)),
                    Cat(io.din(7), io.din(7, 1))
                )
            }
            is(2.U){
                io.dout := Mux(io.LR,
                    Cat(io.din(5, 0), 0.U(2.W)),
                    Cat(Fill(2, io.din(7)), io.din(7, 2))
                )
            }
            is(3.U){
                io.dout := Mux(io.LR,
                    Cat(io.din(4, 0), 0.U(3.W)),
                    Cat(Fill(3, io.din(7)), io.din(7, 3))
                )                
            }
            is(4.U){
                io.dout := Mux(io.LR,
                    Cat(io.din(3, 0), 0.U(4.W)),
                    Cat(Fill(4, io.din(7)), io.din(7, 4))
                )                
            }
            is(5.U){
                io.dout := Mux(io.LR,
                    Cat(io.din(2, 0), 0.U(5.W)),
                    Cat(Fill(5, io.din(7)), io.din(7, 5))
                )                
            }
            is(6.U){
                io.dout := Mux(io.LR,
                    Cat(io.din(1, 0), 0.U(6.W)),
                    Cat(Fill(6, io.din(7)), io.din(7, 6))
                )                
            }
            is(7.U){
                io.dout := Mux(io.LR,
                    Cat(io.din(0), 0.U(7.W)),
                    Cat(Fill(7, io.din(7)), io.din(7))
                )                
            }
        }
    }.otherwise{
        switch(io.shamt){
            is(0.U){
                io.dout := io.din
            }
            is(1.U){
                io.dout := Mux(io.LR, 
                    Cat(io.din(6, 0), 0.U(1.W)),
                    Cat(io.din(0), io.din(7, 1))
                )
            }
            is(2.U){
                io.dout := Mux(io.LR,
                    Cat(io.din(5, 0), 0.U(2.W)),
                    Cat(io.din(1, 0), io.din(7, 2))
                )
            }
            is(3.U){
                io.dout := Mux(io.LR,
                    Cat(io.din(4, 0), 0.U(3.W)),
                    Cat(io.din(2, 0), io.din(7, 3))
                )                
            }
            is(4.U){
                io.dout := Mux(io.LR,
                    Cat(io.din(3, 0), 0.U(4.W)),
                    Cat(io.din(3, 0), io.din(7, 4))
                )                
            }
            is(5.U){
                io.dout := Mux(io.LR,
                    Cat(io.din(2, 0), 0.U(5.W)),
                    Cat(io.din(4, 0), io.din(7, 5))
                )                
            }
            is(6.U){
                io.dout := Mux(io.LR,
                    Cat(io.din(1, 0), 0.U(6.W)),
                    Cat(io.din(5, 0), io.din(7, 6))
                )                
            }
            is(7.U){
                io.dout := Mux(io.LR,
                    Cat(io.din(0), 0.U(7.W)),
                    Cat(io.din(6, 0), io.din(7))
                )                
            }
        }        
    }
}