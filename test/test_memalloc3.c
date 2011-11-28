#include "stdio.h"
#include "stdlib.h"
#include "coffgrader.h"
//
// Chunzhi Su
// Test memory re-allocation.
// Use many child to overwhelme the whole memory, run one by one.
//
int main()
{
	int i;
	int pid;
	int status;

	printf("May be slow. 100 times.\n");
	for (i = 0; i < 100; ++i)
	{
		printf("%d\n", i);
		pid = exec("mem2.coff", 0, 0);
		assertTrue(pid > 0);
		assertTrue(join(pid, &status) == 1);
		assertTrue(status == 0);
	}
	
	printf("done.\n");
	done();
	return 0;
}
