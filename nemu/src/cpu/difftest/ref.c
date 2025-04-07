/***************************************************************************************
* Copyright (c) 2014-2024 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <isa.h>
#include <cpu/cpu.h>
#include <difftest-def.h>
#include <memory/paddr.h>

__EXPORT void difftest_memcpy(paddr_t addr, void *buf, size_t n, bool direction) {
  // assert(0);
  if(direction == DIFFTEST_TO_DUT){
    
  }
  else{
    memcpy((char*)(uintptr_t)addr, buf, n);
  }
}

__EXPORT void difftest_regcpy(void *dut, bool direction) {
  // assert(0);
  CPU_state *state = (CPU_state*)dut;
  if(direction == DIFFTEST_TO_DUT){
    state->pc = cpu.pc;
    for(int i=0;i<MUXDEF(CONFIG_RVE, 16, 32);++i){
      state->gpr[i] = cpu.gpr[i];
    }
  }
  else{
    cpu.pc = state->pc;
    for(int i=0;i<MUXDEF(CONFIG_RVE, 16, 32);++i){
      cpu.gpr[i] = state->gpr[i];
    }    
  }
}

__EXPORT void difftest_exec(uint64_t n) {
  cpu_exec(n);
}

__EXPORT void difftest_raise_intr(word_t NO) {
  assert(0);
}

__EXPORT void difftest_init(int port) {
  void init_mem();
  init_mem();
  /* Perform ISA dependent initialization. */
  init_isa();
}
