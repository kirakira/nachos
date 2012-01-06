#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

char temp[256];
void extend(char* s){
    int i;
    strcpy(temp, "");
    for (i = 0; i + strlen(s) < 10; i++)
        strcat(temp, " ");
    strcat(temp, s);
    strcpy(s, temp);
}

char buf[100][100];
int main(int argc, char** argv)
{
    int size = readdir("/", buf, 100, 100);
    int i;
    FileStat statbuf;
    printf("%s\n", "      name      size      blocks    type      inode     links");
    for (i = 0;  i < size; i++){
        stat(buf[i], &statbuf);
        extend(statbuf.name);
        printf("%s%10d%10d%10d%10d%10d\n",  statbuf.name, statbuf.size, statbuf.sectors,
               statbuf.type, statbuf.inode, statbuf.links);
    }
    return 0;
}
