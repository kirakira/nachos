#include "vmgrader.h"

#define SIZE 2048

int main() {
  int a[SIZE];
  int i;
  for (i = 0; i < SIZE; ++i)
    a[i] = i;
  for (i = 0; i < SIZE; ++i)
    assertTrue(a[i] == i);
  return 0;
}
