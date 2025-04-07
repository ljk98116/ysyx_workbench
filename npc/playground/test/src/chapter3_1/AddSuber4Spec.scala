package chapter3_1

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class AddSuber4Spec extends AnyFreeSpec with Matchers{
    "AddSuber4 should pass" in {
        simulate(new AddSuber4){ dut =>
            val testValues = for{
                x <- -8 to 7
                y <- -8 to 7
            } yield (x, y)
            val addinputSeq = testValues.map { case (x, y) => (new AddSuber4InputBundle).Lit(
                _.data1_i -> x.S.asUInt, _.data2_i -> y.S.asUInt) 
            }
            val subinputSeq = testValues.map { case (x, y) => (new AddSuber4InputBundle).Lit(
                _.data1_i -> x.S.asUInt, _.data2_i -> y.S.asUInt) 
            }
            dut.reset.poke(true.B)
            dut.clock.step()
            dut.reset.poke(false.B)
            dut.clock.step()

            var cycles : Int = 0
            while(cycles < addinputSeq.length){
                dut.io.data1_i.poke(addinputSeq(cycles).data1_i)
                dut.io.data2_i.poke(addinputSeq(cycles).data2_i)
                dut.io.subadd.poke(false.B)

                var target : BigInt = addinputSeq(cycles).data1_i.asSInt.litValue + addinputSeq(cycles).data2_i.asSInt.litValue
                if(target < -8 || target > 7){
                    println(addinputSeq(cycles).data1_i.asSInt)
                    println(addinputSeq(cycles).data2_i.asSInt)
                    println("plus overflow occured")
                    println("")
                    dut.io.overflow.expect(true.B)
                }
                else{
                    dut.io.res.expect(target.S(4.W).asUInt)
                }
                cycles += 1
                dut.clock.step()
            }

            cycles = 0
            while(cycles < subinputSeq.length){
                dut.io.data1_i.poke(subinputSeq(cycles).data1_i)
                dut.io.data2_i.poke(subinputSeq(cycles).data2_i)
                dut.io.subadd.poke(true.B)

                var target : BigInt = subinputSeq(cycles).data1_i.asSInt.litValue - subinputSeq(cycles).data2_i.asSInt.litValue
                if(target < -8 || target > 7){
                    println(subinputSeq(cycles).data1_i.asSInt)
                    println(subinputSeq(cycles).data2_i.asSInt)
                    println("minus overflow occured")
                    println("")
                    dut.io.overflow.expect(true.B)
                }
                else{
                    dut.io.res.expect(target.S(4.W).asUInt)
                }
                cycles += 1
                dut.clock.step()
            }            
        }
    }
}