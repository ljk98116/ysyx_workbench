#include "trap.h"

int main(){
    char hello_str[20] = "hello_world";
    check(strlen(hello_str) == 11);
    char ljk_str[] = ",LJK";
    check(strlen(ljk_str) == 4);

    char *hello_ljk_str = strcat(hello_str, ljk_str);
    check(strcmp(hello_ljk_str, "hello_world,LJK") == 0);
    check(strcmp(hello_ljk_str, "hello_world,OKK") < 0);
    check(strcmp("hello_world,OKK", hello_ljk_str) > 0);

    char copyed_str[20];
    memset(copyed_str, 0, sizeof(copyed_str));
    strncpy(copyed_str, hello_ljk_str, 12);
    check(strncmp(copyed_str, hello_ljk_str, 11) == 0);
    strncpy(copyed_str, ljk_str, 4);
    check(strncmp(copyed_str + 11, ljk_str, 4) == 0);

    char copyed_str2[20];
    strcpy(copyed_str2, hello_ljk_str);
    check(strcmp(copyed_str2, hello_ljk_str) == 0);
    return 0;
}