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

    r = symlink("/tmp/aaa", "b");
    if (r == -1)
        printf("link failed\n");

    close(fd);

    fd = open("b");

    if (fd == -1)
        printf("open failed");

    r = read(fd, buffer, sizeof(buffer));
    printf("%s\n", buffer);

    close(fd);

    return 0;
}
