#include <cpu/cpu.hpp>
#include <cpu/difftest.hpp>

#include <isa.hpp>
#include <locale.h>
#include <utils.hpp>

#include <chrono>

#include "VCPUCore.h"

namespace npc{
#define MAX_INST_TO_PRINT 10

/* npc state */
npc_CPU_state cpu = {};
uint8_t retire_RAT[32];
uint8_t rename_RAT[32];
btb_item_t btb_table[1 << 13];
uint8_t gPHT[1 << 13];
uint8_t lPHT[1 << 13];
uint8_t cPHT[1 << 13];
rob_item_t rob_table[4][1 << 7];
uint32_t rob_id_loc_mem[1 << 7];

uint8_t commit_num;
uint32_t cycle;
uint32_t total_branch_cnt;
uint32_t branch_err_cnt;

bool ref_stop;

static VCPUCore dut;

uint64_t g_nr_guest_inst = 0;
static uint64_t g_timer = 0; // unit: us
static bool g_print_step = false;

static void trace_and_difftest() {
#ifdef CONFIG_ITRACE_COND
  if (ITRACE_COND) { npc_log_write("%s\n", _this->logbuf); }
#endif
  // if (g_print_step) { IFDEF(CONFIG_ITRACE, puts(_this->logbuf)); }
#if CONFIG_DIFFTEST
  difftest_step();
#else
  static uint32_t last_npc_pc[4];
  static uint32_t last_npc_regs[32];
  if(
    (memcmp(last_npc_pc, cpu.pc, sizeof(last_npc_pc)) == 0) &&
    (memcmp(last_npc_regs, cpu.gpr, sizeof(last_npc_regs)) == 0)
  ){
    ref_stop = true;
    nemu_state.state = NEMU_END;
    return;
  }
  memcpy(last_npc_pc, cpu.pc, sizeof(last_npc_pc));
  memcpy(last_npc_regs, cpu.gpr, sizeof(last_npc_regs));
#endif
  // IFDEF(CONFIG_WATCH_POINT, watchpoint_step());
}

static void statistic() {
  IFNDEF(CONFIG_TARGET_AM, setlocale(LC_NUMERIC, ""));
#define NPC_NUMBERIC_FMT MUXDEF(CONFIG_TARGET_AM, "%", "%'") PRIu64
  NPCLog("ipc %lf", (double)g_nr_guest_inst / (cycle * 2));
  NPCLog("single cycle cost %lf us", (double)g_timer / cycle / 2.0);
  NPCLog("host time spent = " NPC_NUMBERIC_FMT " us", g_timer);
  NPCLog("total guest instructions = " NPC_NUMBERIC_FMT, g_nr_guest_inst);
  NPCLog("total branch predict success rate: %.2lf %", 100.0 - 100.0 * (double)branch_err_cnt / (double)(total_branch_cnt));
  if (g_timer > 0) NPCLog("simulation frequency = " NPC_NUMBERIC_FMT " inst/s", g_nr_guest_inst * 1000000 / g_timer);
  else NPCLog("Finish running in less than 1 us and can not calculate the simulation frequency");
}

void assert_fail_msg() {
  isa_reg_display(&cpu, false);
  statistic();
}

/* 执行一个周期 */
static void exec_once(void* tfp){
  commit_num = 0;
  static bool simflag = false;
  dut.clock = 0; dut.eval();
  dut.clock = 1; dut.eval();
  //vcd记录仿真结果
#if CONFIG_USE_VCD
  if(tfp != nullptr) {
    ((VerilatedFstC*)tfp)->dump(cycle);
  }
#endif
  /* 存在指令提交,进行difftest */
  if(commit_num > 0){
    // Log("npc commit_num:%d at %d th cycle", commit_num, cycle);
    trace_and_difftest();
    g_nr_guest_inst += commit_num;
  }
  if(cycle % 100000 == 0 & cycle > 0) NPCLog("running cycle %d", cycle);
  ++cycle;
}

/* 执行n个周期 */
static void execute(uint64_t n, void* tfp) {
  while(n-- > 0 && !ref_stop && nemu_state.state != NEMU_ABORT && nemu_state.state != NEMU_END) {
    exec_once(tfp);
  }
}

void cpu_reset(void* tfp){
  dut.reset = 1;
#if CONFIG_USE_VCD
  if(tfp != nullptr){
    dut.trace((VerilatedFstC*)tfp, 99); // 跟踪所有信号（99=递归深度）
    ((VerilatedFstC*)tfp)->open("wave.fst"); // 输出文件名
  }
#endif
  int n = 5;
  while (n -- > 0) exec_once(tfp);
  dut.reset = 0;  
  ref_stop = false;
}

/* Simulate how the CPU works. */
void cpu_exec(uint64_t n, void* tfp) {
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
      NPCLog("nemu: %s at pc = " NPC_FMT_WORD,
          (nemu_state.state == NEMU_ABORT ? NPC_ANSI_FMT("ABORT", NPC_ANSI_FG_RED) :
           (nemu_state.halt_ret == 0 ? NPC_ANSI_FMT("HIT GOOD TRAP", NPC_ANSI_FG_GREEN) :
            NPC_ANSI_FMT("HIT BAD TRAP", NPC_ANSI_FG_RED))),
          nemu_state.halt_pc);
      // fall through
    case NEMU_QUIT: statistic();
  }
}


}