/***************************************************************************************
* Copyright (c) 2014-2024 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <isa.h>

word_t isa_raise_intr(word_t NO, vaddr_t epc) {
  /* TODO: Trigger an interrupt/exception with ``NO''.
   * Then return the address of the interrupt/exception vector.
   */
#ifdef CONFIG_ETRACE
  Log("[ETRACE] ecall at epc: 0x%x, mcause: %d", epc, NO);
#endif
  cpu.mode = ((cpu.csr[CSR_MSTATUS] & (1 << 11)) >> 11) | ((cpu.csr[CSR_MSTATUS] & (1 << 12)) >> 11);
  cpu.csr[CSR_MSTATUS] &= ~(1 << 7); //清除MPIE
  cpu.csr[CSR_MSTATUS] |= (cpu.csr[CSR_MSTATUS] & (1 << 3)) << 4; //MIE保存到MPIE
  cpu.csr[CSR_MSTATUS] &= ~(1 << 3); //MIE清零
  cpu.csr[CSR_MSTATUS] |= ((1 << 11) | (1 << 12)); //特权级改为M

  cpu.csr[CSR_MCAUSE] = NO;
  cpu.csr[CSR_MEPC] = epc;

  return cpu.csr[CSR_MTVEC];
}

word_t isa_query_intr() {
  return INTR_EMPTY;
}
