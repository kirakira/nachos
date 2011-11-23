#include "stdio.h"

int main(int argc, char *argv[])
{
    int i, pid, status;
    char *arg[2] = {"aaaaa", "hahahaaaaa"};
    if (argc == 0) {
        printf("No arguments, creating another process\n");
        pid = exec("arg.coff", 2, arg);
        join(pid, &status);
        printf("Over, quitting\n");
    } else {
        for (i = 0; i < argc; ++i)
            printf("%s\n", argv[i]);
    }
    return 0;
}
