#include "syscall.h"

int main(void) {
    int fd = creat("/aaa"), fd2, r, i;
    char buffer[] = "asjl1";

    if (fd == -1) {
        printf("create failed\n");
        exit(-1);
    }

    r = write(fd, buffer, sizeof(buffer));
    if (r != sizeof(buffer))
        printf("write returned %d\n", r);
    r = read(fd, buffer, sizeof(buffer));
    printf("%s\n", buffer);

    if (unlink("/aaa") == -1)
        printf("unlink failed\n");

    fd2 = creat("/aaa");
    if (fd != -1)
        printf("creat succeeded\n");

    close(fd);

    fd = creat("/aaa");
    if (fd == -1)
        printf("creat failed\n");

    for (i = 0; i < sizeof(buffer); ++i)
        buffer[i] = 0;
    r = read(fd, buffer, sizeof(buffer));
    if (r > 0)
        printf("it is not empty file\n");

    close(fd);

    return 0;
}
