#include <am.h>
#include "nemu.h"

void __am_timer_init() {
}

void __am_timer_uptime(AM_TIMER_UPTIME_T *uptime) {
  uint32_t base0 = inl(0xa0000048);
  uint32_t base1 = inl(0xa000004c);
  uptime->us = ((uint64_t)base1 << 32) | (uint64_t)base0;
}

void __am_timer_rtc(AM_TIMER_RTC_T *rtc) {
  rtc->second = 0;
  rtc->minute = 0;
  rtc->hour   = 0;
  rtc->day    = 0;
  rtc->month  = 0;
  rtc->year   = 1900;
}
