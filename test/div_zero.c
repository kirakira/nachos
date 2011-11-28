#include "stdio.h"
#include "stdlib.h"

int main() {
  __asm__ (
       "div\t$10,$0,$0"
	   );
}
