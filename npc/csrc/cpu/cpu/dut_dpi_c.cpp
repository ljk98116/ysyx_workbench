#include <memory/paddr.hpp>
#include <isa.hpp>
#include <cpu/cpu.hpp>

using namespace npc;
#ifdef __cplusplus
extern "C"{
#endif

int npc_pmem_read(int raddr){
    return paddr_read(raddr, 4);
}

void npc_pmem_write(int waddr, int wdata, char wmask){
    switch(wmask){
        case 0b0001: paddr_write(waddr, 1, wdata & 0xFF); break;
        case 0b0010: paddr_write(waddr, 1, (wdata >> 8) & 0xFF); break;
        case 0b0100: paddr_write(waddr, 1, (wdata >> 16) & 0xFF); break;
        case 0b1000: paddr_write(waddr, 1, (wdata >> 24) & 0xFF); break;

        case 0b0011: paddr_write(waddr, 2, wdata & 0xFFFF); break;
        case 0b1100: paddr_write(waddr, 2, (wdata >> 16) & 0xFFFF); break;

        case 0b1111: paddr_write(waddr, 4, wdata); break;
    }
}

/* 更新npc的提交状态,设置寄存器值 */
void npc_commit(
    char valid,
    char rat_write_en,
    char rat_write_addr_0,
    char rat_write_addr_1,
    char rat_write_addr_2,
    char rat_write_addr_3,
    char rat_write_data_0,
    char rat_write_data_1,
    char rat_write_data_2,
    char rat_write_data_3,
    int reg_write_data_0,
    int reg_write_data_1,
    int reg_write_data_2,
    int reg_write_data_3,
    int pc0,
    int pc1,
    int pc2,
    int pc3
){
    commit_num = 0;
    if(valid == 0) return;
    /* 4个位置的寄存器写入 */
    if(rat_write_en & 0x1){
        retire_RAT[rat_write_addr_0] = rat_write_data_0;
        cpu.gpr[rat_write_addr_0] = reg_write_data_0;
        ++commit_num;
        cpu.pc[0] = pc0;
    }
    if(rat_write_en & 0x2){
        retire_RAT[rat_write_addr_1] = rat_write_data_1;
        cpu.gpr[rat_write_addr_1] = reg_write_data_1;
        ++commit_num;
        cpu.pc[1] = pc1;
    }
    if(rat_write_en & 0x4){
        retire_RAT[rat_write_addr_2] = rat_write_data_2;
        cpu.gpr[rat_write_addr_2] = reg_write_data_2;
        ++commit_num;
        cpu.pc[2] = pc2;
    }
    if(rat_write_en & 0x8){
        retire_RAT[rat_write_addr_3] = rat_write_data_3;
        cpu.gpr[rat_write_addr_3] = reg_write_data_3;
        ++commit_num;
        cpu.pc[3] = pc3;
    }    
}

/* 更新rename段重命名表*/
void npc_update_renameRAT(){

}

/* 更新retire段重命名表*/
void npc_update_retireRAT(){

}

#ifdef __cplusplus
}
#endif