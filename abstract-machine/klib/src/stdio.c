#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

int printf(const char *fmt, ...) {
  va_list ap;
  int ret = 0;
  va_start(ap, fmt);
  while(*fmt) {
    if (*fmt == '%') {
      ++fmt;
      size_t para_len = 0;
      while (*fmt >= '0' && *fmt <= '9') {
        para_len = para_len * 10 + (*fmt - '0');
        ++fmt;
      }
      switch (*fmt) {
        case 'd': {
          int arg = va_arg(ap, int);
          /* 倒序 */
          char val[32];
          memset(val, 0, sizeof(val));
          int cnt = 0;
          int n = arg;
          if(n == 0) val[0] = '0';
          while (n) {
            val[cnt++] = n % 10 + '0';
            n /= 10;
          }
          size_t arglen = strlen(val);
          /* 翻转 */
          for (int i=0; i< arglen / 2; ++i) {
            char tmp = val[i];
            val[i] = val[arglen - i - 1];
            val[arglen - i - 1] = tmp;
          }
          if (arglen < para_len && para_len > 0) {
            int has_minus = arg < 0 ? 1 : 0;
            if (has_minus) {
              putch('-');
              ++ret;
            }
            for (int i=0;i<para_len - arglen - has_minus;++i) {
              putch('0');
              ++ret;
            }
          }
          for(int i=0;i<arglen;++i) {
            putch(val[i]);
            ++ret;
          }
          break;
        }
        case 'u': {
          uint32_t arg = va_arg(ap, uint32_t);
          /* 倒序 */
          char val[32];
          memset(val, 0, sizeof(val));
          int cnt = 0;
          if(arg == 0) val[0] = '0';
          while (arg) {
            val[cnt++] = arg % 10 + '0';
            arg /= 10;
          }
          size_t arglen = strlen(val);
          /* 翻转 */
          for (int i=0; i< arglen / 2; ++i) {
            char tmp = val[i];
            val[i] = val[arglen - i - 1];
            val[arglen - i - 1] = tmp;
          }
          if (arglen < para_len && para_len > 0) {
            for (int i=0;i<para_len -arglen;++i) {
              putch('0');
              ++ret;
            }
          }
          for(int i=0;i<arglen;++i) {
            putch(val[i]);
            ++ret;
          }
          break;
        }
        case 'x': {
          uint32_t arg = va_arg(ap, uint32_t);
          /* 倒序 */
          char val[32];
          memset(val, 0, sizeof(val));
          int cnt = 0;
          if(arg == 0) val[0] = '0';
          while (arg) {
            val[cnt++] = arg % 16 >= 10 ? (arg % 16 - 10 + 'A') : (arg % 16 + '0');
            arg /= 16;
          }
          size_t arglen = strlen(val);
          /* 翻转 */
          for (int i=0; i< arglen / 2; ++i) {
            char tmp = val[i];
            val[i] = val[arglen - i - 1];
            val[arglen - i - 1] = tmp;
          }
          if (arglen < para_len && para_len > 0) {
            for (int i=0;i<para_len -arglen;++i) {
              putch('0');
              ++ret;
            }
          }
          for(int i=0;i<arglen;++i) {
            putch(val[i]);
            ++ret;
          }
          break;          
        }
        case 's': {
          const char *arg = va_arg(ap, const char *);
          size_t arglen = strlen(arg);
          for (int i=0; i<arglen; ++i) {
            putch(arg[i]);
            ++ret;
          }
          break;
        }
        case 'c': {
          char arg = va_arg(ap, int);
          putch(arg);
          ++ret;
          break;          
        }
      }
      ++fmt; 
    }
    else {
      putch(*fmt);
      ++fmt;
      ++ret;
    }
  }
  putch('\0');
  ++ret;
  va_end(ap);
  return ret;
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  return vsnprintf(out, -1, fmt, ap);
}

int sprintf(char *out, const char *fmt, ...) {
  va_list ap;
  int ret = 0;
  va_start(ap, fmt);
  ret = vsprintf(out, fmt, ap);
  va_end(ap);
  return ret;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  va_list ap;
  int ret = 0;
  va_start(ap, fmt);
  ret = vsnprintf(out, n, fmt, ap);
  va_end(ap);
  return ret;  
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  int ret = 0;
  while(*fmt != '\0'&& ret < n-2){
    if (*fmt == '%') {
      ++fmt;
      size_t para_len = 0;
      while (*fmt >= '0' && *fmt <= '9') {
        para_len = para_len * 10 + (*fmt - '0');
        ++fmt;
      }
      switch (*fmt) {
        case 'd': {
          int arg = va_arg(ap, int);
          /* 倒序 */
          char val[32];
          memset(val, 0, sizeof(val));
          int cnt = 0;
          int n = arg;
          if(n == 0) val[0] = '0';
          while (n) {
            val[cnt++] = n % 10 + '0';
            n /= 10;
          }
          size_t arglen = strlen(val);
          /* 翻转 */
          for (int i=0; i< arglen / 2; ++i) {
            char tmp = val[i];
            val[i] = val[arglen - i - 1];
            val[arglen - i - 1] = tmp;
          }
          if (arglen < para_len && para_len > 0) {
            int has_minus = arg < 0 ? 1 : 0;
            if (has_minus) {
              if (ret >= n - 2) goto end;
              *(out + ret) = '-';
              ++ret;
            }
            for (int i=0;i<para_len - arglen - has_minus;++i) {
              if (ret >= n - 2) goto end;
              *(out + ret) = '0';
              ++ret;
            }
          }
          for(int i=0;i<arglen;++i) {
            if (ret >= n - 2) goto end;
            *(out + ret) = val[i];
            ++ret;
          }
          break;
        }
        case 'u': {
          uint32_t arg = va_arg(ap, uint32_t);
          /* 倒序 */
          char val[32];
          memset(val, 0, sizeof(val));
          int cnt = 0;
          if(arg == 0) val[0] = '0';
          while (arg) {
            val[cnt++] = arg % 10 + '0';
            arg /= 10;
          }
          size_t arglen = strlen(val);
          /* 翻转 */
          for (int i=0; i< arglen / 2; ++i) {
            char tmp = val[i];
            val[i] = val[arglen - i - 1];
            val[arglen - i - 1] = tmp;
          }
          if (arglen < para_len && para_len > 0) {
            for (int i=0;i<para_len -arglen;++i) {
              if (ret >= n - 2) goto end;
              *(out + ret) = '0';
              ++ret;
            }
          }
          for(int i=0;i<arglen;++i) {
            if (ret >= n - 2) goto end;
            *(out + ret) = val[i];
            ++ret;
          }
          break;
        }
        case 'x': {
          uint32_t arg = va_arg(ap, uint32_t);
          /* 倒序 */
          char val[32];
          memset(val, 0, sizeof(val));
          int cnt = 0;
          if(arg == 0) val[0] = '0';
          while (arg) {
            val[cnt++] = arg % 16 >= 10 ? (arg % 16 - 10 + 'A') : (arg % 16 + '0');
            arg /= 16;
          }
          size_t arglen = strlen(val);
          /* 翻转 */
          for (int i=0; i< arglen / 2; ++i) {
            char tmp = val[i];
            val[i] = val[arglen - i - 1];
            val[arglen - i - 1] = tmp;
          }
          if (arglen < para_len && para_len > 0) {
            for (int i=0;i<para_len -arglen;++i) {
              if (ret >= n - 2) goto end;
              *(out + ret) = '0';
              ++ret;
            }
          }
          for(int i=0;i<arglen;++i) {
            if (ret >= n - 2) goto end;
            *(out + ret) = val[i];
            ++ret;
          }
          break;          
        }
        case 's': {
          const char *arg = va_arg(ap, const char *);
          size_t arglen = strlen(arg);
          for (int i=0; i<arglen; ++i) {
            if (ret >= n - 2) goto end;
            *(out + ret) = arg[i];
            ++ret;
          }
          break;
        }
        case 'c': {
          char arg = va_arg(ap, int);
          if (ret >= n - 2) goto end;
          *(out + ret) = arg;
          ++ret;
          break;          
        }
      }
      ++fmt;      
    }
    else {
      if (ret >= n - 2) goto end;
      *(out + ret) = *fmt;
      ++fmt;
      ++ret;
    }
  }
end:
  *(out + ret) = '\0';
  ++ret;
  return ret;
}


#endif
