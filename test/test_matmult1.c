#include "stdio.h"
#include "stdlib.h"
int main() {
  int i, p1, p2, status;
  for (i = 0; i < 5; ++i) {
    p1 = exec("matmult_syn1.coff", 0, 0);
    p2 = exec("matmult_syn1.coff", 0, 0);
    if (p1 == -1 || p2 == -1)
        printf("exec failed\n");
    if (p1 != -1)
        join(p1, &status);
    if (p2 != -1)
        join(p2, &status);
  }
  return 0;
}
