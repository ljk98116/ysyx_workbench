#include <common.hpp>
#include <utils.hpp>
#include <memory/paddr.hpp>
#include <device/device.hpp>
#include <device/alarm.hpp>
#include <device/map.hpp>
#include <stdio.h>
#ifndef CONFIG_TARGET_AM
#include <SDL2/SDL.h>
#endif

namespace npc {
// iringbuf_t *dtrace_buf;
static uint32_t *rtc_port_base = NULL;

static void rtc_io_handler(uint32_t offset, int len, bool is_write) {
  assert(offset == 0 || offset == 4);
  //必须在offset为0时更新，否则gettime调用不到，低32位是上一次的时间
  if (!is_write && offset == 0) {
    uint64_t us = get_time();
    rtc_port_base[0] = (uint32_t)us;
    rtc_port_base[1] = us >> 32;
    // printf("gettime: %d %d\n", rtc_port_base[0], rtc_port_base[1]);
  }
}

#ifndef CONFIG_TARGET_AM
static void timer_intr() {
  if (nemu_state.state == NEMU_RUNNING) {
    extern void dev_raise_intr();
    dev_raise_intr();
  }
}
#endif

void init_timer() {
  rtc_port_base = (uint32_t *)new_space(8);
#ifdef CONFIG_HAS_PORT_IO
  add_pio_map ("rtc", CONFIG_RTC_PORT, rtc_port_base, 8, rtc_io_handler);
#else
  add_mmio_map("rtc", CONFIG_RTC_MMIO, rtc_port_base, 8, rtc_io_handler);
#endif
  // IFNDEF(CONFIG_TARGET_AM, add_alarm_handle(timer_intr));
}

void device_update() {
  static uint64_t last = 0;
  uint64_t now = get_time();
  if (now - last < 1000000 / TIMER_HZ) {
    return;
  }
  last = now;

  // IFDEF(CONFIG_HAS_VGA, vga_update_screen());

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
  // IFDEF(CONFIG_TARGET_AM, ioe_init());
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