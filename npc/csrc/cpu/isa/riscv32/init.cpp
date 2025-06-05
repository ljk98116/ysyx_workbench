#include <isa.hpp>
#include <memory/paddr.hpp>
namespace npc{

static void restart() {
  /* The zero register is always 0. */
  memset(cpu.gpr, 0, sizeof(cpu.gpr));
}

void init_isa(){
  /* Load built-in image. */
  // memcpy(guest_to_host(RESET_VECTOR), img, sizeof(img));

  /* Initialize this virtual computer system. */
  restart();
}

}