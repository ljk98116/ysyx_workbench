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
    }
    cpu.pc[0] = pc0;
    if(rat_write_en & 0x2){
        retire_RAT[rat_write_addr_1] = rat_write_data_1;
        cpu.gpr[rat_write_addr_1] = reg_write_data_1;
        ++commit_num;
    }
    cpu.pc[1] = pc1;
    if(rat_write_en & 0x4){
        retire_RAT[rat_write_addr_2] = rat_write_data_2;
        cpu.gpr[rat_write_addr_2] = reg_write_data_2;
        ++commit_num;
    }
    cpu.pc[2] = pc2;
    if(rat_write_en & 0x8){
        retire_RAT[rat_write_addr_3] = rat_write_data_3;
        cpu.gpr[rat_write_addr_3] = reg_write_data_3;
        ++commit_num;
    }    
    cpu.pc[3] = pc3;
}


void npc_update_branch_predict(    
    char is_branch,
    char branch_pred_err
){
    if(is_branch == 0) return;
    if(is_branch & 0x1){
        ++total_branch_cnt;
    }
    if(is_branch & 0x2){
        ++total_branch_cnt;
    }
    if(is_branch & 0x4){
        ++total_branch_cnt;
    }
    if(is_branch & 0x8){
        ++total_branch_cnt;
    }
    if(branch_pred_err & 0x1){
        ++branch_err_cnt;
    }
    if(branch_pred_err & 0x2){
        ++branch_err_cnt;
    }    
    if(branch_pred_err & 0x4){
        ++branch_err_cnt;
    }
    if(branch_pred_err & 0x8){
        ++branch_err_cnt;
    }
    // NPCLog("npc branch_cnt: %d, branch_err_cnt: %d", total_branch_cnt, branch_err_cnt);
}

void npc_btb_write(
    short waddr,
    char valid,
    char BIA,
    int BTA
){
    btb_table[waddr].V = valid & 0x1;
    btb_table[waddr].bia = BIA;
    btb_table[waddr].bta = BTA;
}

int npc_btb_read_V(short raddr){
    return btb_table[raddr].V;
}

int npc_btb_read_BIA(short raddr){
    return btb_table[raddr].bia;
}

int npc_btb_read_BTA(short raddr){
    return btb_table[raddr].bta;
}

void npc_gpht_write(short waddr, char wdata){
    gPHT[waddr] = wdata;
}

void npc_lpht_write(short waddr, char wdata){
    lPHT[waddr] = wdata;
}

void npc_cpht_write(short waddr, char wdata){
    cPHT[waddr] = wdata;
}

char npc_gpht_read(short raddr){
    return gPHT[raddr];
}

char npc_lpht_read(short raddr){
    return lPHT[raddr];
}

char npc_cpht_read(short raddr){
    return cPHT[raddr];
}

#ifdef __cplusplus
}
#endif