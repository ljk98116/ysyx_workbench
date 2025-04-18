import cpu.core.backend._
import chisel3._
object Main {
  def main(args: Array[String]): Unit = {
    // 生成 Verilog 文件到默认路径（默认生成到当前目录）
    emitVerilog(new cpu.core.backend.ALUReserveStation(16))
  }
}