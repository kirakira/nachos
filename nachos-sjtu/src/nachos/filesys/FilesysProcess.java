package nachos.filesys;

import nachos.machine.Machine;
import nachos.machine.Processor;
import nachos.vm.VMProcess;

/**
 * FilesysProcess is used to handle syscall and exception through some callback methods.
 * 
 * @author starforever
 */
public class FilesysProcess extends VMProcess
{
  protected static final int SYSCALL_MKDIR = 14;
  protected static final int SYSCALL_RMDIR = 15;
  protected static final int SYSCALL_CHDIR = 16;
  protected static final int SYSCALL_GETCWD = 17;
  protected static final int SYSCALL_READDIR = 18;
  protected static final int SYSCALL_STAT = 19;
  protected static final int SYSCALL_LINK = 20;
  protected static final int SYSCALL_SYMLINK = 21;
  
  public int handleSyscall (int syscall, int a0, int a1, int a2, int a3)
  {
    switch (syscall)
    {
      case SYSCALL_MKDIR:
      {
      }
        
      case SYSCALL_RMDIR:
      {
      }
        
      case SYSCALL_CHDIR:
      {
      }
        
      case SYSCALL_GETCWD:
      {
      }
        
      case SYSCALL_READDIR:
      {
      }
        
      case SYSCALL_STAT:
      {
      }
       
      case SYSCALL_LINK:
      {
      }
      
      case SYSCALL_SYMLINK:
      {
      }
      
      default:
        return super.handleSyscall(syscall, a0, a1, a2, a3);
    }
  }
  
  public void handleException (int cause)
  {
    if (cause == Processor.exceptionSyscall)
    {
      //TODO implement this
    }
    else
      super.handleException(cause);
  }
}
