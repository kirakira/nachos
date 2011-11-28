#include "syscall.h"
#include "usergrader1.h"

#define FILE_LIMIT 16

/* test create open close unlink */
inline void test1(){
    int fd , n;  
    startTestID( 1 );
    n = numOpen();
    fd = creat( TEST_FILE_NAME0 ); 
    assertTrueID( 0 , fileExist( 0 ) );         
    assertTrueID( 1 , n + 1 == numOpen()  );    
    close( fd );
    assertTrueID( 2 , n == numOpen() );         
    unlink( TEST_FILE_NAME0 );
    assertTrueID( 3 , !fileExist( 0 ) );       
}

/* write after close the file */
inline void test2(){
    int fd , n = -1 ;
    startTestID( 2 );
    fd = creat( TEST_FILE_NAME0 ); 
    assertTrueID( 0 , fileExist( 0 ) );
    close( fd );
    assertTrueID( 1  , write( fd , &n , sizeof(int) ) == -1 );
}
/* open exceed the stub file system's limit */
inline void test3(){
    int i , fd[ FILE_LIMIT ] ;
    startTestID( 3 );
    assertTrueID( 0 , fileExist( 0 ) );
    for( i = 0 ; i < FILE_LIMIT ; i ++ )
        fd[i] = open( TEST_FILE_NAME0 );

    assertTrueID( 1 , open( TEST_FILE_NAME0 ) == -1 );
    
     for( i = 0 ; i < FILE_LIMIT ; i ++ )
        if( fd[i] > 0 ) assertTrueID( i + 2 , !close( fd[i] ) );
}

/* opened file get closed after exit normally */
inline void test4(){
    int n , id , status;
    startTestID( 4 );
    n = numOpen();
    id = exec( "grader_user1_child4.coff" , 0 , 0 );
    assertTrueID( 0 , id > 0 );
    assertTrueID( 2 , join( id , &status ) == 1 );
    assertTrueID( 3 , n == numOpen() );
}

/* opened file get closed after being killed */
inline void test5(){
    int n , id , status;
    startTestID( 5 );
    n = numOpen();    
    id = exec( "grader_user1_child5.coff" , 0 , 0 );    
    assertTrueID( 0 , id > 0 );
    assertTrueID( 2 , join( id , &status ) == 0 );
    assertTrueID( 3 , n == numOpen() );
}



int main(){
    test1();
    test2();
    test3();
    test4();
    test5();
    done();
    return 0;
}

