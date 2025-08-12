#include <am.h>
#include <nemu.h>

#define KEYDOWN_MASK 0x8000

void __am_input_keybrd(AM_INPUT_KEYBRD_T *kbd) {
  uint32_t keycode = inl(0xa0000060);
  kbd->keydown = keycode & KEYDOWN_MASK;
  kbd->keycode = keycode & 0xff;
}
