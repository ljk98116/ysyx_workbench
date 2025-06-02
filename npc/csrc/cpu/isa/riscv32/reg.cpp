#include <isa.hpp>
#include <isa/riscv32/reg.hpp>

namespace npc{
const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

void isa_reg_display() {
  printf("pc: 0x%x\n", cpu.pc);
  const int total_reg_num = 32;
  for(int i=0;i<total_reg_num;++i){
    printf("%s: 0x%x \n", reg_name(i), gpr(i));
  }
}

word_t isa_reg_str2val(const char *s, bool *success) {
  for(int i=0;i<32;++i){
    if(strcmp(reg_name(i), &s[1]) == 0){
      return gpr(i);
    }
  }
  if(strcmp(&s[1], "pc") == 0) return cpu.pc;
  printf("regname %s not found\n", s);
  return 0;
}    

}