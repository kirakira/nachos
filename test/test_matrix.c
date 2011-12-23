//#include "vmgrader.h"
//#include "coffgrader.h"
#include "syscall.h"

#define PAGE_SIZE 128
#define MATRIX_SIZE 32
#define INIT_SIZE PAGE_SIZE * 16

//int swapSize;

inline
int valueAt(int i, int j) {
  return 37 * i + j;
}

int add_stack(int depth) {
	//printf("depthing...");
  int a[MATRIX_SIZE][MATRIX_SIZE];
  int i, j;
  for(j = 0;j < MATRIX_SIZE;++j)
	  for(i = 0;i < MATRIX_SIZE;++i)
		  a[i][j] = valueAt(i, j);
  //int old = swapSize;
  //swapSize = getSwapSize();
  //assertTrue(swapSize >= old);
  if (depth > 1)
    if(add_stack(depth - 1) == -1)
		return -1;
  for(j = 0;j < MATRIX_SIZE;++j)
	  for(i = 0;i < MATRIX_SIZE;++i)
		  if(a[i][j] != valueAt(i, j)) {
			  printf("value incorrect!\n");
			  return -1;
		  }
}

int main() {
  //assertTrueWMsg(phyPages() <= 64, "too many pages to run this test.");
  //assertTrue(phyPages() <= 64);
  //int depth = readParameter(0);
  //assertTrueWMsg(depth > 0, "depth should > 0.");
  //skip = readParameter(1);
  //assertTrueWMsg(skip >= 0, "skip factor should >= 0.");
	//printf("running...");
  char a[PAGE_SIZE * 16];
  int i;
  for (i = 0; i < INIT_SIZE; i += PAGE_SIZE)
    a[i] = 1;
  //swapSize = getSwapSize();
  //assertTrue(swapSize > 0);
  //add_stack(depth);
  if(add_stack(1) == -1)
	  return -1;
  //done();
  return 0;
}
