#include "stdio.h"
#include "stdlib.h"
#include "coffgrader.h"
//
// Chunzhi Su
// Test memory allocation on multy processes
// Test join after child has exited.
//
int main()
{
	int i;
	int pid[10];
	int status;
	char* args[10];
	args[0] = "";
	args[2] = 0;    //end of array
	
	for (i = 0; i < 10; ++i)
	{
  		char tmp[20];
  		sprintf(tmp, "%d", i);
  		args[1] = tmp;
		pid[i] = exec("mem.coff", 2, args);
	}

	for (i = 9; i >= 0; --i)	//reverse join, expect some child to have finished when join.
	{
//		printf("Join child %d with pid = %d\n", i, pid[i]);
		assertTrue(pid[i] > 0);
		assertTrue(join(pid[i], &status) == 1);
		assertTrue(status == 0);
//		printf("Join child %d done.\n", i);

	}
	
//	printf("done.\n");
	done();
	return 0;
}
