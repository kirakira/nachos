#include "vmgrader.h"
//
// Cheng Yu
//
char* argv[3];

int main() {
   int status;
   argv[0] = "nachos";
   argv[1] = "testing";
   argv[2] =  "echo.";
   join(exec("echo.coff", 3, argv), &status);
   if(status != 0) {
       printf("join failed.\n");
       fail();
   }
   done();
   return 0;
}
