#include "stdio.h"
#include "stdlib.h"
#include "coffgrader.h"
//
// Xiangru Chen
//
int main() {
  int i, pid, status;
  for (i = 0; i < 10; ++i) {
    pid = exec("hello.coff", 0, 0);
    assertTrue(pid > 0);
    assertTrue(join(pid, &status) == 1);
    assertTrue(status == 0);
  }
  printf("done.\n");

  done();

  return 0;
}
