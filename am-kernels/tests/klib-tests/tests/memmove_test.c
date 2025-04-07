#include "trap.h"

char a[6] = {2, 2, 3, 4, 5, 6};
//2 2 4 5 6 6
int main(){
    memmove(a+2, a+3, 3);
    for(int i=0;i<6;++i){
        if(i < 2) check(a[i] == 2);
        if(i == 2) check(a[i] == 4);
        if(i == 3) check(a[i] == 5);
        if(i > 3) check(a[i] == 6);
    }
    return 0;
}