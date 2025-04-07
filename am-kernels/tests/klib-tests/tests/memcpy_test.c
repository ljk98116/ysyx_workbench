#include "trap.h"

char a[60];
char b[70][8];
char c[30];
int main(){
    memset(a, 6, sizeof(a));
    for(int i=0;i<60;++i){
        check(a[i] == 6);
    }
    memset(b, 100, sizeof(b));
    for(int i=0;i<70;++i)
    for(int j=0;j<8;++j){
        check(b[i][j] == 100);
    }
    memset(c, 80, sizeof(c));
    memcpy(a, c, sizeof(c));
    for(int i=0;i<60;++i){
        if(i < 30) check(a[i] == 80);
        else check(a[i] == 6);
    }
    return 0;
}