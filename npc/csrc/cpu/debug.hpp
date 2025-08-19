#pragma once

#include <common.hpp>
#include <stdio.h>
#include <utils.hpp>

namespace npc {

#define NPCLog(format, ...) \
    _NPCLog(NPC_ANSI_FMT("[%s:%d %s] " format, NPC_ANSI_FG_BLUE) "\n", \
        __FILE__, __LINE__, __func__, ## __VA_ARGS__)

#define NPCAssert(cond, format, ...) \
  do { \
    if (!(cond)) { \
      MUXDEF(CONFIG_TARGET_AM, printf(NPC_ANSI_FMT(format, NPC_ANSI_FG_RED) "\n", ## __VA_ARGS__), \
        (fflush(stdout), fprintf(stderr, NPC_ANSI_FMT(format, NPC_ANSI_FG_RED) "\n", ##  __VA_ARGS__))); \
      IFNDEF(CONFIG_TARGET_AM, fflush(npc_log_fp)); \
      extern void assert_fail_msg(); \
      assert_fail_msg(); \
      assert(cond); \
    } \
  } while (0)

#define npc_panic(format, ...) NPCAssert(0, format, ## __VA_ARGS__)

#define TODO() npc_panic("please implement me")

}