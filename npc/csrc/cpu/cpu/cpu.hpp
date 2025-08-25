#pragma once

#include <common.hpp>

namespace npc{
void cpu_exec(uint64_t n, void* tfp = nullptr);
void cpu_reset(void* tfp);

void set_nemu_state(int state, vaddr_t pc, int halt_ret);
void invalid_inst(vaddr_t thispc);

#define NEMUTRAP(thispc, code) set_nemu_state(NEMU_END, thispc, code)
#define INV(thispc) invalid_inst(thispc)

/* npc data structure */
extern uint8_t retire_RAT[32];
extern uint8_t rename_RAT[32];
extern uint8_t gPHT[1 << 13];
extern uint8_t lPHT[1 << 13];
extern uint8_t cPHT[1 << 13];

typedef struct {
    bool V : 1;
    char bia;
    uint32_t bta;
} btb_item_t;

typedef struct {
    uint32_t pc;
    uint8_t valid : 1;
    uint32_t Imm;
    uint8_t Opcode : 7;
    uint8_t rs1 : 5;
    uint8_t rs2 : 5;
    uint8_t rd : 5;
    uint8_t funct3 : 5;
    uint8_t funct7 : 7;
    uint8_t Type : 3;
    uint8_t HasRs1 : 1;
    uint8_t HasRs2 : 1;
    uint8_t HasRd : 1;
    uint8_t id : 7;
    uint8_t ps1;
    uint8_t ps2;
    uint8_t pd : 7;
    uint8_t oldpd;
    uint8_t rdy1 : 1;
    uint8_t rdy2 : 1;
    uint8_t rdy : 1;
    uint8_t isBranch : 1;
    uint8_t isStore : 1;
    uint8_t isLoad : 1;
    uint8_t hasException : 1;
    uint8_t ExceptionType;
    uint32_t targetBrAddr;
    uint32_t reg_wb_data;
    uint8_t storeIdx;
    uint8_t gbranch_res : 1;
    uint8_t lbranch_res : 1;
    uint8_t branch_res : 1;
    uint16_t global_pht_idx: 13;
    uint16_t local_pht_idx: 13;
    uint8_t bht_idx;
    uint16_t btb_idx : 13;
    uint32_t branch_pred_addr;
} rob_item_t;

extern btb_item_t btb_table[1 << 13];
extern rob_item_t rob_table[4][1 << 7];
extern uint32_t rob_id_loc_mem[1 << 7];

extern uint8_t commit_num;
extern uint32_t cycle;
extern bool ref_stop;
extern uint32_t total_branch_cnt;
extern uint32_t branch_err_cnt;

}