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

#include "sdb.h"

#define NR_WP 32

static WP wp_pool[NR_WP] = {};
//head即当前的活动的watchpoint链表
//free是空闲watchpoint链表的表头
static WP *head = NULL, *free_ = NULL;

void init_wp_pool() {
  int i;
  for (i = 0; i < NR_WP; i ++) {
    wp_pool[i].NO = i;
    wp_pool[i].next = (i == NR_WP - 1 ? NULL : &wp_pool[i + 1]);
    wp_pool[i].expr = NULL;
    wp_pool[i].last_expr_value = 0;
  }

  head = NULL;
  free_ = wp_pool;
}

/* TODO: Implement the functionality of watchpoint */

/* 分配新节点插入head */
WP *new_WP(){
  /* 无空闲 */
  if(!free_){
    Log("No free watchpoint here");
    assert(0);
    return NULL;
  }
  /* 选取第一个WP返回，移动free_到下一个位置 */
  WP *ret = free_;
  free_ = free_->next;
  ret->next = NULL;
  return ret;
}

/* 作为头部插入 */
void free_WP(WP *wp){
  if(!wp){
    Log("Error: try to free nullptr");
    assert(0);
    return;
  }
  if(wp->expr) free(wp->expr); //释放表达式的堆内存
  wp->last_expr_value = 0; //放弃原有表达式值
  wp->next = free_;
  free_ = wp;
}

/* 插入head链表 */
void AddWP(WP *wp){
  if(!head) head = wp;
  else{
    WP *cur = head;
    while(cur->next != NULL) cur = cur->next;
    cur->next = wp;
  }
}

/* 删除指定的WP */
int DeleteWP(int NO){
  WP *cur = head;
  bool found = false;
  WP *prev = NULL;
  while(cur != NULL){
    if(cur->NO == NO){
      found = true;
      /* 从head删除cur */
      if(prev == NULL){
        head = cur->next;
        cur->next = NULL;
      }
      else{
        prev->next = cur->next;
      }
      /* 释放该WP */
      free_WP(cur);
      Log("watchpoint %d deleted successfully", NO);
      return found;
    }
    prev = cur;
    cur = cur->next;
  }
  Log("watchpoint %d not found", NO);
  return found;
}

void watchpoint_step(){
  WP *cur = head;
  bool triggered = false;
  while(cur != NULL){
    bool success = true;
    word_t res = expr(cur->expr, &success);
    if(!success){
      Log("Error: watchpoint %d calculate expr: %s failed", cur->NO, cur->expr);
      assert(0);
    }
    if(res != cur->last_expr_value){
      printf("Hardware watchpoint %d: %s\n", cur->NO, cur->expr);
      printf("Old Value = %u\n", cur->last_expr_value);
      printf("New Value = %u\n", res);
      triggered = true;
    }
    cur->last_expr_value = res;
    cur = cur->next;
  }
  if(triggered) nemu_state.state = NEMU_STOP;
}

void print_watchpoints(){
  WP *cur = head;
  while(cur != NULL){
    printf("watchpoint %d: %s\n", cur->NO, cur->expr);
    cur = cur->next;
  }
}