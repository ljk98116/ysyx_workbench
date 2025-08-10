#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

int printf(const char *fmt, ...) {
  char out[2048];
  memset(out, 0, sizeof(out));
  va_list ap;
  int ret = -1;
  va_start(ap, fmt);
  ret = vsnprintf(out, -1, fmt, ap);
  va_end(ap);
  for(int i=0;i<ret;++i){
    putch(out[i]);
  }
  return ret;
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  return vsnprintf(out, -1, fmt, ap);
}

int sprintf(char *out, const char *fmt, ...) {
  va_list ap;
  int ret = -1;
  va_start(ap, fmt);
  ret = vsprintf(out, fmt, ap);
  va_end(ap);
  return ret;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  va_list ap;
  int ret = -1;
  va_start(ap, fmt);
  ret = vsnprintf(out, n, fmt, ap);
  va_end(ap);
  return ret;  
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  int len = 0;
  char buf[1024];
  while(*fmt && len < n){
    switch(*fmt){
      case '%':{
        fmt++;
        memset(buf, 0 , sizeof(buf));
        /* 统计数字字符串 */
        int para_len = 0;
        while((*fmt) >= '0' && (*fmt) <= '9'){
          para_len = para_len * 10 + (*fmt - '0');
          fmt++;
        }
        switch(*fmt){
          case 'd':{
            int val = va_arg(ap, int);
            if(para_len > 0){
              int val_len = itoa(val, buf);
              int off = 0;
              if(para_len > val_len){
                for(int i=0;i<para_len - val_len;++i){
                  *(out + len + i) = '0';
                }
                off = para_len - val_len;
              }
              memcpy(out + len + off, buf, val_len);
              len += off + val_len;
            }
            else{
              int val_len = itoa(val, buf);
              memcpy(out + len, buf, val_len);
              len += val_len;
            }           
            break;
          }
          case 's':{
            char *s = va_arg(ap, char*);
            memcpy(out + len, s, strlen(s));
            len += strlen(s);
            break;
          }
          case 'x':{
            int val = va_arg(ap, int);
            if(para_len > 0){
              int val_len = itox(val, buf);
              int off = 0;
              if(para_len > val_len){
                for(int i=0;i<para_len - val_len;++i){
                  *(out + len + i) = '0';
                }
                off = para_len - val_len;
              }
              memcpy(out + len + off, buf, val_len);
              len += off + val_len;
            }
            else{
              int val_len = itox(val, buf);
              memcpy(out + len, buf, val_len);
              len += val_len;
            }           
            break;            
          }
        }
        break;
      }
      default: 
      {
        *(out + len) = *fmt;
        ++len;
        break;
      }
    }
    fmt++;
  }
  *(out + len) = *fmt;
  ++len;
  return len;
}


#endif
