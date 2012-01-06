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
// if your file system doesn't support more than 20 files per dictory
// you must be failed
  sprintf(loc,"source.q\0");
  int src, dst, amount;
  buf[BUFSIZE]='\0';

// we need a source text file to continue
  dst = creat(loc);
  if (dst==-1) {
    printf("Unable to create file\n");
    fail();
  }
  close(dst);
  dst = open(loc);
  if (dst==-1) {
    printf("Unable to open /source.q file\n");
    fail();
  }
  int i=0;
  int j=0;
  for(i=0;i<10;i++)
	  buf[i]='1';
  int size=0;
  int ret=0;
  ret=write(dst, buf, BUFSIZE);
  close(dst);

// phase 1 directory create
  for(i=0;i<=20;i++)
  {
    sprintf(loc2,"000000000000000000000000000000D%d\0",i);
    printf("Creating %s\n",loc2);
    assertTrueWMsg(mkdir(loc2)==0,"Create failed");
  }
  assertTrueWMsg(chdir(loc2)==0,"Create error");
  
//phase 2 file copy
  for(i=0;i<=20;i++)
  {
    src = open(loc);
    if (src==-1) {
      printf("Unable to open %s\n", argv[1]);
      fail();
    }

    sprintf(loc2,"/000000000000000000000000000000D%d%s\0",i,loc);
    dst = creat(loc2);
    if (dst==-1) {
      printf("Unable to create file\n");
      fail();
    }
    close(dst);
    dst = open(loc2);
    if (dst==-1) {
      printf("Unable to open %s file\n",loc2);
      fail();
    }
    printf("Creating %s\n",loc2);
    while ((amount = read(src, buf, BUFSIZE))>0) {
      write(dst, buf, amount);
    }
    close(src);
    close(dst);
  }
  sprintf(loc2,"source.q");
  dst = open(loc2);
  if (dst==-1) {
    printf("Unable to open %s file\n",loc2);
    fail();
  }

//phase 3 file consis.
  assertTrueWMsg(unlink("/source.q")==0,"failed to unlink /source.q");
  assertTrueWMsg(unlink("/000000000000000000000000000000D10/source.q")==0,"failed to unlink /000000000000000000000000000000D10/source.q");
  assertTrueWMsg(!unlink("/000000000000000000000000000000D100/source.q")==0,"unlink /000000000000000000000000000000D100/source.q which doesn't exist");
  assertTrueWMsg(unlink("/000000000000000000000000000000D20/source.q")==0,"failed to unlink /000000000000000000000000000000D20/source.q");

  assertTrueWMsg((src=creat("/000000000000000000000000000000D10/source.q"))!=-1,"failed to create /000000000000000000000000000000D10/source.q again");
  close(src);

  assertTrueWMsg((src=open("/000000000000000000000000000000D20/source.q"))==-1,"open /000000000000000000000000000000D20/source.q again on deleting it without create new file");

  assertTrueWMsg((src=creat("/000000000000000000000000000000D20/source.q"))!=-1,"failed to create /000000000000000000000000000000D20/source.q again");
  close(src);
  assertTrueWMsg((src=open("/000000000000000000000000000000D20/source.q"))!=-1,"failed to open new /000000000000000000000000000000D20/source.q");
  assertTrueWMsg(unlink("/000000000000000000000000000000D20/source.q")==0,"failed to unlink new /000000000000000000000000000000D20/source.q");
  assertTrueWMsg((open("/000000000000000000000000000000D20/source.q"))==-1,"open /000000000000000000000000000000D20/source.q again on deleting it second time");
  close(src);


  close(dst);
  assertTrueWMsg((src=creat("/000000000000000000000000000000D20/source.q"))!=-1,"failed to create /000000000000000000000000000000D20/source.q again");
  close(src);
done();
  return 0;
}
