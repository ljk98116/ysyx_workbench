#include <klib.h>
#include <klib-macros.h>
#include <stdint.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

size_t strlen(const char *s) {
  size_t ret = 0;
  while(*s){
    ret++;
    s++;
  }
  return ret;
}

char *strcpy(char *dst, const char *src) {
  assert(dst && src);
  char *ret = dst;
  while(*src){
    *dst = *src;
    dst++;
    src++;
  }
  *dst = *src;
  return ret;
}

char *strncpy(char *dst, const char *src, size_t n) {
  assert(dst && src);
  for(size_t i=0;i<n;++i){
    *(dst + i) = *(src + i) ? *(src + i) : '\0';
  }
  return dst;
}

char *strcat(char *dst, const char *src) {
  assert(dst && src);
  char *ret = dst;
  while(*dst) dst++;
  while(*src){
    *dst = *src;
    src++;
    dst++;
  }
  return ret;
}

int strcmp(const char *s1, const char *s2) {
  assert(s1 && s2);
  while(*s1 == *s2){
    if(*s1 == '\0' || *s2 == '\0'){
      return 0;
    }
    s1++;
    s2++;
  }
  return *s1 - *s2;
}

int strncmp(const char *s1, const char *s2, size_t n) {
  assert(s1 && s2);
  size_t i = 0;
  while(i < n && *s1 == *s2){
    if(*s1 == '\0' || *s2 == '\0') return 0;
    ++i;
  }
  return *s1 - *s2;
}

void *memset(void *s, int c, size_t n) {
  assert(s);
  for(size_t i=0;i<n;++i){
    *((char *)s + i) = c;
  }
  return s;
}

void *memmove(void *dst, const void *src, size_t n) {
  assert(dst && src);
  void *ret = dst;
  if(dst < src){
    while(n--){
      *((char*)dst++) = *((char*)src++);
    }
  }
  else{
    while(n--){
      *((char*)dst + n) = *((char*)src + n);
    }
  }
  return ret;
}

void *memcpy(void *out, const void *in, size_t n) {
  assert(out && in);
  for(size_t i=0;i<n;++i){
    *((char*)out + i) = *((char*)in + i);
  }
  return out;  
}

int memcmp(const void *s1, const void *s2, size_t n) {
  assert(s1 && s2);
  for(size_t i=0;i<n;++i){
    if(*((char*)s1 + i) != *((char*)s2 + i)) return *((char*)s1 + i) - *((char*)s2 + i);
  }
  return 0;
}

#endif
