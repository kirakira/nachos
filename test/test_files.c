/*
 *  Test the syscalls associated with files.
 *
 *  Xiangru Chen
 */

#include "stdio.h"
#include "stdlib.h"
#include "coffgrader.h"

int main() {
  char filename[20];
  strcpy(filename, "testfile");
  sprintf(filename + strlen(filename), "%d", random(10000));
  FILE f = creat(filename);
  assertTrue(f > 1);
  char* msg = "this is a test.";
  int len = strlen(msg);
  assertTrue(write(f, msg, len) == len);
  close(f);
  assertTrue(write(f, "should be error", 4) == -1);

  f = open(filename);
  assertTrue(f > 1);
  char buf[20];
  assertTrue(read(f, buf, len + 1) == len);
  assertTrue(strcmp(msg, buf) == 0);
  assertTrue(read(f, buf, 1) == 0);
  close(f);
  assertTrue(read(f, buf, 1) == -1);

  unlink(filename);
  assertTrue(open(filename) == -1);

  done();

  return 0;
}
