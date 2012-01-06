#include "filesysgrader.h"
#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

#define BUFSIZE 1024
#define BUFSIZE_s 50
char loc[BUFSIZE+1];
char loc2[BUFSIZE+1];
char buf[BUFSIZE+1];
char buf2[BUFSIZE_s];
int main(int argc, char** argv)
{

  sprintf(loc,"567890123456789.1234\0");
  int src, dst, amount;
  buf[BUFSIZE]='\0';
  /*if (argc!=2) {
    printf("Usage: fs_size <dst>\n");
    return 1;
  }
*/
  creat(loc);
  dst = open(loc);
  if (dst==-1) {
    printf("Unable to create file\n", loc);
    return 1;
  }
  int i=0;
  for(i=0;i<BUFSIZE;i++)
	  buf[i]='1';

  int size=0;
  int ret=0;
  while (size<1024*120) {
    ret=write(dst, buf, BUFSIZE);
    size+=ret;
    printf("%d:%d\n", size,ret);
  }
  i=size;
  close(dst);
  printf("typing\n");
  src = open(loc);
  if (src==-1) {
    printf("Unable to open file");
    return 1;
  }
  size=0;
  while ((amount = read(src, buf2, BUFSIZE_s))>0) {
    printf("%d::%s\n", size, buf2);
    size+=amount;
  }
  if(i!=size)
  {
	printf("size written does not equal to size read");
        fail();
  }
  close(src);
  done();
  return 0;
}
