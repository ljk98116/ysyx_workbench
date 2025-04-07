package chapter7

import chisel3._
import chisel3.util._
import chapter2.Bcd7Seg

class ps2_keyboard extends Module{
    var io = IO(new Bundle{
        var ps2_clk = Input(Clock())
        var ps2_data = Input(Bool())
        var seg_keycode = Output(Vec(2, UInt(8.W)))
        var seg_ascii = Output(Vec(2, UInt(8.W)))
        var seg_presscnt = Output(Vec(2, UInt(8.W)))
    })

    val up::down::Nil = Enum(2)
    var sampling = WireInit(false.B)
    var keyvalue = WireInit((0.U)(16.W))
    var ascii = WireInit((0.U)(8.W))

    /* 256 * 16bits, LowerCase|UpperCase, Locked/Unlocked */
    val keyrom = VecInit(
        Seq.tabulate(256)((i) => {
            i match {
                case 0x0E => Cat((0x60.U)(8.W), (0x7E.U)(8.W)) // ~/`
                case 0x16 => Cat((0x31.U)(8.W), (0x21.U)(8.W)) // 1/!
                case 0x1E => Cat((0x32.U)(8.W), (0x40.U)(8.W)) // 2/@
                case 0x26 => Cat((0x33.U)(8.W), (0x23.U)(8.W)) // 3/#
                case 0x25 => Cat((0x34.U)(8.W), (0x24.U)(8.W)) // 4/$
                case 0x2E => Cat((0x35.U)(8.W), (0x25.U)(8.W)) // 5/%
                case 0x36 => Cat((0x36.U)(8.W), (0x5E.U)(8.W)) // 6/^
                case 0x3D => Cat((0x37.U)(8.W), (0x26.U)(8.W)) // 7/&
                case 0x3E => Cat((0x38.U)(8.W), (0x2A.U)(8.W)) // 8/*
                case 0x46 => Cat((0x39.U)(8.W), (0x28.U)(8.W)) // 9/(
                case 0x45 => Cat((0x30.U)(8.W), (0x29.U)(8.W)) // 0/)
                case 0x4E => Cat((0x2D.U)(8.W), (0x5F.U)(8.W)) // -_
                case 0x55 => Cat((0x3D.U)(8.W), (0x2B.U)(8.W)) // =+
                case 0x5D => Cat((0x5C.U)(8.W), (0x7C.U)(8.W)) // \|
                case 0x66 => 0x8.U(16.W) //backspace
                case 0x0D => 0x9.U(16.W) //tab
                case 0x15 => Cat((0x71.U)(8.W), (0x51.U)(8.W)) // qQ
                case 0x1D => Cat((0x77.U)(8.W), (0x57.U)(8.W)) // wW
                case 0x24 => Cat((0x65.U)(8.W), (0x45.U)(8.W)) // eE
                case 0x2D => Cat((0x72.U)(8.W), (0x52.U)(8.W)) // rR
                case 0x2C => Cat((0x74.U)(8.W), (0x54.U)(8.W)) // tT
                case 0x35 => Cat((0x79.U)(8.W), (0x59.U)(8.W)) // yY
                case 0x3C => Cat((0x75.U)(8.W), (0x55.U)(8.W)) // uU
                case 0x43 => Cat((0x69.U)(8.W), (0x49.U)(8.W)) // iI
                case 0x44 => Cat((0x6F.U)(8.W), (0x4F.U)(8.W)) // oO
                case 0x4D => Cat((0x70.U)(8.W), (0x50.U)(8.W)) // pP
                case 0x54 => Cat((0x5B.U)(8.W), (0x7B.U)(8.W)) // [{
                case 0x5B => Cat((0x5D.U)(8.W), (0x7D.U)(8.W)) // ]}
                case 0x5A => 0xD.U(16.W) //enter
                case 0x58 => 0x14.U(16.W) //capslock
                case 0x1C => Cat((0x61.U)(8.W), (0x41.U)(8.W)) // aA
                case 0x1B => Cat((0x73.U)(8.W), (0x53.U)(8.W)) // sS
                case _ => 0.U
            }
        })
    )
    /* press counter */
    var press_counter_reg = RegInit((0.U)(8.W))
    /* press state */
    var state_reg = RegInit(up)

    /* used for negedge check */
    var ps2_clk_reg = RegInit((0.U)(3.W))
    /* ps2 data buffer, 11bits */
    var buffer = RegInit(VecInit(
        Seq.fill(11)(false.B)
    ))
    /* used for buffer */
    var counter = RegInit((0.U)(4.W))
    /* scan code */
    var keycode = WireInit((0.U)(8.W))

    /* special keys status */
    var is_shift = RegInit(false.B)
    var is_cap = RegInit(false.B)

    ps2_clk_reg := Cat(ps2_clk_reg(1, 0), io.ps2_clk.asBool)
    sampling := ps2_clk_reg(2) & (~ps2_clk_reg(1))
    var interval = RegInit((1.U)(1.W))
    /* 状态全0认为开始检测 */
    when(sampling){
        when(counter === 10.U){
            when(~buffer(0) & io.ps2_data & buffer.asUInt(9, 1).xorR){
                //printf("state_reg: %d, last_press: 0x%x, buffer: 0x%x, press_sync_cnt: 0x%x\n", 
                    //state_reg, last_press, buffer.asUInt(8,1), press_sync_cnt)
                /* 转换状态 */
                when(buffer.asUInt(8, 1) === 0xF0.U){
                    state_reg := up
                    is_shift := false.B
                    /* 按键计数 */
                    press_counter_reg := press_counter_reg + 1.U
                    interval := true.B
                }.otherwise{
                    when(~interval.orR){
                        state_reg := Mux(buffer.asUInt(8,1).orR, down, state_reg)
                        /* 是否按下了shift */
                        is_shift := Mux((buffer.asUInt(8, 1) === 0x12.U) | (buffer.asUInt(8, 1) === 0x59.U), true.B, is_shift)
                        /* 是否按下了caplock */
                        is_cap := Mux(buffer.asUInt(8, 1) === 0x58.U, ~is_cap, is_cap)
                        /* 是否按下了numlock */
                        // is_numlock := buffer(8, 1) === 0x77
                    }
                    interval := Mux(interval.orR, interval - 1.U, interval)
                }
            }
            /* 计数器清零 */
            counter := 0.U
        }.otherwise{
            buffer(counter) := io.ps2_data
            counter := counter + 1.U
        }
    }.otherwise{
    }
    /* 保存当前输入码 */
    keycode := buffer.asUInt(8, 1)
    
    /* 找到keyrom项 */
    keyvalue := keyrom(keycode)
    ascii := 0.U
    /* 转换大小写，取低位 */
    switch(keycode){
        is(0x66.U, 0x0D.U, 0x5A.U, 0x58.U){ // backspace, tab, enter, capslock
            ascii := keyvalue
        }
        is(
            0x0E.U, 
            0x16.U, 0x1E.U, 0x26.U, 0x25.U, 0x2E.U, 0x36.U, 0x3D.U, 0x3E.U, 0x46.U, 0x45.U,
            0x4E.U, 0x55.U, 0x5D.U,
            0x54.U, 0x5B.U
        ){
            ascii := Mux(is_shift, keyvalue(7,0), keyvalue(15, 8))
        }
        is(
            0x15.U, 0x1D.U, 0x24.U, 0x2D.U, 0x2C.U, 0x35.U, 0x3C.U, 0x43.U, 0x44.U, 0x4D.U,
            0x1C.U, 0x1B.U
        ){
            ascii := Mux(is_shift ^ is_cap, keyvalue(7,0), keyvalue(15, 8))
        }
    }

    /* 数码管连接 */
    var keycode_segs = Seq.fill(2)(Module(new Bcd7Seg))
    var ascii_segs = Seq.fill(2)(Module(new Bcd7Seg))
    var press_segs = Seq.fill(2)(Module(new Bcd7Seg))

    keycode_segs(0).io.b := keycode(3, 0)
    keycode_segs(1).io.b := keycode(7, 4)
    io.seg_keycode(0) := Mux(state_reg === down, keycode_segs(0).io.h, 0xFF.U(8.W))
    io.seg_keycode(1) := Mux(state_reg === down, keycode_segs(1).io.h, 0xFF.U(8.W))

    ascii_segs(0).io.b := ascii(3, 0)
    ascii_segs(1).io.b := ascii(7, 4)
    io.seg_ascii(0) := Mux(state_reg === down, ascii_segs(0).io.h, 0xFF.U(8.W))
    io.seg_ascii(1) := Mux(state_reg === down, ascii_segs(1).io.h, 0xFF.U(8.W))

    press_segs(0).io.b := press_counter_reg(3, 0)
    press_segs(1).io.b := press_counter_reg(7, 4)
    io.seg_presscnt(0) := press_segs(0).io.h
    io.seg_presscnt(1) := press_segs(1).io.h
}