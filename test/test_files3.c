#include "stdio.h"
#include "stdlib.h"
#include "coffgrader.h"
//
// Chunzhi Su
//
#define BUFSIZE 2048

int main()
{
	char* filename = "File_Not_Exist";
	FILE g = open(filename);
	assertTrue(g == -1);

	filename = "File_Exist";
	g = open(filename);
	assertTrue(g > 1);

	//int len = strlen(msg);
	char msg[20];
	assertTrue(read(g, msg, 20) == 10);
	assertTrue(strcmp(msg, "hello.coff") == 0);
	close(g);
	
	char tmp[20];
	assertTrue(read(g, tmp, 10) == -1);
	assertTrue(write(g, "should be error", 4) == -1);


	filename = "on_the_fly_delete";
	g = open(filename);
	assertTrue(g == -1);
	g = creat(filename);
	assertTrue(g > 1);

	assertTrue(write(g, "HelloWorld", 10) == 10);
	assertTrue(unlink("on_the_fly_delete") == 0);		//on the fly unlink.
	char tmp2[20];
//	assertTrue(read(g, tmp2, 10) == 10);
//	assertTrue(strcmp(tmp2, "HelloWorld") == 0);

	FILE h = open("on_the_fly_delete");
	assertTrue(h == -1);
	assertTrue(close(h) == -1);
	assertTrue(close(g) == 0);

	h = open("on_the_fly_delete");
	assertTrue(h == -1);

	printf("seems ok.\n");
	done();
	return 0;
}
