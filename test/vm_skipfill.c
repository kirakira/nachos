#include "vmgrader.h"
//
// Xiangru Chen
//
int main() {
  int children = readParameter(0);
  int pid[20];
  int i, j;
  assertTrueWMsg(children > 0, "there should be at lease one child.");
  for (i = 0; i < children; ++i) {
    pid[i] = exec("vm_skipfill_child.coff", 0, 0);
    assertTrueWMsg(pid[i] > 0, "exec fail.");
  }
  for (i = 0; i < children; ++i) {
    int status;
    assertTrue(join(pid[i], &status) == 1);
  }
  done();
  return 0;
}
