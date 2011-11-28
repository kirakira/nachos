#include "stdio.h"
#include "stdlib.h"
#include "coffgrader.h"
//
// Xiangru Chen
//
int main(int argc, char* argv[]) {
  int id = atoi(argv[1]);
  int i;
  for (i = 0; i < 10000; ++i);
  P(id);
  printf("I'm child %s.\n", argv[1]);
  V(id + 1);
  exit(100 + id);
  assertTrue(0);
}
