#include "vmgrader.h"
#include "coffgrader.h"
#include "syscall.h"

#define PAGE_SIZE 128
#define MATRIX_SIZE 64
#define INIT_SIZE PAGE_SIZE * 16

int swapSize;
int arr[MATRIX_SIZE][MATRIX_SIZE];

inline
int valueAt(int i, int j) {
  return 2543 * i + j;
}

int add_stack(int depth) {
  int i, j;
  int old = swapSize;
  for(j = 0;j < MATRIX_SIZE;++j)
	  for(i = 0;i < MATRIX_SIZE;++i)
		  arr[i][j] = valueAt(i, j);
  swapSize = getSwapSize();
  assertTrue(swapSize >= old);
  if (depth > 1)
    if(add_stack(depth - 1) == -1)
		fail();
  for(j = 0;j < MATRIX_SIZE;++j)
	  for(i = 0;i < MATRIX_SIZE;++i)
		  if(arr[i][j] != valueAt(i, j)) {
			  printf("incorrect value!\n");
			  fail();
		  }
}

int main() {
  assertTrueWMsg(phyPages() <= 16, "too many pages to run this test.");
  if(phyPages() > 64)
	  fail();
  swapSize = getSwapSize();
  assertTrue(swapSize > 0);
  if(add_stack(2) == -1)
	  fail();
  done();
  return 0;
}
