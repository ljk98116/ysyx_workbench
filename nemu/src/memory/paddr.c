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

#include <memory/host.h>
#include <memory/paddr.h>
#include <device/mmio.h>
#include <isa.h>

#if   defined(CONFIG_PMEM_MALLOC)
static uint8_t *pmem = NULL;
#else // CONFIG_PMEM_GARRAY
static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN = {};
#endif

iringbuf_t *mtrace_buf;

uint8_t* guest_to_host(paddr_t paddr) { return pmem + paddr - CONFIG_MBASE; }
paddr_t host_to_guest(uint8_t *haddr) { return haddr - pmem + CONFIG_MBASE; }

static word_t pmem_read(paddr_t addr, int len) {
  word_t ret = host_read(guest_to_host(addr), len);
  return ret;
}

static void pmem_write(paddr_t addr, int len, word_t data) {
  host_write(guest_to_host(addr), len, data);
}

static void out_of_bound(paddr_t addr) {
  panic("address = " FMT_PADDR " is out of bound of pmem [" FMT_PADDR ", " FMT_PADDR "] at pc = " FMT_WORD,
      addr, PMEM_LEFT, PMEM_RIGHT, cpu.pc);
}

void init_mem() {
#if   defined(CONFIG_PMEM_MALLOC)
  pmem = malloc(CONFIG_MSIZE);
  assert(pmem);
#endif
  IFDEF(CONFIG_MEM_RANDOM, memset(pmem, rand(), CONFIG_MSIZE));
  Log("physical memory area [" FMT_PADDR ", " FMT_PADDR "]", PMEM_LEFT, PMEM_RIGHT);
#ifdef CONFIG_MTRACE
  mtrace_buf = iringbuf_create(MTRACE_INFO_SIZE * MTRACE_NUM);
#endif
}

word_t paddr_read(paddr_t addr, int len) {
  if (likely(in_pmem(addr))) {
    word_t data = pmem_read(addr, len);
#ifdef CONFIG_MTRACE
    char label = 'R';
    char *type = "MEM";
    char buf[MTRACE_INFO_SIZE];
    memset(buf, '\0', MTRACE_INFO_SIZE);
    sprintf(buf, "addr: 0x%08x\tlen: %d\tlabel: %c\ttype:%s\tdata:0x%08x\n", addr, len, label, type, data);
    iringbuf_write(mtrace_buf, buf, MTRACE_INFO_SIZE);
#endif
    return data;
  }
#ifdef CONFIG_DEVICE
  word_t data = mmio_read(addr, len);
#ifdef CONFIG_MTRACE
  char label = 'R';
  char *type = "IO";
  char buf[MTRACE_INFO_SIZE];
  memset(buf, '\0', MTRACE_INFO_SIZE);
  sprintf(buf, "addr: 0x%08x\tlen: %d\tlabel: %c\ttype:%s\tdata:0x%08x\n", addr, len, label, type, data);
  iringbuf_write(mtrace_buf, buf, MTRACE_INFO_SIZE);
#endif  
  return data;
#endif
  // IFDEF(CONFIG_DEVICE, return mmio_read(addr, len));
  out_of_bound(addr);
  return 0;
}

void paddr_write(paddr_t addr, int len, word_t data) {
  if (likely(in_pmem(addr))) { 
    pmem_write(addr, len, data);
#ifdef CONFIG_MTRACE
    char label = 'W';
    char *type = "MEM";
    char buf[MTRACE_INFO_SIZE];
    memset(buf, '\0', MTRACE_INFO_SIZE);
    sprintf(buf, "addr: 0x%08x\tlen: %d\tlabel: %c\ttype:%s\tdata:0x%08x\n", addr, len, label, type, data);
    iringbuf_write(mtrace_buf, buf, MTRACE_INFO_SIZE);
#endif
    return;
  }
#ifdef CONFIG_DEVICE
  mmio_write(addr, len, data);
#ifdef CONFIG_MTRACE
  char label = 'W';
  char *type = "IO";
  char buf[MTRACE_INFO_SIZE];
  memset(buf, '\0', MTRACE_INFO_SIZE);
  sprintf(buf, "addr: 0x%08x\tlen: %d\tlabel: %c\ttype:%s\tdata:0x%08x\n", addr, len, label, type, data);
  iringbuf_write(mtrace_buf, buf, MTRACE_INFO_SIZE);
#endif
  return;
#endif
  // IFDEF(CONFIG_DEVICE, mmio_write(addr, len, data); return);
  out_of_bound(addr);
}