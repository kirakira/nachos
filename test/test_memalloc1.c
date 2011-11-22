#include "stdio.h"
#include "stdlib.h"
int main() {
  int i, pid, status;
  for (i = 0; i < 10; ++i) {
    pid = exec("hello.coff", 0, 0);
    if (pid <= 0 || join(pid, &status) != 1 || status != 0)
    {
        printf("wrong");
        exit(0);
    }
  }
  printf("done.\n");

  return 0;
}
