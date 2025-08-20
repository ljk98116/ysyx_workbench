#pragma once

#include <common.hpp>

namespace npc {
    void init_map();
    void init_serial();
    void init_timer();
    void init_vga();
    void init_i8042();
    // void init_audio();
    // void init_disk();
    // void init_sdcard();
    // void init_alarm();

    void send_key(uint8_t, bool);
    void vga_update_screen();
}