#include <dlfcn.h>

#include <isa.hpp>
#include <cpu/cpu.hpp>
#include <memory/paddr.hpp>
#include <utils.hpp>
#include <cpu/difftest.hpp>

namespace npc{
#if CONFIG_DIFFTEST
void (*ref_difftest_memcpy)(paddr_t addr, void *buf, size_t n, bool direction) = NULL;
void (*ref_difftest_regcpy)(void *dut, bool direction) = NULL;
void (*ref_difftest_exec)(uint64_t n) = NULL;
void (*ref_difftest_raise_intr)(uint64_t NO) = NULL;
void (*ref_difftest_init)(int port) = NULL;

static bool is_skip_ref = false;
static int skip_dut_nr_inst = 0;

// this is used to let ref skip instructions which
// can not produce consistent behavior with NEMU
void difftest_skip_ref() {
  is_skip_ref = true;
  // If such an instruction is one of the instruction packing in QEMU
  // (see below), we end the process of catching up with QEMU's pc to
  // keep the consistent behavior in our best.
  // Note that this is still not perfect: if the packed instructions
  // already write some memory, and the incoming instruction in NEMU
  // will load that memory, we will encounter false negative. But such
  // situation is infrequent.
  skip_dut_nr_inst = 0;
}

// this is used to deal with instruction packing in QEMU.
// Sometimes letting QEMU step once will execute multiple instructions.
// We should skip checking until NEMU's pc catches up with QEMU's pc.
// The semantic is
//   Let REF run `nr_ref` instructions first.
//   We expect that DUT will catch up with REF within `nr_dut` instructions.
void difftest_skip_dut(int nr_ref, int nr_dut) {
  skip_dut_nr_inst += nr_dut;

  while (nr_ref -- > 0) {
    ref_difftest_exec(1);
  }
}

void init_difftest(char *ref_so_file, long img_size, int port) {
  assert(ref_so_file != NULL);

  void *handle;
  handle = dlopen(ref_so_file, RTLD_LAZY);
  assert(handle);

  ref_difftest_memcpy = 
        (void (*)(paddr_t addr, void *buf, size_t n, bool direction))dlsym(handle, "difftest_memcpy");
  assert(ref_difftest_memcpy);

  ref_difftest_regcpy = 
        (void (*)(void *dut, bool direction))dlsym(handle, "difftest_regcpy");
  assert(ref_difftest_regcpy);

  ref_difftest_exec = 
        (void (*)(uint64_t n))dlsym(handle, "difftest_exec");
  assert(ref_difftest_exec);

  ref_difftest_raise_intr = 
        (void (*)(uint64_t NO))dlsym(handle, "difftest_raise_intr");
  assert(ref_difftest_raise_intr);

  void (*ref_difftest_init)(int) = (void (*)(int port))dlsym(handle, "difftest_init");
  assert(ref_difftest_init);

  NPCLog("Differential testing: %s", NPC_ANSI_FMT("ON", NPC_ANSI_BG_GREEN));
  NPCLog("The result of every instruction will be compared with %s. "
      "This will help you a lot for debugging, but also significantly reduce the performance. "
      "If it is not necessary, you can turn it off in menuconfig.", ref_so_file);

  ref_difftest_init(port);
  ref_difftest_memcpy(RESET_VECTOR, guest_to_host(RESET_VECTOR), img_size, DIFFTEST_TO_REF);
  CPU_state ref;
  ref.pc = RESET_VECTOR;
  memset(ref.gpr, 0, sizeof(ref.gpr));
  ref_difftest_regcpy(&ref, DIFFTEST_TO_REF);
}

static void checkregs(CPU_state *ref) {
  if (!isa_difftest_checkregs(ref)) {
    nemu_state.state = NEMU_ABORT;
    if(commit_num > 0) nemu_state.halt_pc = cpu.pc[commit_num - 1];
    NPCLog("npc regs:\n");
    isa_reg_display(&cpu, false);
    NPCLog("nemu regs:\n");
    isa_reg_display(ref, true);
  }
  else{
    // Log("%d th cycle, check done", cycle);
    // isa_reg_display(&cpu, false);
  }
}

/* 乱序npc指令提交时触发difftest_step */
void difftest_step() {
  static int last_ref_pc;
  CPU_state ref_r;
  if(commit_num > 0){
    ref_difftest_exec(commit_num);
    ref_difftest_regcpy(&ref_r, DIFFTEST_TO_DUT);
    if(ref_r.pc == last_ref_pc) {
      ref_stop = true;
      nemu_state.state = NEMU_END;
      nemu_state.halt_pc = ref_r.pc;
    }
    // NPCLog("ref pc: 0x%x", ref_r.pc);
    checkregs(&ref_r);
    last_ref_pc = ref_r.pc;
  }
}

#endif
}


