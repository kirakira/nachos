/*
 *  Test illegal join.
 *
 *  Chunzhi Su
 */

#include "stdio.h"
#include "stdlib.h"
#include "coffgrader.h"

int main()
{
	char* args[10];
	args[0] = "";
	args[1] = "0";
	args[2] = 0;    //end of array

	int pid = exec("child.coff", 2, args);
	assertTrue(pid > 0);

	char tmp[20];
	sprintf(tmp, "%d", pid);
	args[1] = tmp;
	pid = exec("child.coff", 2, args);
	assertTrue(pid > 0);

	int status = 0;
	assertTrue(join(pid, &status) == 1);
	assertTrue(status == 0);

	printf("seems ok.\n");
	done();

	return 0;
}
