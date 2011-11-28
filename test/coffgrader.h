#ifndef COFFGRADER_H_
#define COFFGRADER_H_
//
// Kang Zhang
//
#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int ag1(int a0);
int ag2(int a0, int a1);
int ag3(int a0, int a1, int a2);

__asm__("\
ag1:\
ag2:\
ag3:\
ag4:\
	addiu	$2, $0, -1	; \
	syscall			; \
	j	$31		; \
");

#define ACTION_DONE     0
#define ACTION_FAIL     1
#define ACTION_P        2
#define ACTION_V        3
#define ACTION_READ     4
#define ACTION_STORE    5
#define ACTION_RANDOM   6
#define ACTION_READ_PAR 7

inline
void P(int semaphoreID){
  ag2(ACTION_P,semaphoreID);
}

inline
void V(int semaphoreID){
  ag2(ACTION_V,semaphoreID);
}

inline
void storeValue(int index,int value){
  ag3(ACTION_STORE,index,value);
}

inline
int readValue(int index){
  return ag2(ACTION_READ,index);
}

inline
int random(int n){
  return ag2(ACTION_RANDOM,n);
}

inline
int readParameter(int index){
  return ag2(ACTION_READ_PAR,index);
}

inline
void done(){
  ag1(ACTION_DONE);
}

inline
void fail(){
  ag1(ACTION_FAIL);
}

inline
void assertTrue(int value){
  if( !value )
    fail();
}

#endif //COFFGRADER_H_
