#include <memory/iringbuf.h>
#include <stdio.h>
#include <debug.h>

iringbuf_t *iringbuf_create(uint32_t len){
    iringbuf_t *ret = (iringbuf_t*)calloc(1, sizeof(iringbuf_t));
    ret->buf = malloc(len + 1);
    ret->length = len;
    ret->start = 0;
    ret->end = 0;
    memset(ret->buf, '\0', len);
    return ret;
}

int iringbuf_destroy(iringbuf_t *buf){
    if(buf == NULL) return 0;
    if(buf->buf != NULL) free(buf->buf);
    free(buf);
    return 0;
}

//length: 5 0 1 2 3 4 5
//end : 6
int iringbuf_write(iringbuf_t *buf, const void *data, size_t len){
    //异常
    if(buf == NULL) return -1;
    if(buf->buf == NULL) return -2;
    // iringbuf满了
    // if(buf->end - buf->start + len > buf->length) return -3;
    size_t sz = buf->end % (buf->length + 1) + len > buf->length ? buf->length - buf->end % (buf->length + 1) : len;
    memcpy(buf->buf + buf->end % (buf->length + 1), data, sz);
    if(len > sz){
        memcpy(buf->buf, data + sz, len - sz);
    }
    buf->end += len;
    if(buf->end > buf->length) buf->start = buf->end;
    return 0;
}

int iringbuf_empty(iringbuf_t *buf){
    if(buf == NULL) return -1;
    if(buf->buf == NULL) return -2;
    return buf->end - buf->start == 0;
}

int iringbuf_full(iringbuf_t *buf){
    if(buf == NULL) return -1;
    if(buf->buf == NULL) return -2;
    return buf->end - buf->start >= buf->length;    
}

int iringbuf_read(iringbuf_t *buf, void *dst, size_t len){
    if(buf == NULL) return -1;
    if(buf->buf == NULL) return -2;
    size_t sz = buf->start % (buf->length + 1) + len > buf->length ? buf->length - buf->start % (buf->length + 1) : len;
    memcpy(dst, buf->buf + buf->start % (buf->length + 1), sz);
    if(len > sz){
        memcpy(dst + sz, buf->buf, len - sz);
    }
    return 0;
}

void iringbuf_print(iringbuf_t *buf){
    if(buf == NULL) return;
    if(buf->buf == NULL) return;
    size_t start = buf->start % (buf->length + 1);
    size_t end = buf->end % (buf->length + 1);
    if(start < end){
        while(start != end){
            char *p = buf->buf + start;
            putchar(*p);
            ++start;
        }
    }
    else{
        while(start < buf->length){
            char *p = buf->buf + start;
            putchar(*p);
            ++start;            
        }
        size_t i = 0;
        while(i < end){
            char *p = buf->buf + i;
            putchar(*p);
            ++i;            
        }
    }
}