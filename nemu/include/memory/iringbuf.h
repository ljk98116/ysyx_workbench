#ifndef __MEMORY_IRINGBUF_H__
#define __MEMORY_IRINGBUF_H__

#include <stdint.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
    void *buf;
    uint32_t length;
    uint32_t start;
    uint32_t end;
} iringbuf_t;

iringbuf_t *iringbuf_create(uint32_t len);
int iringbuf_destroy(iringbuf_t *buf);
int iringbuf_write(iringbuf_t *buf, const void *data, size_t len);
int iringbuf_empty(iringbuf_t *buf);
int iringbuf_full(iringbuf_t *buf);
int iringbuf_read(iringbuf_t *buf, void *dst, size_t len);
void iringbuf_print(iringbuf_t *buf);

#define ITRACE_INFO_SIZE 64
#define ITRACE_INST_NUM 10
#define ITRACE_BUFFER_SIZE ITRACE_INFO_SIZE * ITRACE_INST_NUM

#endif