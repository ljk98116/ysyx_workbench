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

elf_func_t func_table[50000];
Elf32_Sym *elf_symtab;
char * elf_strtab;

void init_rand();
void init_log(const char *log_file);
void init_mem();
void init_difftest(char *ref_so_file, long img_size, int port);
void init_device();
void init_sdb();
void init_disasm();

static void welcome() {
  Log("Trace: %s", MUXDEF(CONFIG_TRACE, ANSI_FMT("ON", ANSI_FG_GREEN), ANSI_FMT("OFF", ANSI_FG_RED)));
  IFDEF(CONFIG_TRACE, Log("If trace is enabled, a log file will be generated "
        "to record the trace. This may lead to a large log file. "
        "If it is not necessary, you can disable it in menuconfig"));
  Log("Build time: %s, %s", __TIME__, __DATE__);
  printf("Welcome to %s-NEMU!\n", ANSI_FMT(str(__GUEST_ISA__), ANSI_FG_YELLOW ANSI_BG_RED));
  printf("For help, type \"help\"\n");
  //Log("Exercise: Please remove me in the source code and compile NEMU again.");
  //assert(0);
}

#ifndef CONFIG_TARGET_AM
#include <getopt.h>

void sdb_set_batch_mode();
word_t expr(char *e, bool *success);
//static void expr_test();
static char *log_file = NULL;
static char *diff_so_file = NULL;
static char *img_file = NULL;
static char *elf_file = NULL;
static int difftest_port = 1234;

static long load_img() {
  if (img_file == NULL) {
    Log("No image is given. Use the default build-in image.");
    return 4096; // built-in image size
  }

  FILE *fp = fopen(img_file, "rb");
  Assert(fp, "Can not open '%s'", img_file);

  fseek(fp, 0, SEEK_END);
  long size = ftell(fp);

  Log("The image is %s, size = %ld", img_file, size);

  fseek(fp, 0, SEEK_SET);
  int ret = fread(guest_to_host(RESET_VECTOR), size, 1, fp);
  assert(ret == 1);

  fclose(fp);
  return size;
}

static int parse_args(int argc, char *argv[]) {
  const struct option table[] = {
    {"batch"    , no_argument      , NULL, 'b'},
    {"log"      , required_argument, NULL, 'l'},
    {"diff"     , required_argument, NULL, 'd'},
    {"port"     , required_argument, NULL, 'p'},
    {"help"     , no_argument      , NULL, 'h'},
    {"elf"      , required_argument, NULL, 'e'},
    {0          , 0                , NULL,  0 },
  };
  int o;
  while ( (o = getopt_long(argc, argv, "-bhe:l:d:p:", table, NULL)) != -1) {
    switch (o) {
      case 'b': sdb_set_batch_mode(); break;
      case 'p': sscanf(optarg, "%d", &difftest_port); break;
      case 'l': log_file = optarg; break;
      case 'e': elf_file = optarg; break;
      case 'd': diff_so_file = optarg; break;
      case 1: img_file = optarg; return 0;
      default:
        printf("Usage: %s [OPTION...] IMAGE [args]\n\n", argv[0]);
        printf("\t-b,--batch              run with batch mode\n");
        printf("\t-l,--log=FILE           output log to FILE\n");
        printf("\t-d,--diff=REF_SO        run DiffTest with reference REF_SO\n");
        printf("\t-p,--port=PORT          run DiffTest with port PORT\n");
        printf("\t-e,--elf=FILE           load elf FILE\n");
        printf("\n");
        exit(0);
    }
  }
  return 0;
}

#if 0
static void expr_test(){
  /* test exprs */
  FILE* fp = fopen("/home/ljk/Arch/ics2024_old/nemu/tools/gen-expr/input.txt", "r");
  char res_str[34] = {0};
  char input_str[65535 + 34] = {0};
  char expr_str[65535] = {0};
  bool success = true;
  int cnt = 0;
  while(fp && fgets(input_str, sizeof(input_str), fp)){
    sscanf(input_str, "%s %s", res_str, expr_str);
    printf("%s %s\n", res_str, expr_str);
    word_t res = expr(expr_str, &success);
    if(res != (uint32_t)atol(res_str) || !success){
      panic("%d th expr cal error, expected: 0x%x, actual: 0x%x\n", cnt, (uint32_t)atol(res_str), res);
      success = false;
    }
    ++cnt;
  }
  if(success) printf("EXPR TESTS ALL PASSED !!!\n");
  fclose(fp);
}
#endif

static long init_ftrace(){
  /* 检查elf文件输入 */
  if(elf_file == NULL){
    Log("No elf_file is given, ignore ftrace");
    return 0;
  }
  /* 打开elf文件 */
  FILE *fp = fopen(elf_file, "r");
  Assert(fp, "Can not open '%s'", elf_file);
  /* 获取elf文件大小 */
  fseek(fp, 0, SEEK_END);
  long size = ftell(fp);
  Log("The elf file is %s, size = %ld", elf_file, size);
  /* 返回elf文件头部 */
  fseek(fp, 0, SEEK_SET);
  /* 获取elf文件头结构 */
#ifdef CONFIG_ISA_riscv
#if !defined(CONFIG_RV64)
  /* 解析elf文件内容 */
  Elf32_Ehdr ehdr;
  size_t sz = fread(&ehdr, sizeof(Elf32_Ehdr), 1, fp);
  Assert(sz == 1, "Read Elf file failed");
  fseek(fp, ehdr.e_shoff, SEEK_SET);
  /* 定位节头表的位置,读入节头表到内存 */
  Elf32_Shdr *shdr = (Elf32_Shdr*)malloc(sizeof(Elf32_Shdr) * ehdr.e_shnum);
  int n = fread(shdr, sizeof(Elf32_Shdr) * ehdr.e_shnum, 1, fp);
  assert(n == 1);
  /* 从节头表找到字符串表和符号表的位置 */
  size_t symtab_offset = 0;
  size_t symtab_sz = 0;
  size_t symtab_entsz = 0;

  size_t strtab_offset = 0;
  size_t strtab_sz = 0;
  size_t strtab_num = 0;
  //找到符号表、字符串表属性
  for(int i=0;i<ehdr.e_shnum;++i){
    Elf32_Shdr *cur = &shdr[i];
    if(cur->sh_type == SHT_SYMTAB){
      symtab_offset = cur->sh_offset;
      symtab_sz = cur->sh_size;
      symtab_entsz = cur->sh_entsize;
    }
    if(cur->sh_type == SHT_STRTAB && strtab_num < 1){
      strtab_offset = cur->sh_offset;
      strtab_sz = cur->sh_size;
      ++strtab_num;
    }
  }
  //读取符号表、字符串表到内存
  elf_symtab = (Elf32_Sym*)malloc(symtab_sz);
  fseek(fp, symtab_offset, SEEK_SET);
  int n1 = fread(elf_symtab, symtab_sz, 1, fp);

  elf_strtab = (char*)malloc(strtab_sz);
  fseek(fp, strtab_offset, SEEK_SET);
  int n2 = fread(elf_strtab, strtab_sz, 1, fp);  

  assert(n1 == 1 && n2 == 1);  
  //创建函数表
  size_t func_cnt = 0;
  for(int i=0;i<symtab_sz / symtab_entsz;++i){
    Elf32_Sym* symtab_ent = &elf_symtab[i];
    if(ELF32_ST_TYPE(symtab_ent->st_info) == STT_FUNC){
      func_table[func_cnt].name = elf_strtab + symtab_ent->st_name;
      func_table[func_cnt].size = symtab_ent->st_size;
      //重定位文件
      if(ehdr.e_type == ET_REL){
        //已经定义的符号,st_value表示符号在其所对应的节中的偏移量
        if(symtab_ent->st_shndx != SHN_UNDEF){
          size_t sh_idx = symtab_ent->st_shndx;
          Elf32_Shdr *cur = (Elf32_Shdr*)((uintptr_t)shdr + sh_idx * ehdr.e_shentsize);
          func_table[func_cnt].start = symtab_ent->st_value + cur->sh_addr;
        }
      }
      else if(ehdr.e_type == ET_EXEC || ehdr.e_type == ET_DYN){
        func_table[func_cnt].start = symtab_ent->st_value;
      }
      ++func_cnt;
    }
  }
#else
#endif
#endif
  fclose(fp);
  return size;
}

void init_monitor(int argc, char *argv[]) {
  /* Perform some global initialization. */

  /* Parse arguments. */
  parse_args(argc, argv);

  /* Set random seed. */
  init_rand();

  /* Open the log file. */
  init_log(log_file);

  /* Initialize memory. */
  init_mem();

  /* Initialize devices. */
  IFDEF(CONFIG_DEVICE, init_device());

  /* Perform ISA dependent initialization. */
  init_isa();

  /* Load the image to memory. This will overwrite the built-in image. */
  long img_size = load_img();

  /* Initialize differential testing. */
  init_difftest(diff_so_file, img_size, difftest_port);

  /* Initialize the simple debugger. */
  init_sdb();

  IFDEF(CONFIG_ITRACE, init_disasm());

  /* ftrace */
  init_ftrace();

  /* Display welcome message. */
  welcome();

  /* test exprs, for PA1 */
  // expr_test();

}

#else // CONFIG_TARGET_AM
static long load_img() {
  extern char bin_start, bin_end;
  size_t size = &bin_end - &bin_start;
  Log("img size = %ld", size);
  memcpy(guest_to_host(RESET_VECTOR), &bin_start, size);
  return size;
}

void am_init_monitor() {
  init_rand();
  init_mem();
  init_isa();
  load_img();
  IFDEF(CONFIG_DEVICE, init_device());
  welcome();
}
#endif
