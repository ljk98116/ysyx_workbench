#include <isa.hpp>
#include <isa/riscv32/reg.hpp>
#include <cpu/cpu.hpp>

namespace npc{
const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

void isa_reg_display(void *cpu_ptr, bool is_ref) {
  if(is_ref){
    CPU_state *ref_ptr = (CPU_state*)cpu_ptr;
    printf("ref pc: 0x%x\n", ref_ptr->pc);
    const int total_reg_num = 32;
    for(int i=0;i<total_reg_num / 4 ;++i){
      printf("%s: 0x%x \t", reg_name(4 * i), gpr(ref_ptr, 4 * i));
      printf("%s: 0x%x \t", reg_name(4 * i + 1), gpr(ref_ptr, 4 * i + 1));
      printf("%s: 0x%x \t", reg_name(4 * i + 2), gpr(ref_ptr, 4 * i + 2));
      printf("%s: 0x%x \n", reg_name(4 * i + 3), gpr(ref_ptr, 4 * i + 3));
    }
  }
  else{
    npc_CPU_state*dut_ptr = (npc_CPU_state*)cpu_ptr;
    if(commit_num){
      printf("npc pc: 0x%x\n", dut_ptr->pc[commit_num - 1]);
    }
    const int total_reg_num = 32;
    for(int i=0;i<total_reg_num / 4 ;++i){
      printf("%s: 0x%x \t", reg_name(4 * i), gpr(dut_ptr, 4 * i));
      printf("%s: 0x%x \t", reg_name(4 * i + 1), gpr(dut_ptr, 4 * i + 1));
      printf("%s: 0x%x \t", reg_name(4 * i + 2), gpr(dut_ptr, 4 * i + 2));
      printf("%s: 0x%x \n", reg_name(4 * i + 3), gpr(dut_ptr, 4 * i + 3));
    }    
  }
}

word_t isa_reg_str2val(const char *s, bool *success) {
  for(int i=0;i<32;++i){
    if(strcmp(reg_name(i), &s[1]) == 0){
      return gpr((&cpu), i);
    }
  }
  if(strcmp(&s[1], "pc") == 0) return cpu.pc[commit_num - 1];
  printf("regname %s not found\n", s);
  return 0;
}    

}