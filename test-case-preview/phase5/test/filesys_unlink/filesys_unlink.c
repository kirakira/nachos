#include "syscall.h"
#include "coffgrader.h"
#include "filesysgrader.h"

int main() {
	assertTrue(!mkdir("dir1"));
	int fd = creat("dir1");
	assertTrue(fd >= 2);
	int status;
	int pid1 = exec("unlink_writer.coff", 0, 0);
	assertTrue(!rmdir("dir1"));
	int pid2 = exec("unlink_reader.coff", 0, 0);
	join(pid1, &status);
	unlink("dir1");
	join(pid2, &status);
	done();
}