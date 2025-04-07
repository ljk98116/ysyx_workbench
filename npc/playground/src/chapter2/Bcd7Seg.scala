package chapter2
import chisel3._

class Bcd7Seg extends Module{
    var io = IO(new Bundle{
        var b = Input(UInt(4.W))
        var h = Output(UInt(8.W))
    })
    io.h := ~0.U(8.W) //必须有本内容
    var segs = WireInit(
        VecInit(
            Seq.tabulate(16)((i) => {
                i match {
                    case 0 => {
                        ~("b11111100".U)(8.W)
                    }
                    case 1 =>{
                        ~("b01100000".U)(8.W)
                    }
                    case 2 =>{
                        ~("b11011010".U)(8.W)
                    }
                    case 3 =>{
                        ~("b11110010".U)(8.W)
                    }
                    case 4 =>{
                        ~("b01100110".U)(8.W)
                    }
                    case 5 =>{
                        ~("b10110110".U)(8.W)
                    }
                    case 6 =>{
                        ~("b10111110".U)(8.W)
                    }
                    case 7 =>{
                        ~("b11100000".U)(8.W)
                    }
                    case 8 =>{
                        ~("b11111110".U)(8.W)
                    }
                    case 9 =>{
                        ~("b11110110".U)(8.W)
                    }
                    case 10 =>{
                        ~("b11101110".U)(8.W)
                    }
                    case 11 =>{
                        ~("b00111110".U)(8.W)
                    }
                    case 12 =>{
                        ~("b10011100".U)(8.W)
                    }
                    case 13 =>{
                        ~("b01111010".U)(8.W)
                    }
                    case 14 =>{
                        ~("b10011110".U)(8.W)
                    }
                    case 15 =>{
                        ~("b10001110".U)(8.W)
                    }
                    case _ =>{
                        ~0.U(8.W)
                    }
                }
            })
        )
    )
    io.h := segs(io.b)
}