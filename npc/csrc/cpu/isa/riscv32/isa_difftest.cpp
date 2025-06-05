#include <isa.hpp>
#include <cpu/difftest.hpp>
#include "isa/riscv32/reg.hpp"
#include <cpu/cpu.hpp>

namespace npc{

bool isa_difftest_checkregs(CPU_state *ref_r) {
  if(commit_num == 0) return true;
  vaddr_t dut_pc = cpu.pc[commit_num - 1];
  if(dut_pc != ref_r->pc) return false;
  /* 逐个寄存器比对 */
  for(int i=0;i<32;++i){
    if(cpu.gpr[i] != ref_r->gpr[i]) return false;
  }
  return true;
}

void isa_difftest_attach() {
}

}