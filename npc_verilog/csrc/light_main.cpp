#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "Vlight.h"
#include <verilated.h>
// use vcd
#include <verilated_vcd_c.h>
// nvboard
#include <nvboard.h>

static TOP_NAME dut;

void nvboard_bind_all_pins(TOP_NAME* top);

void single_cycle() {
  dut.clk = 0; dut.eval();
  dut.clk = 1; dut.eval();
}

void reset(int n) {
  dut.rst = 1;
  while (n -- > 0) single_cycle();
  dut.rst = 0;
}

int main(int argc, char *argv[]){
#ifdef SIMULATION
    int t = 10;
    VerilatedContext *contextp = new VerilatedContext;
    contextp->commandArgs(argc, argv);
    Vdouble_switch *top = new Vdouble_switch{contextp};
    VerilatedVcdC *tfp = new VerilatedVcdC;
    //打开追踪功能
    contextp->traceEverOn(true);
    top->trace(tfp, 0);
    tfp->open("wave.vcd");
    tfp->dumpvars(0, "");
    while (t--) {
        int a = rand() & 1;
        int b = rand() & 1;
        top->a = a;
        top->b = b;
        top->eval();
        printf("a = %d, b = %d, f = %d\n", a, b, top->f);
        assert(top->f == (a ^ b));
        tfp->dump(contextp->time());
        contextp->timeInc(1);
    }
    tfp->close();
    delete top;
    delete contextp;
#endif
    nvboard_bind_all_pins(&dut);
    nvboard_init();

    reset(10);

    while(1) {
        nvboard_update();
        single_cycle();
    }
    return 0;
}