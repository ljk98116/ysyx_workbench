#include <sdb/sdb.hpp>
#include <VCPUCore.h>

using namespace npc;

int main(int argc, char *argv[]){
    Verilated::commandArgs(argc, argv);
#if CONFIG_USE_VCD
    Verilated::traceEverOn(true); // 启用跟踪
#endif
    init_monitor(argc, argv);
    sdb_mainloop();
    return 0;
}