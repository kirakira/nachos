#include "syscall.h"
#include "stdlib.h"
int main( int argc , char *argv[] ) {
    int i , n , k;
    char fname[ 256 ];
    if( argc != 3 ){
        printf("usage :many <filename> number");
    }
    n = atoi( argv[2] );
    strcpy( fname , argv[1] );
    strcat( fname , ".coff" );
    
    for( i =  0 ; i < n ; i ++ ){
        k = exec( fname , 0 , 0 );
    }
    return 0;
}   
