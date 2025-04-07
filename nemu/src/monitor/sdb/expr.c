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
#include <memory/paddr.h>
/* We use the POSIX regex functions to process regular expressions.
 * Type 'man regex' for more information about POSIX regex functions.
 */
#include <regex.h>

static word_t str2int(char *num, int target, bool *success){
  unsigned long ret = 0;
  unsigned long cur = 1;
  for(int i=strlen(num) - 1;i>= (target == 16 ? 2 : 0);--i){
    if(num[i] >= 'A' && num[i] <= 'F') ret += (num[i] - 'A' + 10) * cur;
    else if(num[i] >= 'a' && num[i] <= 'f') ret += (num[i] - 'a' + 10) * cur;
    else if(num[i] >= '0' && num[i] <= '9') ret += (num[i] - '0') * cur;
    else{
      *success = false;
      return 0;
    }
    cur *= target;
  }
  *success = true;
  return ret;
}

enum {
  TK_NOTYPE = 256, TK_EQ, TK_NEQ,
  TK_NUMBER, 
  TK_HEX,
  TK_SIGN,
  TK_REG,
  TK_DEREF,
  TK_AND,
  TK_OR,
  /* TODO: Add more token types */
  TK_PLUS = '+',
  TK_MINUS = '-',
  TK_MULT = '*',
  TK_DIV = '/',
  TK_LEFTPAR = '(',
  TK_RIGHTPAR = ')'
};

static struct rule {
  const char *regex;
  int token_type;
} rules[] = {

  /* TODO: Add more rules.
   * Pay attention to the precedence level of different rules.
   */

  {" +", TK_NOTYPE},     // spaces
  {"\\+", '+'},          // plus
  {"\\-", '-'},          // minus
  {"\\*", '*'},          // mult
  {"/", '/'},            // div
  {"==", TK_EQ},         // equal
  {"!=", TK_NEQ},        // not equal
  {"\\(", '('},          // (
  {"\\)", ')'},          // )
  {"0[xX][0-9|a-f|A-F]+", TK_HEX},   // hex number
  {"[0-9]+", TK_NUMBER}, // number
  {"\\$[a-z|A-Z|0-9]+", TK_REG},       // register
  {"&&", TK_AND},        // and
  {"\\|\\|", TK_OR}          // or
};

#define NR_REGEX ARRLEN(rules)

static regex_t re[NR_REGEX] = {};

/* Rules are used for many times.
 * Therefore we compile them only once before any usage.
 */
void init_regex() {
  int i;
  char error_msg[128];
  int ret;

  for (i = 0; i < NR_REGEX; i ++) {
    ret = regcomp(&re[i], rules[i].regex, REG_EXTENDED);
    if (ret != 0) {
      regerror(ret, &re[i], error_msg, 128);
      panic("regex compilation failed: %s\n%s", error_msg, rules[i].regex);
    }
  }
}

typedef struct token {
  int type;
  char str[32];
} Token;

static Token tokens[65535] __attribute__((used)) = {};
static int nr_token __attribute__((used))  = 0;

static bool make_token(char *e) {
  int position = 0;
  int i;
  regmatch_t pmatch;

  nr_token = 0;

  while (e[position] != '\0') {
    /* Try all rules one by one. */
    for (i = 0; i < NR_REGEX; i ++) {
      if (regexec(&re[i], e + position, 1, &pmatch, 0) == 0 && pmatch.rm_so == 0) {
        char *substr_start = e + position;
        int substr_len = pmatch.rm_eo;

        Log("match rules[%d] = \"%s\" at position %d with len %d: %.*s",
            i, rules[i].regex, position, substr_len, substr_len, substr_start);

        if(substr_len >= 32) {
          panic("too long for a token at position %d with len %d: %.*s", position, substr_len, substr_len, substr_start);
        }
        position += substr_len;

        /* TODO: Now a new token is recognized with rules[i]. Add codes
         * to record the token in the array `tokens'. For certain types
         * of tokens, some extra actions should be performed.
         */

        switch (rules[i].token_type) {
          case TK_NOTYPE: break;
          case TK_DIV:
          case TK_EQ:
          case TK_NEQ:
          case TK_HEX:
          case TK_LEFTPAR:
          case TK_MINUS:
          case TK_MULT:
          case TK_NUMBER:
          case TK_PLUS:
          case TK_RIGHTPAR: 
          case TK_AND:
          case TK_OR:
          case TK_REG:{
            tokens[nr_token].type = rules[i].token_type;
            memset(tokens[nr_token].str, 0, sizeof(tokens[nr_token].str));
            memcpy(tokens[nr_token].str, substr_start, substr_len);
            ++nr_token;
            break;
          }
          default: break;
        }
        break;
      }
    }

    if (i == NR_REGEX) {
      printf("no match at position %d\n%s\n%*.s^\n", position, e, position, "");
      return false;
    }
  }

  return true;
}

static bool check_parentheses(int start, int end){
  if(tokens[start].type != TK_LEFTPAR || tokens[end].type != TK_RIGHTPAR) return false;
  //是否内部括号成对
  int is_pair = 0;
  for(int i=start+1;i<=end-1;++i){
    if(is_pair < 0) return false;
    if(tokens[i].type == TK_LEFTPAR) ++is_pair;
    if(tokens[i].type == TK_RIGHTPAR) --is_pair;
  }
  return is_pair == 0;
}

static int find_patition_op(int start, int end){
  int target_idx = -1;
  int target_type = -1; //错误情况
  int in_parenthesis = 0;
  for(int i=start; i <= end; ++i){
    switch(tokens[i].type){
      case TK_PLUS:
      case TK_MINUS:{
        if(!in_parenthesis){
          if(target_idx == -1 
          || target_type == TK_MULT || target_type == TK_DIV
          || target_type == TK_PLUS || target_type == TK_MINUS){
            target_idx = i;
            target_type = tokens[i].type;
          }
        }
        break;
      }
      case TK_MULT:
      case TK_DIV:{
        if(!in_parenthesis){
          if(target_idx == -1 || target_type == TK_MULT || target_type == TK_DIV){
            target_idx = i;
            target_type = tokens[i].type;
          }
        }
        break;
      }
      case TK_EQ:
      case TK_NEQ:{
        if(!in_parenthesis){
          if(target_idx == -1 
          || target_type == TK_MULT || target_type == TK_DIV
          || target_type == TK_PLUS || target_type == TK_MINUS
          || target_type == TK_EQ || target_type == TK_NEQ){
            target_idx = i;
            target_type = tokens[i].type;
          }          
        }
        break;
      }
      case TK_AND:
      case TK_OR:{
        if(!in_parenthesis){
          if(target_idx == -1 
          || target_type == TK_MULT || target_type == TK_DIV
          || target_type == TK_PLUS || target_type == TK_MINUS
          || target_type == TK_EQ || target_type == TK_NEQ
          || target_type == TK_AND || target_type == TK_OR){
            target_idx = i;
            target_type = tokens[i].type;
          }          
        }
        break;
      }
      case TK_LEFTPAR:{
        in_parenthesis += 1;
        break;
      }
      case TK_RIGHTPAR:{
        in_parenthesis -= 1;
        break;
      }
      case TK_NUMBER:
      case TK_HEX:{
        break;
      }
    }
  }
  return target_idx;
}

/* start: token起始序号, end: token最后一个序号 */
static word_t eval(int start, int end, bool *success){
  if(end < start){
    *success = false;
    return 0;
  }
  //找优先级最低的最后一个运算符对应的token
  if(start == end){
    if(tokens[start].type != TK_NUMBER && tokens[start].type != TK_HEX && tokens[start].type != TK_REG){
      *success = false;
      panic("expr cal result is not a number");
      return 0;
    }
    if(tokens[start].type == TK_NUMBER) return str2int(tokens[start].str, 10u, success);
    if(tokens[start].type == TK_HEX) return str2int(tokens[start].str, 16u, success);
    if(tokens[start].type == TK_REG) return isa_reg_str2val(tokens[start].str, success);
    *success = false;
    panic("expr cal result is not a number");
    return 0;
  }
  /* 带括号 */
  if(check_parentheses(start, end) == true){
    return eval(start + 1, end - 1, success);
  }
  /* 解引用, 负数, 读寄存器 */
  if(tokens[start].type == TK_SIGN) {
    return -eval(start + 1, end, success);
  }
  if(tokens[start].type == TK_DEREF){
    uint32_t paddr = eval(start + 1, end, success);
    word_t ret = paddr_read(paddr, 4);
    return ret;
  }
  int target_idx = find_patition_op(start, end);
  if(target_idx == -1) {
    printf("%d %d %s %s\n", start, end, tokens[start].str, tokens[end].str);
    *success = false;
    panic("expr can not calculate");
  }
  word_t val1 = eval(start, target_idx - 1, success);
  word_t val2 = eval(target_idx + 1, end, success);
  switch(tokens[target_idx].type){
    case TK_PLUS:{
      return val1 + val2;
    }
    case TK_MINUS:{
      return val1 - val2;
    }
    case TK_MULT:{
      return val1 * val2;
    }
    case TK_DIV:{
      if(val2 == 0) {
        printf("warning div 0 in expr\n");
      }
      return val1 / val2;
    }
    case TK_AND:{
      return val1 && val2;
    }
    case TK_OR:{
      return val1 || val2;
    }
    case TK_EQ:{
      return val1 == val2;
    }
    case TK_NEQ:{
      return val1 != val2;
    }
    default:{
      *success = false;
      panic("not implmented");
    }
  }
  return 0;
}

word_t expr(char *e, bool *success) {
  if (!make_token(e)) {
    *success = false;
    return 0;
  }
  /* check deref */
  for(int i=0;i<nr_token;++i){
    //这个地方添加了对括号的筛选
    if(tokens[i].type == TK_MULT && (i == 0 || 
        (i > 0 && tokens[i-1].type != TK_NUMBER && tokens[i-1].type != TK_HEX
        && tokens[i-1].type != TK_LEFTPAR && tokens[i-1].type != TK_RIGHTPAR)
      )){
      tokens[i].type = TK_DEREF;
    }
    if(tokens[i].type == TK_MINUS && 
      (i == 0 || (i > 0 && tokens[i-1].type != TK_NUMBER && tokens[i-1].type != TK_HEX && tokens[i-1].type != TK_LEFTPAR && tokens[i-1].type != TK_RIGHTPAR)
    )){
      tokens[i].type = TK_SIGN;
    }
  }
  /* TODO: Insert codes to evaluate the expression. */
  word_t ret = eval(0, nr_token - 1, success);
  return ret;
}