#include "vmgrader.h"
//
// Xiangru Chen
//
#define SIZE 65536

char space[SIZE];

int main() {
  assertTrueWMsg(phyPages() <= 16, "too many pages to run this test.");
  assertTrue(phyPages() + getSwapSize() < SIZE);
  done();
  return 0;
}
