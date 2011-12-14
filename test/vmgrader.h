#ifndef VMGRADER_H_
#define VMGRADER_H_
//
// Xiangru Chen
//
#include "coffgrader.h"

#define ACTION_PHYPAGES 20
#define ACTION_GETSWAPSIZE 21

// Get the number of physical pages
inline
int phyPages() {
  return ag1(ACTION_PHYPAGES);
}

// Get the size of swap file
inline
int getSwapSize() {
  return ag1(ACTION_GETSWAPSIZE);
}

inline
void assertTrueWMsg(int value, char* msg) {
  if (!value)
    printf("%s\n", msg);
  assertTrue(value);
}

#endif
