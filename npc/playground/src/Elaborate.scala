object Elaborate extends App {
  val firtoolOptions = Array(
    "--lowering-options=" + List(
      // make yosys happy
      // see https://github.com/llvm/circt/blob/main/docs/VerilogGeneration.md
      "disallowLocalVariables",
      "disallowPackedArrays",
      "locationInfoStyle=wrapInAtSquareBracket"
    ).reduce(_ + "," + _)
  )
  // circt.stage.ChiselStage.emitSystemVerilogFile(new gcd.GCD(), args, firtoolOptions)
  /* chapter1 */
  // circt.stage.ChiselStage.emitSystemVerilogFile(new chapter1.MuxSelector(), args, firtoolOptions)
  /* chapter2 */
  // circt.stage.ChiselStage.emitSystemVerilogFile(new chapter2.PriorityDecoder83(), args, firtoolOptions)
  /* chapter3 */
  // circt.stage.ChiselStage.emitSystemVerilogFile(new chapter3_1.AddSuber4, args, firtoolOptions)
  /* chapter3 */
  // circt.stage.ChiselStage.emitSystemVerilogFile(new chapter3_2.ALU4, args, firtoolOptions)
  /* chapter6 */
  // circt.stage.ChiselStage.emitSystemVerilogFile(new chapter6_1.PRandGen(42), args, firtoolOptions)
  /* chapter6 */
  // circt.stage.ChiselStage.emitSystemVerilogFile(new chapter6_2.BarrelShifter, args, firtoolOptions)
  /* chapter7 */
  // circt.stage.ChiselStage.emitSystemVerilogFile(new chapter7.ps2_keyboard, args, firtoolOptions)
  /* eaglecore */
  // circt.stage.ChiselStage.emitSystemVerilogFile(new cpu.core.ROB, args, firtoolOptions)
  circt.stage.ChiselStage.emitSystemVerilogFile(new cpu.core.CPUCore(""), args, firtoolOptions)
}
