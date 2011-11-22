#include "stdio.h"
#include "stdlib.h"

int main() {
  char filename[20];
  strcpy(filename, "testfile");
  char* msg = "this is a test.";
  int len = strlen(msg);
  FILE f = creat(filename);
  if (!(f > 1))
  {
      printf("wrong1");
      exit(0);
  }
  if (!(write(f, msg, len) == len))
  {
      printf("wrong2");
      exit(0);
  }
  close(f);
  if (write(f, "should be error", 4) != -1)
  {
      printf("wrong3");
      exit(0);
  }

  f = open(filename);
  if (!(f > 1))
  {
      printf("wrong4");
      exit(0);
  }
  char buf[20] = {0};
  if (!(read(f, buf, len + 1) == len))
  {
      printf("wrong5 %d %s", len, buf);
      exit(0);
  }
  if (strcmp(msg, buf) != 0)
  {
      printf("wrong6");
      exit(0);
  }
  if (read(f, buf, 1) != 0)
  {
      printf("wrong7");
      exit(0);
  }
  close(f);
  if (read(f, buf, 1) != -1)
  {
      printf("wrong8");
      exit(0);
  }

  unlink(filename);
  if (open(filename) != -1)
  {
      printf("wrong9");
      exit(0);
  }

  printf("success");
  return 0;
}
