#include "vmgrader.h"
//
// Xiangru Chen
//
#define PAGE_SIZE 1024
#define SIZE 1024

char a[SIZE][PAGE_SIZE];

inline
char valueAt(int i, int j) {
  return (i ^ j) & 127;
}

int main() {
  int size = readParameter(1);
  int skip = readParameter(2);
  int i, j;
  assertTrueWMsg(0 < size && size <= SIZE, "illegal size value");
  assertTrueWMsg(skip >= 0, "the skip factor should >= 0.");
  for (j = 0; j < PAGE_SIZE; j += (1 << skip))
    for (i = 0; i < size; ++i)
      a[i][j] = valueAt(i, j);
  for (j = 0; j < PAGE_SIZE; j += (1 << skip))
    for (i = 0; i < size; ++i)
      assertTrue(a[i][j] == valueAt(i, j));
  return 0;
}
