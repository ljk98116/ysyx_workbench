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

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <assert.h>
#include <string.h>

// this should be enough
static char buf[65535] = {};
static char code_buf[65535 + 128] = {}; // a little larger than `buf`
static char *code_format =
"#include <stdio.h>\n"
"int main() { "
"  unsigned result = %s; "
"  printf(\"%%u\", result); "
"  return 0; "
"}";
static int buf_end = 0;
static uint32_t choose(uint32_t n){
  return rand() % n;
}

static void abort_expr(int pos){
  memset(&buf[pos], 0, sizeof(buf) - buf_end - 1);
}

static int gen_num(){
  int pos = buf_end;
  uint32_t num = rand() % UINT32_MAX;
  int choise = choose(2);
  uint32_t target = 10;
  if(choise == 1) target = 16;

  //if(buf_end + strlen("(unsigned int)") >= sizeof(buf)) goto bad;

  //strcpy(&buf[buf_end], "(unsigned int)");
  //buf_end += strlen("(unsigned int)");
  if(target == 16){
    if(buf_end + 2 >= sizeof(buf)) goto bad;
    buf[buf_end++] = '0', buf[buf_end++] = 'x';
  }
  int start = buf_end;
  while(num){
    int now = num % target;
    if(buf_end >= sizeof(buf)) goto bad;
    if(now >= 10 && target == 16) buf[buf_end++] = now - 10 + 'A';
    else buf[buf_end++] = now + '0';
    num /= target;
  }
  int end = buf_end - 1;
  while(start < end){
    char tmp = buf[start];
    buf[start] = buf[end];
    buf[end] = tmp;
    ++start;
    --end;
  }
  if(buf_end + 1 >= sizeof(buf)) goto bad;
  buf[buf_end++] = 'u';
  return 0;
bad:
  abort_expr(pos);
  return -1;
}

/* +, -, *, / */
static int gen_rand_op(){
  int id = choose(4);
  if(buf_end + 1 == sizeof(buf)) {abort_expr(buf_end); return -1;}
  switch(id){
    case 0: {buf[buf_end++] = '+'; break;}
    case 1: {buf[buf_end++] = '-'; break;}
    case 2: {buf[buf_end++] = '*'; break;}
    case 3: {buf[buf_end++] = '/'; break;}
  }
  return 0;
}

static int gen(char c){
  if(buf_end + 1 >= sizeof(buf)) return -1;
  buf[buf_end++] = c;
  return 0;
}

static int gen_rand_expr(int depth) {
  int pos = buf_end;
  /* 防止爆栈 */
  if(depth >= 20) return -1;
  switch (choose(3)) {
    case 0: {
      if(gen_num() == -1){
        abort_expr(pos);
        return -1;
      }
      return 0;
    }
    case 1: {
      if(gen('(') == -1) return -1;
      if(gen_rand_expr(depth+1) == -1) {
        abort_expr(pos);
        return -1;
      } 
      if(gen(')') == -1){
        abort_expr(pos);
        return -1;
      }
      return 0;
    }
    default: {
      if(gen_rand_expr(depth+1) == -1){
        abort_expr(pos);
        return -1;
      } 
      if(gen_rand_op() == -1){
        abort_expr(pos);
        return -1;
      }
      if(gen_rand_expr(depth+1) == -1){
        abort_expr(pos);
        return -1;
      }

      return 0;
    }
  }
  return -1;
}

int main(int argc, char *argv[]) {
  int seed = time(0);
  srand(seed);
  int loop = 1;
  if (argc > 1) {
    sscanf(argv[1], "%d", &loop);
  }
  int i;
  for (i = 0; i < loop; i ++) {
    buf_end = 0;
    memset(buf, 0, sizeof(buf));
    if(gen_rand_expr(0) == -1) {
      //printf("%d th gen expr failed\n", i);
      --i;
      continue;
    }
    
    //buf_end += strlen("*1u");
    sprintf(code_buf, code_format, buf);

    FILE *fp = fopen("/tmp/.code.c", "w");
    assert(fp != NULL);
    fputs(code_buf, fp);
    fclose(fp);

    int ret = system("gcc /tmp/.code.c -o /tmp/.expr -O2 -Wall -Werror");
    if (ret != 0) { --i; continue;}

    fp = popen("/tmp/.expr", "r");
    assert(fp != NULL);

    int result;
    ret = fscanf(fp, "%d", &result);
    pclose(fp);

    int p = 0;
    for(int i=0;i<buf_end;++i){
      if(buf[i] != 'u') buf[p++] = buf[i];
    }
    memset(&buf[p], 0, sizeof(buf) - p - 1);
    printf("%u %s\n", result, buf);
  }
  return 0;
}
