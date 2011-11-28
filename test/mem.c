#include "stdio.h"
#include "stdlib.h"
#include "coffgrader.h"
//
// Chunzhi Su
// calc fib mod 15485863, allocate many memory
//

int a[1000];

int main(int argc, char* argv[])
{
	int id = atoi(argv[1]);
//	printf("child %d start.\n", id);
	
	int i;
	for (i = 0; i < 1000; ++i)
		a[i] = 0;

	a[0] = 1;
	a[1] = 1;
	for (i = 2; i < 1000; ++i)
		a[i] = (a[i - 1] + a[i - 2]) % 15485863;
    exit(0);
//	printf("Answer : %d\n", a[999]);
//	printf("child %d done.\n", id);
}
