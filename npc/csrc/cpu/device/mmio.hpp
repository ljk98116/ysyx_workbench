#pragma once

#include <common.hpp>
namespace npc{
    word_t mmio_read(paddr_t addr, int len);
    void mmio_write(paddr_t addr, int len, word_t data);
}