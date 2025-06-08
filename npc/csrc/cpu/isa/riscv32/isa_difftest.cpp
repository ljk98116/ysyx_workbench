#include <isa.hpp>
#include <cpu/difftest.hpp>
#include "isa/riscv32/reg.hpp"
#include <cpu/cpu.hpp>

namespace npc{

bool isa_difftest_checkregs(CPU_state *ref_r) {
  if(commit_num == 0) return true;
  for(int i=0;i<commit_num;++i){
    Log("npc pc[%d]: 0x%x", i, cpu.pc[i]);
  }
  /* 逐个寄存器比对 */
  for(int i=0;i<32;++i){
    if(cpu.gpr[i] != ref_r->gpr[i]) return false;
  }
  return true;
}

void isa_difftest_attach() {
}

}