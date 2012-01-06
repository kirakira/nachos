#ifndef FILESYSGRADER_H_
#define FILESYSGRADER_H_
//
// Bo Tang
//
#include "coffgrader.h"

#define ACTION_GETDISKFREESIZE 22

// Get the free size of disk
inline
int getFreeDiskSize() {
  return ag1(ACTION_GETDISKFREESIZE);
}

inline
void assertTrueWMsg(int value, char* msg) {
  if (!value)
    printf("%s\n", msg);
  assertTrue(value);
}

inline
void printCwd(){
	char buf[100];
    getcwd(buf, 100);
    printf("%s\r\n", buf);
}

#endif
