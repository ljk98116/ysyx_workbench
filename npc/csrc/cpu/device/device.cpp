#include <common.hpp>
#include <utils.hpp>
#include <memory/paddr.hpp>
#include <device/alarm.hpp>
#include <device/device.hpp>

#ifndef CONFIG_TARGET_AM
#include <SDL2/SDL.h>
#endif

namespace npc {
// iringbuf_t *dtrace_buf;

void device_update() {
  static uint64_t last = 0;
  uint64_t now = get_time();
  if (now - last < 1000000 / TIMER_HZ) {
    return;
  }
  last = now;

  IFDEF(CONFIG_HAS_VGA, vga_update_screen());

#ifndef CONFIG_TARGET_AM
  SDL_Event event;
  while (SDL_PollEvent(&event)) {
    switch (event.type) {
      case SDL_QUIT:
        nemu_state.state = NEMU_QUIT;
        break;
#ifdef CONFIG_HAS_KEYBOARD
      // If a key was pressed
      case SDL_KEYDOWN:
      case SDL_KEYUP: {
        uint8_t k = event.key.keysym.scancode;
        bool is_keydown = (event.key.type == SDL_KEYDOWN);
        send_key(k, is_keydown);
        break;
      }
#endif
      default: break;
    }
  }
#endif
}

void sdl_clear_event_queue() {
#ifndef CONFIG_TARGET_AM
  SDL_Event event;
  while (SDL_PollEvent(&event));
#endif
}

void init_device() {
  IFDEF(CONFIG_TARGET_AM, ioe_init());
  init_map();

  init_serial();
  init_timer();
// IFDEF(CONFIG_HAS_VGA, init_vga());
// IFDEF(CONFIG_HAS_KEYBOARD, init_i8042());
// IFDEF(CONFIG_HAS_AUDIO, init_audio());
// IFDEF(CONFIG_HAS_DISK, init_disk());
// IFDEF(CONFIG_HAS_SDCARD, init_sdcard());

// IFNDEF(CONFIG_TARGET_AM, init_alarm());
// #ifdef CONFIG_DTRACE
//   dtrace_buf = iringbuf_create(DTRACE_INFO_SIZE * DTRACE_NUM);
// #endif
}
}