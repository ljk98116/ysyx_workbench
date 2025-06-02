#include <sdb/sdb.hpp>

using namespace npc;

int main(int argc, char *argv[]){
    init_monitor(argc, argv);
    sdb_mainloop();
    return 0;
}