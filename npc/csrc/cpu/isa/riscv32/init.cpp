#include <isa.hpp>
#include <memory/paddr.hpp>
namespace npc{

static void restart() {
  /* Set the initial program counter. */
  cpu.pc = RESET_VECTOR;

  /* The zero register is always 0. */
  cpu.gpr[0] = 0;
}

void init_isa(){
  /* Load built-in image. */
  // memcpy(guest_to_host(RESET_VECTOR), img, sizeof(img));

  /* Initialize this virtual computer system. */
  restart();
}

}