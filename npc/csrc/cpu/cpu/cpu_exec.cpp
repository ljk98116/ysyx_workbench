#include <cpu/cpu.hpp>
#include <cpu/difftest.hpp>

#include <isa.hpp>
#include <locale.h>
#include <utils.hpp>
#include "VCPUCore.h"

namespace npc{
#define MAX_INST_TO_PRINT 10

/* npc state */
npc_CPU_state cpu = {};
uint8_t retire_RAT[32];
uint8_t rename_RAT[32];
uint8_t commit_num;
uint32_t cycle;
bool ref_stop;

static TOP_NAME dut;

uint64_t g_nr_guest_inst = 0;
static uint64_t g_timer = 0; // unit: us
static bool g_print_step = false;

static void trace_and_difftest() {
#ifdef CONFIG_ITRACE_COND
  if (ITRACE_COND) { log_write("%s\n", _this->logbuf); }
#endif
  // if (g_print_step) { IFDEF(CONFIG_ITRACE, puts(_this->logbuf)); }
  IFDEF(CONFIG_DIFFTEST, difftest_step());
  // IFDEF(CONFIG_WATCH_POINT, watchpoint_step());
}

static void statistic() {
  IFNDEF(CONFIG_TARGET_AM, setlocale(LC_NUMERIC, ""));
#define NUMBERIC_FMT MUXDEF(CONFIG_TARGET_AM, "%", "%'") PRIu64
  Log("host time spent = " NUMBERIC_FMT " us", g_timer);
  Log("total guest instructions = " NUMBERIC_FMT, g_nr_guest_inst);
  if (g_timer > 0) Log("simulation frequency = " NUMBERIC_FMT " inst/s", g_nr_guest_inst * 1000000 / g_timer);
  else Log("Finish running in less than 1 us and can not calculate the simulation frequency");
}

void assert_fail_msg() {
  isa_reg_display(&cpu, false);
  statistic();
}

/* 执行一个周期 */
static void exec_once(VerilatedVcdC* tfp){
  commit_num = 0;
  dut.clock = 0; dut.eval();
  dut.clock = 1; dut.eval();
  //vcd记录仿真结果
  if(tfp != nullptr) tfp->dump(cycle);
  /* 存在指令提交,进行difftest */
  if(commit_num > 0){
    Log("npc commit_num:%d at %d th cycle", commit_num, cycle);
    trace_and_difftest();
    g_nr_guest_inst += commit_num;
  }
  ++cycle;
}

/* 执行n个周期 */
static void execute(uint64_t n, VerilatedVcdC* tfp) {
  while(n-- > 0 && !ref_stop && nemu_state.state != NEMU_ABORT && nemu_state.state != NEMU_END) {
    exec_once(tfp);
  }
}

void cpu_reset(VerilatedVcdC* tfp){
  dut.reset = 1;
  dut.trace(tfp, 99); // 跟踪所有信号（99=递归深度）
  tfp->open("wave2.vcd"); // 输出文件名
  int n = 5;
  while (n -- > 0) exec_once(tfp);
  dut.reset = 0;  
  ref_stop = false;
}

/* Simulate how the CPU works. */
void cpu_exec(uint64_t n, VerilatedVcdC* tfp) {
  g_print_step = (n < MAX_INST_TO_PRINT);
  switch (nemu_state.state) {
    case NEMU_END: case NEMU_ABORT: case NEMU_QUIT:
      printf("Program execution has ended. To restart the program, exit NEMU and run again.\n");
      return;
    default: nemu_state.state = NEMU_RUNNING;
  }
  uint64_t timer_start = get_time();

  execute(n, tfp);

  uint64_t timer_end = get_time();
  g_timer += timer_end - timer_start;

  switch (nemu_state.state) {
    case NEMU_RUNNING: nemu_state.state = NEMU_STOP; break;

    case NEMU_END: case NEMU_ABORT:
      Log("nemu: %s at pc = " FMT_WORD,
          (nemu_state.state == NEMU_ABORT ? ANSI_FMT("ABORT", ANSI_FG_RED) :
           (nemu_state.halt_ret == 0 ? ANSI_FMT("HIT GOOD TRAP", ANSI_FG_GREEN) :
            ANSI_FMT("HIT BAD TRAP", ANSI_FG_RED))),
          nemu_state.halt_pc);
      // fall through
    case NEMU_QUIT: statistic();
  }
}


}