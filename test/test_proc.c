/*
 * Test the syscalls associated with processes.
 *
 * Xiangru Chen
 */

#include "stdio.h"
#include "stdlib.h"
#include "coffgrader.h"

int main() {

  char* args[10];
  int pid[20];
  int children = 10;

  args[0] = "";
  args[2] = 0;

  char tmp[20];

  int i;
  for (i = 0; i < children; ++i) {
    sprintf(tmp, "%d", i);
    args[1] = tmp;
    pid[i] = exec("test_proc_child.coff", 2, args);
    assertTrue(pid[i] > 0);
  }

  printf("I'm the father.\n");

  V(0);

  int status;
  for (i = children - 1; i >= 0; --i) {
    assertTrue(join(pid[i], &status) == 1);
    assertTrue(status == 100 + i);
  }

  assertTrue(join(0, &status) == -1);

  int mustfail = exec("mustfail.coff", 0, 0);

  assertTrue(join(mustfail, &status) == 0);

  assertTrue(exec("file_not_exist", 0, 0) == -1);

  printf("seems ok.\n");

  done();

  return 0;
}
