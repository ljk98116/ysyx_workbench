#pragma once

#include <stdio.h>
#include <stdint.h>
#include <common.hpp>

namespace npc
{

typedef struct watchpoint {
  int NO;
  struct watchpoint *next;

  /* TODO: Add more members if necessary */
  char *expr; //监控点表达式
  word_t last_expr_value; //上一次的求值结果
} WP;

WP *new_WP();
void free_WP(WP *wp);
void AddWP(WP *wp);
int DeleteWP(int NO);
void watchpoint_step();
void print_watchpoints();

typedef uint32_t word_t;
void sdb_mainloop();
void init_monitor(int argc, char *argv[]);
void sdb_set_batch_mode();
word_t expr(char *e, bool *success);

}