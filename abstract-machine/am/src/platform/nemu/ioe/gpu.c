#include <am.h>
#include <nemu.h>

#define SYNC_ADDR (VGACTL_ADDR + 4)

static int w = 0, h = 0;
void __am_gpu_init() {
  int i;
  /* 读取VGA Ctrl MMIO 0xa0000100， 获取宽高 */
  uint32_t vgactrl = inl(0xa0000100);
  w = vgactrl >> 16;
  h = vgactrl & 0xffff;
  uint32_t *fb = (uint32_t *)(uintptr_t)FB_ADDR;
  for (i = 0; i < w * h; i ++) fb[i] = i;
  outl(SYNC_ADDR, 1);
}

void __am_gpu_config(AM_GPU_CONFIG_T *cfg) {
  // *cfg = (AM_GPU_CONFIG_T) {
  //   .present = true, .has_accel = false,
  //   .width = 0, .height = 0,
  //   .vmemsz = 0
  // };

  /* 读取VGA Ctrl MMIO 0xa0000100， 获取宽高 */
  uint32_t vgactrl = inl(0xa0000100);
  int width = vgactrl >> 16;
  int height = vgactrl & 0xffff;
  /* 读取屏幕大小 */
  uint32_t screen_size = inl(0xa1000000);  
  *cfg = (AM_GPU_CONFIG_T) {
    .present = true, .has_accel = false,
    .width = width, .height = height,
    .vmemsz = screen_size
  };
}

void __am_gpu_fbdraw(AM_GPU_FBDRAW_T *ctl) {
  /* 将像素值写入FB_ADDR加上偏移后的位置 */
  if(!ctl->sync && (ctl->w == 0 || ctl->h == 0)) return;
  uint32_t *pixels = (uint32_t*)(uintptr_t)ctl->pixels;
  uint32_t *fb_addr = (uint32_t*)(uintptr_t)FB_ADDR;
  for(int i=ctl->y;i<ctl->y + ctl->h;++i){
    for(int j=ctl->x;j<ctl->x + ctl->w;++j){
      fb_addr[i * w + j] = pixels[(i - ctl->y) * ctl->w + (j - ctl->x)];
    }
  }
  if (ctl->sync) {
    outl(SYNC_ADDR, 1);
  }
}

void __am_gpu_status(AM_GPU_STATUS_T *status) {
  status->ready = true;
}
