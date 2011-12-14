/*
 *  Test illegal join.
 *
 *  Chunzhi Su
 */

#include "stdio.h"
#include "stdlib.h"
#include "coffgrader.h"

int main(int argc, char* argv[])
{
	int id = atoi(argv[1]);
    int i;
	if (id <= 0)
	{
		while (true)
		{
            i = 1;
		}
	}
	else
	{
		int status = 0;
		if (join(id, &status) == -1)
			exit(0);
		else
			exit(1);
	}
}
