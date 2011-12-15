#include "syscall.h"
#include "stdio.h"
#include "coffgrader.h"
#include "vmgrader.h"

int main() {
	assertTrueWMsg(phyPages() <= 16, "this test requires no more than 16 physical pages\n");
	if(phyPages() > 16)
		fail();
	int pid[4];
	int i;
	int status;
	for(i = 0;i < 2;++i)
		pid[i] = exec("test_matrix2.coff", 0, 0);
	for(i = 2;i < 4;++i)
		pid[i] = exec("matmult.coff", 0, 0);
	join(exec("wait.coff", 0, 0), &status);
	if(status != 0) {
		printf("incorrect value! of wait\n");
		fail();
	}
	for(i = 0;i < 2;++i) {
		join(pid[i], &status);
		if(status != 0) {
			printf("incorrect value! of matrix2\n");
			fail();
		}
	}
	for(i = 2;i < 4;++i) {
		join(pid[i], &status);
		if(status != 7220) {
			printf("incorrect value of matmult!\n");
			fail();
		}
	}
	done();
	return 0;
}