#include "syscall.h"
#include "usergrader1.h"
int main(){
    assertTrueID( 1 , open( TEST_FILE_NAME0 ) > 1 );   
    return 0;
}
