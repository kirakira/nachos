#include "syscall.h"

int main()
{
    char *argv[1];
    exec("deadloop.coff", 0, argv);
    return 0;
}
