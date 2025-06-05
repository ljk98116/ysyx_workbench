#pragma once

#include <common.hpp>

namespace npc{
void cpu_exec(uint64_t n);
void cpu_reset();

void set_nemu_state(int state, vaddr_t pc, int halt_ret);
void invalid_inst(vaddr_t thispc);

#define NEMUTRAP(thispc, code) set_nemu_state(NEMU_END, thispc, code)
#define INV(thispc) invalid_inst(thispc)

/* npc data structure */
extern uint8_t retire_RAT[32];
extern uint8_t rename_RAT[32];
extern uint8_t commit_num;
extern uint32_t cycle;
extern bool ref_stop;
}