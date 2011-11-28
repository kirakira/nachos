#include "stdio.h"
#include "stdlib.h"

int main() {
  __asm__ (
	   "addiu\t$2, $0, -2\n\t"
	   "syscall"
	   );
}
