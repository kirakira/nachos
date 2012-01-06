#include "syscall.h"
#include "coffgrader.h"
#include "filesysgrader.h"

int main() {
	int fd = open("dir1");
	assertTrue(fd >= 2);
	int status;
	join(exec("wait.coff", 0, 0), &status);
	char* buf = "echo.coff";
	write(fd, buf, 20);
	return 0;
}