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

#include <cpu/cpu.h>
#include <cpu/decode.h>
#include <cpu/difftest.h>
#include <cpu/ifetch.h>

#include <locale.h>

#include <memory/iringbuf.h>

/* The assembly code of instructions executed is only output to the screen
 * when the number of instructions executed is less than this value.
 * This is useful when you use the `si' command.
 * You can modify this value as you want.
 */
#define MAX_INST_TO_PRINT 10

CPU_state cpu = {};
uint64_t g_nr_guest_inst = 0;
static uint64_t g_timer = 0; // unit: us
static bool g_print_step = false;

void device_update();
void watchpoint_step();
static void trace_and_difftest(Decode *_this, vaddr_t dnpc) {
#ifdef CONFIG_ITRACE_COND
  if (ITRACE_COND) { log_write("%s\n", _this->logbuf); }
#endif
  if (g_print_step) { IFDEF(CONFIG_ITRACE, puts(_this->logbuf)); }
  IFDEF(CONFIG_DIFFTEST, difftest_step(_this->pc, dnpc));
  IFDEF(CONFIG_WATCH_POINT, watchpoint_step());
}

#ifdef CONFIG_FTRACE
static elf_func_t *func_table_find(word_t target){
  for(int i=0;i<50000;++i){
    if(func_table[i].start == target){
      return &func_table[i];
    }
  }
  return NULL;
}
#endif

#define immUJ_(imm, i) do{ *imm = (SEXT(BITS(i, 31, 31), 1) << 20) | \
  (BITS(i, 19, 12) << 12) | \
  (BITS(i, 30, 21) << 1) | \
  (BITS(i, 20, 20) << 11);} while(0)

static void exec_once(Decode *s, vaddr_t pc) {
  //recv call/ret save to ftrace_buf
  //ret : jalr x0, x1, 0
  //call : auipc x1, offset[31:12]
  //       jalr x1, x1, offset[11:0]
  //jal: 
  //check call offset
#ifdef CONFIG_FTRACE
  //是否有对应call的auipc指令
  static int ready_call = 0;
  static word_t call_offset = 0;
  static elf_func_t *func_stack[600];
  static int func_stack_top = -1;
  static char fbuf[FTRACE_INFO_SIZE];
#endif
#if CONFIG_ISA_riscv
  //recv call
  if(ready_call == 0){
    if(((s->isa.inst & 0b1111111) == 0b0010111) && (((s->isa.inst >> 7) & 0b11111) == 1)){
      ready_call = 1;
      call_offset |= (s->isa.inst >> 12) << 12;
    }
  }
  else{
      if(((s->isa.inst & 0b1111111) == 0b1100111) && 
        (((s->isa.inst >> 7) & 0b11111) == 1) && 
        (((s->isa.inst >> 15) & 0b11111) == 1)){
          call_offset |= (s->isa.inst >> 20);
      //write to ftrace_buf
      #ifdef CONFIG_FTRACE
          memset(fbuf, '\0', sizeof(fbuf));
          //locate the function name
          word_t call_target = (s->pc + call_offset) & 0xfffffffe;
          elf_func_t *func = func_table_find(call_target);
          func_stack[++func_stack_top] = func;
          Assert(func != NULL, "func can not be found in elf");
          if(func != NULL){
            sprintf(fbuf, "pc: 0x%08x call 0x%x[%s@0x%x]\n", s->pc, call_offset, func->name, func->start);
            iringbuf_write(ftrace_buf, fbuf, FTRACE_INFO_SIZE);
          }
      #endif
          call_offset = 0;
          ready_call = 0;
      }
  }
  //recv ret
  if(((s->isa.inst & 0b1111111) == 0b1100111) &&
      (((s->isa.inst >> 7) & 0b11111) == 0) && 
      (((s->isa.inst >> 15) & 0b11111) == 1)
  ){
    //write to ftrace_buf
    #ifdef CONFIG_FTRACE
        memset(fbuf, '\0', sizeof(fbuf));
        Assert(func_stack_top != -1, "func can not be found in elf");        
        sprintf(fbuf, "pc: 0x%08x ret[%s@0x%x]\n", s->pc, func_stack[func_stack_top]->name, func_stack[func_stack_top]->start);
        --func_stack_top;
        iringbuf_write(ftrace_buf, fbuf, FTRACE_INFO_SIZE);
    #endif    
  }
  else if(((s->isa.inst & 0b1111111) == 0b1100111)){
    //write to ftrace_buf
    #ifdef CONFIG_FTRACE
        int reg_id = (s->isa.inst >> 15) & 0b11111;
        word_t call_target = cpu.gpr[reg_id];
        elf_func_t *func = func_table_find(call_target);
        Assert(func != NULL, "func can not be found at 0x%x", call_target);    
        func_stack[++func_stack_top] = func;    
        memset(fbuf, '\0', sizeof(fbuf));     
        sprintf(fbuf, "pc: 0x%08x call[%s@0x%x]\n", s->pc, func_stack[func_stack_top]->name, func_stack[func_stack_top]->start);
        iringbuf_write(ftrace_buf, fbuf, FTRACE_INFO_SIZE);
    #endif
  }
  //recv jal
  if((s->isa.inst & 0b1111111) == 0b1101111){
#ifdef CONFIG_FTRACE
    //计算偏移量
    word_t imm;
    immUJ_(&imm, s->isa.inst);
    //求出目标跳转地址
    word_t call_target = s->pc + imm;
    //找函数名称，更新函数名称栈
    elf_func_t *func = func_table_find(call_target);
    if(func != NULL){
      func_stack[++func_stack_top] = func;
      memset(fbuf, '\0', sizeof(fbuf));
      Assert(func_stack_top != -1, "func can not be found in elf");        
      sprintf(fbuf, "pc: 0x%08x call[%s@0x%x]\n", s->pc, func_stack[func_stack_top]->name, func_stack[func_stack_top]->start);
      iringbuf_write(ftrace_buf, fbuf, FTRACE_INFO_SIZE); 
    }
#endif
  }
#endif
  s->pc = pc;
  s->snpc = pc;
  isa_exec_once(s);
  cpu.pc = s->dnpc;
#ifdef CONFIG_ITRACE
  char *p = s->logbuf;
  p += snprintf(p, sizeof(s->logbuf), FMT_WORD ":", s->pc);
  int ilen = s->snpc - s->pc;
  int i;
  uint8_t *inst = (uint8_t *)&s->isa.inst;
#ifdef CONFIG_ISA_x86
  for (i = 0; i < ilen; i ++) {
#else
  for (i = ilen - 1; i >= 0; i --) {
#endif
    p += snprintf(p, 4, " %02x", inst[i]);
  }
  int ilen_max = MUXDEF(CONFIG_ISA_x86, 8, 4);
  int space_len = ilen_max - ilen;
  if (space_len < 0) space_len = 0;
  space_len = space_len * 3 + 1;
  memset(p, ' ', space_len);
  p += space_len;

  void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
  disassemble(p, s->logbuf + sizeof(s->logbuf) - p,
      MUXDEF(CONFIG_ISA_x86, s->snpc, s->pc), (uint8_t *)&s->isa.inst, ilen);
  
  static char buf[ITRACE_INFO_SIZE];
  memset(buf, '\0', sizeof(buf));
  sprintf(buf, "pc: 0x%08x inst: 0x%08x %s\n", s->pc, s->isa.inst, p);
  //save to itrace_buf
  iringbuf_write(itrace_buf, buf, ITRACE_INFO_SIZE);
#endif
}

static void execute(uint64_t n) {
  Decode s;
  for (;n > 0; n --) {
    exec_once(&s, cpu.pc);
    g_nr_guest_inst ++;
    trace_and_difftest(&s, cpu.pc);
    if (nemu_state.state != NEMU_RUNNING) break;
    IFDEF(CONFIG_DEVICE, device_update());
  }
}

static void statistic() {
  IFNDEF(CONFIG_TARGET_AM, setlocale(LC_NUMERIC, ""));
#define NUMBERIC_FMT MUXDEF(CONFIG_TARGET_AM, "%", "%'") PRIu64
  Log("host time spent = " NUMBERIC_FMT " us", g_timer);
  Log("total guest instructions = " NUMBERIC_FMT, g_nr_guest_inst);
  if (g_timer > 0) Log("simulation frequency = " NUMBERIC_FMT " inst/s", g_nr_guest_inst * 1000000 / g_timer);
  else Log("Finish running in less than 1 us and can not calculate the simulation frequency");
#ifdef CONFIG_ITRACE
  Log("****** NEMU ITRACE RESULT/%d ******\n", ITRACE_INST_NUM);
  //打印itrace_buf内的全部内容
  iringbuf_print(itrace_buf);
  //销毁itrace_buf
  iringbuf_destroy(itrace_buf);
#endif
#ifdef CONFIG_MTRACE
  Log("****** NEMU MTRACE RESULT/%d ******\n", MTRACE_NUM);
  iringbuf_print(mtrace_buf);
  iringbuf_destroy(mtrace_buf);
#endif
#ifdef CONFIG_FTRACE
  Log("****** NEMU FTRACE RESULT/%d ******\n", FTRACE_NUM);
  iringbuf_print(ftrace_buf);
  iringbuf_destroy(ftrace_buf);
  free(elf_symtab);
  free(elf_strtab);
#endif
}

void assert_fail_msg() {
  isa_reg_display();
  statistic();
}

/* Simulate how the CPU works. */
void cpu_exec(uint64_t n) {
  g_print_step = (n < MAX_INST_TO_PRINT);
  switch (nemu_state.state) {
    case NEMU_END: case NEMU_ABORT: case NEMU_QUIT:
      printf("Program execution has ended. To restart the program, exit NEMU and run again.\n");
      return;
    default: nemu_state.state = NEMU_RUNNING;
  }

  uint64_t timer_start = get_time();

  execute(n);

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
