#include <memory/paddr.hpp>
#include <memory/host.hpp>
#include <isa.hpp>
#include <device/mmio.hpp>
#include <cpu/cpu.hpp>

namespace npc{

#if defined(CONFIG_PMEM_MALLOC)
static uint8_t *pmem = NULL;
#else // CONFIG_PMEM_GARRAY
static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN = {};
#endif

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
#if defined(CONFIG_PMEM_MALLOC)
  pmem = malloc(CONFIG_MSIZE);
  assert(pmem);
#endif
  IFDEF(CONFIG_MEM_RANDOM, memset(pmem, rand(), CONFIG_MSIZE));
  Log("physical memory area [" FMT_PADDR ", " FMT_PADDR "]", PMEM_LEFT, PMEM_RIGHT);
// #ifdef CONFIG_MTRACE
//   mtrace_buf = iringbuf_create(MTRACE_INFO_SIZE * MTRACE_NUM);
// #endif
}

word_t paddr_read(paddr_t addr, int len){
  /* 判断是否是MMIO */
  if(
    (addr >= 0xa00003f8 && addr <= 0xa00003ff) ||
    (addr >= 0xa0000048 && addr <= 0xa000004f) ||
    (addr == 0xa0000060)
  ){
    // printf("recv mmio read 0x%x at cycle: %d\n", addr, cycle);
    return mmio_read(addr, len);
  }
  if (likely(in_pmem(addr))) {
    word_t data = pmem_read(addr, len);
    return data;
  }
  return 0;
}

void paddr_write(paddr_t addr, int len, word_t data) {
  if(
    (addr >= 0xa00003f8 && addr <= 0xa00003ff) ||
    (addr >= 0xa0000048 && addr <= 0xa000004c) ||
    (addr == 0xa0000060)
  ){
    // printf("recv mmio write 0x%x at cycle: %d\n", addr, cycle);
    return mmio_write(addr, len, data);
  }  
  if (likely(in_pmem(addr))) { 
    pmem_write(addr, len, data);
    return;
  }
}

}