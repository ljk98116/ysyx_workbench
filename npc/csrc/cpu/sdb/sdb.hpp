#pragma once

#include <stdio.h>
#include <stdint.h>

namespace npc
{

typedef uint32_t word_t;
void sdb_mainloop();
void init_monitor(int argc, char *argv[]);
void sdb_set_batch_mode();
word_t expr(char *e, bool *success);

}