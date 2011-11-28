#ifndef _USERGRADER1_H_
#define _USERGRADER1_H_

#include "coffgrader.h"

#define ACTION_FILE_EXIST       8
#define ACTION_NUM_OPEN         9
#define ACTION_START_ID         10
#define ACTION_ASSERT_ID        11

#define TEST_FILE_NAME0     "f0"
#define TEST_FILE_NAME1     "f1"

inline int fileExist( int index ){
    return ag2( ACTION_FILE_EXIST , index );
}

inline int numOpen(){
    return ag1( ACTION_NUM_OPEN );
}

inline void startTestID( int id ){
    ag2( ACTION_START_ID , id );
}

inline void assertTrueID( int id , int value ){
    ag3( ACTION_ASSERT_ID , id , value );
}

#endif
