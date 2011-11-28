#include "stdio.h"
#include "stdlib.h"
#include "coffgrader.h"
//
// Xiangru Chen
//
#define BUFSIZE 2048

int main() {
  char filename[20];
  char buf[BUFSIZE];
  sprintf(filename, "%d", random(1000000));
  strcat(filename, ".coff");
  FILE f = open("hello.coff");
  FILE g = creat(filename);
  assertTrue(f > 1);
  assertTrue(g > 1);
  int readCount;
  while ((readCount = read(f, buf, BUFSIZE)) > 0) {
    assertTrue(write(g, buf, readCount) == readCount);
  }
  close(f);
  close(g);
  int pid = exec(filename, 0, 0);
  assertTrue(pid > 0);
  int status;
  assertTrue(join(pid, &status) == 1);
  assertTrue(status == 0);
  assertTrue(unlink(filename) == 0);
  assertTrue(open(filename) == -1);
  done();
  return 0;
}
