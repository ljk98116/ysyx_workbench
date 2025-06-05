#pragma once

#include <common.hpp>

namespace npc{
typedef struct {
  word_t gpr[MUXDEF(CONFIG_RVE, 16, 32)];
  vaddr_t pc;
} MUXDEF(CONFIG_RV64, riscv64_CPU_state, riscv32_CPU_state);

typedef struct {
  word_t gpr[MUXDEF(CONFIG_RVE, 16, 32)];
  vaddr_t pc[4];
} MUXDEF(CONFIG_RV64, npc_riscv64_CPU_state, npc_riscv32_CPU_state);

#define isa_mmu_check(vaddr, len, type) (MMU_DIRECT)
}