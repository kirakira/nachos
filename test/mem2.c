#include "stdio.h"
#include "stdlib.h"
#include "coffgrader.h"
//
// Chunzhi Su
// calc fib mod 15485863, allocate many memory
//

int a[10000];

int main()
{
	int i;
	for (i = 0; i < 10000; ++i)
		a[i] = 0;

	a[0] = 1;
	a[1] = 1;
	for (i = 2; i < 10000; ++i)
		a[i] = (a[i - 1] + a[i - 2]) % 15485863;

//	printf("%d\n", a[9999]);
    exit(0);
}
