#include "vmgrader.h"
//
// Xiangru Chen
//
#define PAGE_SIZE 1024
#define INIT_SIZE PAGE_SIZE * 16

int swapSize;
int skip;

inline
int valueAt(int i, int j) {
  return (i ^ j) & 127;
}

void add_stack(int depth) {
  char a[PAGE_SIZE+1];
  int i;
  for (i = 0; i < PAGE_SIZE; i += (1 << skip))
    a[i] = valueAt(depth, i);
  int old = swapSize;
  swapSize = getSwapSize();
  assertTrue(swapSize >= old);
  if (depth > 1)
    add_stack(depth - 1);
  for (i = 0; i < PAGE_SIZE; i += (1 << skip))
    assertTrue(a[i] == valueAt(depth, i));
}

int main() {
  assertTrueWMsg(phyPages() <= 16, "too many pages to run this test.");
  int depth = readParameter(0);
  assertTrueWMsg(depth > 0, "depth should > 0.");
  skip = readParameter(1);
  assertTrueWMsg(skip >= 0, "skip factor should >= 0.");
  char a[PAGE_SIZE * 16];
  int i;
  for (i = 0; i < INIT_SIZE; i += PAGE_SIZE)
    a[i] = 1;
  swapSize = getSwapSize();
  assertTrue(swapSize > 0);
  add_stack(depth);
  done();
  return 0;
}
