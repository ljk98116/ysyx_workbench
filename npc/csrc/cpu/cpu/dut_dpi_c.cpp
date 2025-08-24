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

#define ROBRW_API(elem, width, type) \
    void npc_rob_write_##elem(char bankid, int index, int data) { \
        rob_table[bankid][index].elem = (type)(data & ((1LL << width) - 1)); \
    } \
    \
    int npc_rob_read_##elem(char bankid, int index) { \
        return rob_table[bankid][index].elem; \
    }

ROBRW_API(pc, 32, uint32_t)
ROBRW_API(valid, 1, uint8_t)
ROBRW_API(Imm, 32, uint32_t)
ROBRW_API(Opcode, 7, uint8_t)
ROBRW_API(rs1, 5, uint8_t)
ROBRW_API(rs2, 5, uint8_t)
ROBRW_API(rd, 5, uint8_t)
ROBRW_API(funct3, 5, uint8_t)
ROBRW_API(funct7, 7, uint8_t)
ROBRW_API(Type, 3, uint8_t)
ROBRW_API(HasRs1, 1, uint8_t)
ROBRW_API(HasRs2, 1, uint8_t)
ROBRW_API(HasRd, 1, uint8_t)
ROBRW_API(id, 7, uint8_t)
ROBRW_API(ps1, 8, uint8_t)
ROBRW_API(ps2, 8, uint8_t)
ROBRW_API(pd, 7, uint8_t)
ROBRW_API(oldpd, 8, uint8_t)
ROBRW_API(rdy1, 1, uint8_t)
ROBRW_API(rdy2, 1, uint8_t)
ROBRW_API(rdy, 1, uint8_t)
ROBRW_API(isBranch, 1, uint8_t)
ROBRW_API(isStore, 1, uint8_t)
ROBRW_API(isLoad, 1, uint8_t)
ROBRW_API(hasException, 1, uint8_t)
ROBRW_API(ExceptionType, 8, uint8_t)
ROBRW_API(targetBrAddr, 32, uint32_t)
ROBRW_API(reg_wb_data, 32, uint32_t)
ROBRW_API(storeIdx, 8, uint8_t)
ROBRW_API(gbranch_res, 1, uint8_t)
ROBRW_API(lbranch_res, 1, uint8_t)
ROBRW_API(branch_res, 1, uint8_t)
ROBRW_API(global_pht_idx, 13, uint16_t)
ROBRW_API(local_pht_idx, 13, uint16_t)
ROBRW_API(bht_idx, 8, uint8_t)
ROBRW_API(branch_pred_addr, 32, uint32_t);

void npc_rob_id_loc_mem_write(char index, char data){
    rob_id_loc_mem[index] = data;
}

int npc_rob_id_loc_mem_read(char index){
    return rob_id_loc_mem[index];
}

#ifdef __cplusplus
}
#endif