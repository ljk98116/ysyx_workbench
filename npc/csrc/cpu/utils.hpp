#pragma once

#include <cstdint>
#include <common.hpp>

namespace npc{

// ----------- state -----------
extern FILE* npc_log_fp;
extern bool npc_log_enable();

enum { NEMU_RUNNING, NEMU_STOP, NEMU_END, NEMU_ABORT, NEMU_QUIT };

typedef struct {
  int state;
  vaddr_t halt_pc;
  uint32_t halt_ret;
} NEMUState;

extern NEMUState nemu_state;

// ----------- timer -----------

uint64_t get_time();

// ----------- log -----------

#define NPC_ANSI_FG_BLACK   "\33[1;30m"
#define NPC_ANSI_FG_RED     "\33[1;31m"
#define NPC_ANSI_FG_GREEN   "\33[1;32m"
#define NPC_ANSI_FG_YELLOW  "\33[1;33m"
#define NPC_ANSI_FG_BLUE    "\33[1;34m"
#define NPC_ANSI_FG_MAGENTA "\33[1;35m"
#define NPC_ANSI_FG_CYAN    "\33[1;36m"
#define NPC_ANSI_FG_WHITE   "\33[1;37m"
#define NPC_ANSI_BG_BLACK   "\33[1;40m"
#define NPC_ANSI_BG_RED     "\33[1;41m"
#define NPC_ANSI_BG_GREEN   "\33[1;42m"
#define NPC_ANSI_BG_YELLOW  "\33[1;43m"
#define NPC_ANSI_BG_BLUE    "\33[1;44m"
#define NPC_ANSI_BG_MAGENTA "\33[1;45m"
#define NPC_ANSI_BG_CYAN    "\33[1;46m"
#define NPC_ANSI_BG_WHITE   "\33[1;47m"
#define NPC_ANSI_NONE       "\33[0m"

#define NPC_ANSI_FMT(str, fmt) fmt str NPC_ANSI_NONE

#define npc_log_write(...) IFDEF(CONFIG_TARGET_NATIVE_ELF, \
  do { \
    if (npc_log_able() && npc_log_fp != NULL) { \
      fprintf(npc_log_fp, __VA_ARGS__); \
      fflush(npc_log_fp); \
    } \
  } while (0) \
)

#define _NPCLog(...) \
  do { \
    printf(__VA_ARGS__); \
    npc_log_write(__VA_ARGS__); \
  } while (0)

}
