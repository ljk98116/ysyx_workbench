#pragma once

#include <elf.h>
#include <common.hpp>
#include <isa/riscv32/isa-def.hpp>

namespace npc{

// The macro `__GUEST_ISA__` is defined in $(CFLAGS).
// It will be expanded as "x86" or "mips32" ...
// typedef concat(__GUEST_ISA__, _CPU_state) CPU_state;
// typedef concat(__GUEST_ISA__, _ISADecodeInfo) ISADecodeInfo;
typedef riscv32_CPU_state CPU_state;
typedef npc_riscv32_CPU_state npc_CPU_state;
// monitor
extern unsigned char isa_logo[];
void init_isa();

// reg
extern npc_CPU_state cpu;
void isa_reg_display(void *cpu_ptr, bool is_ref);
word_t isa_reg_str2val(const char *name, bool *success);

// exec
struct Decode;
int isa_exec_once(struct Decode *s);

// memory
enum { MMU_DIRECT, MMU_TRANSLATE, MMU_FAIL };
enum { MEM_TYPE_IFETCH, MEM_TYPE_READ, MEM_TYPE_WRITE };
enum { MEM_RET_OK, MEM_RET_FAIL, MEM_RET_CROSS_PAGE };
#ifndef isa_mmu_check
int isa_mmu_check(vaddr_t vaddr, int len, int type);
#endif
paddr_t isa_mmu_translate(vaddr_t vaddr, int len, int type);

// interrupt/exception
vaddr_t isa_raise_intr(word_t NO, vaddr_t epc);
#define INTR_EMPTY ((word_t)-1)
word_t isa_query_intr();

// difftest
bool isa_difftest_checkregs(CPU_state *ref_r);
void isa_difftest_attach();

}