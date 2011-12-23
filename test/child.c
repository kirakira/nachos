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
	if (id <= 0)
	{
		while (true)
		{
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
