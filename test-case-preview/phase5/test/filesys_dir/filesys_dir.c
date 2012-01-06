#include "filesysgrader.h"
#include "stdio.h"
#include "stdlib.h"

char buf[100];

int main(int argc, char** argv)
{
    int size = getFreeDiskSize();
    assertTrue(!mkdir("dir1"));
    assertTrue(!mkdir("dir1/dir1_1"));
    printCwd();
    chdir("dir1/dir1_1");
    printCwd();
    chdir("../../dir1/dir1_1/.././..");// /dir1/dir1_1/../../dir1/dir1_1/.././..==/
    printCwd();
    mkdir("dir2");// mkdir /dir2
    chdir("dir2");// cwd /dir2
	//printCwd();
    //assertTrue(rmdir("/dir1")); // for dir1_1 in /dir1 corrected editor:MLSheng
    assertTrue(rmdir("dir1"));  // test path added by MLSheng
	//printCwd();
    assertTrue(rmdir("/dir1/dir1"));//of couse only /dir1/dir1_1 exists
	//printCwd();
	assertTrue(!rmdir("/dir1/dir1_1"));// or size is not correct corrected editor:MLSheng
	//printCwd();
	assertTrue(!rmdir("/dir1"));// or size is not correct corrected editor:MLSheng
    printCwd();// /dir2
    chdir("/");// cwd /
    assertTrue(!rmdir("/dir2"));
    assertTrue(getFreeDiskSize() == size);
    done();
    return 0;
}
