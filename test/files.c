#include "syscall.h"

int main(void) {
    int fd, fd2, r, i;
    char buffer[] = "asjl1";

    r = mkdir("tmp/");
    if (r == -1) {
        printf("mkdir failed\n");
    }

    fd = creat("tmp/aaa");


    if (fd == -1) {
        printf("create failed\n");
        exit(-1);
    }

    r = write(fd, buffer, sizeof(buffer));
    if (r != sizeof(buffer))
        printf("write returned %d\n", r);

    r = unlink("/tmp/aaa");
    if (r != 0)
        printf("rm failed");

    close(fd);

    fd = open("/tmp/aaa");
    if (fd != -1)
        printf("open succeeded");

    fd = creat("/tmp/aaa");

    if (fd == -1)
        printf("create failed");

    r = read(fd, buffer, sizeof(buffer));
    if (r > 0)
        printf("file is old");

    close(fd);

    return 0;
}
