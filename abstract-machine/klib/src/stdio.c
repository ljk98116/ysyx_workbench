#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

int printf(const char *fmt, ...) {
  panic("Not implemented");
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
        switch(*fmt){
          case 'd':{
            int val = va_arg(ap, int);
            int val_len = itoa(val, buf);
            memcpy(out + len, buf, val_len);
            len += val_len;
            break;
          }
          case 's':{
            char *s = va_arg(ap, char*);
            memcpy(out + len, s, strlen(s));
            len += strlen(s);
            break;
          }
        }
        break;
      }
      default: 
      {
        *(out+ len) = *fmt;
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
