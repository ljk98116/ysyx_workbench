#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "VMuxSelector.h"
#include <verilated.h>
// use vcd
#include <verilated_vcd_c.h>
// nvboard
#include <nvboard.h>

static TOP_NAME dut;

void nvboard_bind_all_pins(TOP_NAME* top);

void single_cycle() {
  dut.clock = 0; dut.eval();
  dut.clock = 1; dut.eval();
}

void reset(int n) {
  dut.reset = 1;
  while (n -- > 0) single_cycle();
  dut.reset = 0;
}

int main(int argc, char **argv){
    nvboard_bind_all_pins(&dut);
    nvboard_init();

    reset(10);

    while(1) {
        nvboard_update();
        single_cycle();
    }
    return 0;
}