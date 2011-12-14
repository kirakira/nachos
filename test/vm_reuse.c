#include "vmgrader.h"
//
// Xiangru Chen
//
#define PAGE_SIZE 1024

void run_child() {
  int pid = exec("vm_reuse_child.coff", 0, 0);
  assertTrueWMsg(pid > 0, "exec fail.");
  int status;
  assertTrueWMsg(join(pid, &status) == 1, "child exited abnormally.");
}

int main() {
  assertTrueWMsg(phyPages() <= 4, "too many pages to run this test.");
  int times = readParameter(0);
  int i;
  run_child();
  int swapSize = getSwapSize();
  assertTrue(swapSize > 0);
  for (i = 0; i < times; ++i) {
    run_child();
    assertTrueWMsg(getSwapSize() <= swapSize + 8 * PAGE_SIZE, "fail: used too many swap space");
  }
  done();
}
