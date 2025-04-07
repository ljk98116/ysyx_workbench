#include "trap.h"

char a[6] = {5, 6, 7, 8, 9, 11};
char b[6] = {5, 6, 7, 8, 10, 11};

int main()
{
    check(memcmp(a, b, sizeof(a)) < 0);
    check(memcmp(b, a, sizeof(a)) > 0);
    b[4] = 9;
    check(memcmp(a, b, sizeof(a)) == 0 && memcmp(b, a, sizeof(a)) == 0);
    return 0;
}