#include <utils.hpp>
#include <common.hpp>

namespace npc{

extern uint64_t g_nr_guest_inst;

FILE *npc_log_fp = NULL;

void init_log(const char *log_file) {
  npc_log_fp = stdout;
  if (log_file != NULL) {
    FILE *fp = fopen(log_file, "w");
    NPCAssert(fp, "Can not open '%s'", log_file);
    npc_log_fp = fp;
  }
  NPCLog("Log is written to %s", log_file ? log_file : "stdout");
}

bool npc_log_enable() {
  return MUXDEF(CONFIG_TRACE, (g_nr_guest_inst >= CONFIG_TRACE_START) &&
         (g_nr_guest_inst <= CONFIG_TRACE_END), false);
}

}