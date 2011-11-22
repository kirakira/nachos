#include "stdio.h"
#include "stdlib.h"
#include "coffgrader.h"
//
// Xiangru Chen
//
int main() {
  int i;
  for (i = 0; i < 10; ++i) {
    exec("matmult_syn.coff", 0, 0);
  }
  V(0);
  for (i = 0; i < 10; ++i) {
    P(1);
  }
  done();
  return 0;
}
