#include "syscall.h"
#include "coffgrader.h"
#include "filesysgrader.h"

int main() {
	int fd = open("dir1");
	int status;
	join(exec("wait.coff", 0, 0), &status);
	join(exec("wait.coff", 0, 0), &status);
	char buf[20];
	read(fd, buf, 20);
	char file[10];
	int i;
	for(i = 0;i < 10;++i) {
		if(buf[i] != 0)
			file[i] = buf[i];
		else {
			file[i] = '\0';
			break;
		}
	}
	int pid = exec(file, 0, 0);
	assertTrue(pid > 0);
	join(pid, &status);
	return 0;
}