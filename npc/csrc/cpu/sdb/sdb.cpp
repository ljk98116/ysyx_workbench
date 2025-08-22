#include <sdb/sdb.hpp>

#include <readline/readline.h>
#include <readline/history.h>
#include <stdlib.h>
#include <utils.hpp>

#include <cpu/cpu.hpp>
#include <isa.hpp>
#include <memory/paddr.hpp>

// use vcd
#if CONFIG_USE_VCD
#include <verilated_fst_c.h>
#endif

#include <VCPUCore.h>

namespace npc{
static int is_batch_mode = false;
static void* tfp_cur;

void init_regex();
void init_wp_pool();

/* We use the `readline' library to provide more flexibility to read from stdin. */
static char* rl_gets() {
  static char *line_read = NULL;

  if (line_read) {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(npc) ");

  if (line_read && *line_read) {
    add_history(line_read);
  }

  return line_read;
}

static int cmd_c(char *args) {
  cpu_exec(-1, tfp_cur);
  return 0;
}


static int cmd_q(char *args) {
  nemu_state.state = NEMU_QUIT;
  return -1;
}

static int cmd_help(char *args);

static int cmd_si(char *args){
  word_t N = 1;
  if(args) N = atoll(args);
  cpu_exec(N, tfp_cur);
  return 0;
}

static int cmd_info(char *args){
  if(strlen(args) > 1 || strlen(args) == 0){
    printf("info cmd illegal\n");
    return 0;
  }
  /* 打印寄存器 */
  if(args[0] == 'r'){
    isa_reg_display(&cpu, false);
  }
  /* 打印监视点 */
  // if(args[0] == 'w'){
  //   print_watchpoints();
  // }
  return 0;
}

static int cmd_x(char *args){
  int expr_start_idx = 0;
  char *expr_str = NULL;
  char *N_str = args;
  int good = 0;
  for(int i=0;i<strlen(args);++i){
    //截取表达式，空格后的字符串
    if(args[i] == ' ') {
      expr_start_idx = i + 1;
      good = 1;
      expr_str = &args[expr_start_idx];
      break;
    }
  }
  if(!good || !expr_str){
    printf("illegal input for x command\n");
    return 0;
  }

  /* 假设表达式为16进制 */
  /* 求表达式值 */
  bool success = true;
  word_t paddr = expr(expr_str, &success);
  //求出输出的4字节的个数
  args[expr_start_idx] = '\0';
  word_t N = atoll(N_str);
  for(int i=0;i<N;++i){
    word_t rval = paddr_read(paddr + (i << 2), 4);
    printf("0x%08x ", rval);
  }
  printf("\n");
  return success == true ? 0 : -1;
}

static int cmd_p(char *args){
  /* 假设表达式为16进制 */
  bool success = true;
  word_t res = expr(args, &success);
  printf("%u\n", res);
  return success == true ? 0 : -1; 
}

static struct {
  const char *name;
  const char *description;
  int (*handler) (char *);
} cmd_table [] = {
  { "help", "Display information about all supported commands", cmd_help },
  { "c", "Continue the execution of the program", cmd_c },
  { "q", "Exit NEMU", cmd_q },

  /* TODO: Add more commands */
  { "si", "Step N period, default is 1", cmd_si},
  { "info", "get info for register[r] or watchpoints[w]", cmd_info},
  { "x", "output the expr value within N * 4 bytes in format hex", cmd_x},
  { "p", "get expr value", cmd_p},
  // { "w", "set watchpoint", cmd_w},
  // { "d", "del watchpoint id N", cmd_d}
};

#define NR_CMD ARRLEN(cmd_table)

static int cmd_help(char *args) {
  /* extract the first argument */
  char *arg = strtok(NULL, " ");
  int i;

  if (arg == NULL) {
    /* no argument given */
    for (i = 0; i < NR_CMD; i ++) {
      printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
    }
  }
  else {
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(arg, cmd_table[i].name) == 0) {
        printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
        return 0;
      }
    }
    printf("Unknown command '%s'\n", arg);
  }
  return 0;
}

void sdb_set_batch_mode() {
  is_batch_mode = true;
}

void sdb_mainloop(){
  cycle = 0;
  void *tfp = nullptr;
#if CONFIG_USE_VCD
  tfp = (void*)(new VerilatedFstC); // 创建 VCD 对象
  tfp_cur = tfp;
#endif
  cpu_reset(tfp);
  if (is_batch_mode) {
    cmd_c(NULL);
    return;
  }

  for (char *str; (str = rl_gets()) != NULL; ) {
    char *str_end = str + strlen(str);

    /* extract the first token as the command */
    char *cmd = strtok(str, " ");
    if (cmd == NULL) { continue; }

    /* treat the remaining string as the arguments,
     * which may need further parsing
     */
    char *args = cmd + strlen(cmd) + 1;
    if (args >= str_end) {
      args = NULL;
    }

#ifdef CONFIG_DEVICE
    extern void sdl_clear_event_queue();
    sdl_clear_event_queue();
#endif

    int i;
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(cmd, cmd_table[i].name) == 0) {
        if (cmd_table[i].handler(args) < 0) { 
      #if CONFIG_USE_VCD
          ((VerilatedFstC*)tfp)->close();
          if(tfp != nullptr) {
            delete (VerilatedFstC*)tfp;
            tfp_cur = nullptr;
            tfp = nullptr;
          }
      #endif
          return; 
        }
        break;
      }
    }

    if (i == NR_CMD) { printf("Unknown command '%s'\n", cmd); }
  }
}

void init_sdb() {
  /* Compile the regular expressions. */
  init_regex();

  /* Initialize the watchpoint pool. */
  // init_wp_pool();
}

}