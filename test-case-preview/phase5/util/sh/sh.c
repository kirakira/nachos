#include "stdio.h"
#include "stdlib.h"

#define BUFFERSIZE	64

#define MAXARGSIZE	16
#define MAXARGS		16

/**
 * tokenizeCommand
 *
 * Splits the specified command line into tokens, creating a token array with a maximum
 * of maxTokens entries, using storage to hold the tokens. The storage array should be as
 * long as the command line.
 *
 * Whitespace (spaces, tabs, newlines) separate tokens, unless
 * enclosed in double quotes. Any character can be quoted by preceeding
 * it with a backslash. Quotes must be terminated.
 *
 * Returns the number of tokens, or -1 on error.
 */
static int tokenizeCommand(char* command, int maxTokens, char *tokens[], char* storage) {
    const int quotingCharacter = 0x00000001;
    const int quotingString = 0x00000002;
    const int startedArg = 0x00000004;

    int state = 0;    
    int numTokens = 0;

    char c;

    assert(maxTokens > 0);

    while ((c = *(command++)) != '\0') {
	if (state & quotingCharacter) {
	    switch (c) {
	    case 't':
		c = '\t';
		break;
	    case 'n':
		c = '\n';
		break;
	    }
	    *(storage++) = c;
	    state &= ~quotingCharacter;
	}
	else if (state & quotingString) {
	    switch (c) {
	    case '\\':
		state |= quotingCharacter;
		break;
	    case '"':
		state &= ~quotingString;
		break;
	    default:
		*(storage++) = c;
		break;
	    }
	}
	else {
	    switch (c) {
	    case ' ':
	    case '\t':
	    case '\n':
		if (state & startedArg) {
		    *(storage++) = '\0';
		    state &= ~startedArg;
		}
		break;
	    default:
		if (!(state & startedArg)) {
		    if (numTokens == maxTokens) {
			return -1;
		    }
		    tokens[numTokens++] = storage;
		    state |= startedArg;
		}

		switch (c) {
		case '\\':
		    state |= quotingCharacter;
		    break;
		case '"':
		    state |= quotingString;
		    break;
		default:
		    *(storage++) = c;
		    break;
		}
	    }
	}
    }

    if (state & quotingCharacter) {
	printf("Unmatched \\.\n");
	return -1;
    }

    if (state & quotingString) {
	printf("Unmatched \".\n");
	return -1;
    }

    if (state & startedArg) {
	*(storage++) = '\0';
    }

    return numTokens;
}


void extend(char* s){
    int i;
    char temp[256] = "";
    for (i = 0; i + strlen(s) < 15; i++)
        strcat(temp, " ");
    strcat(temp, s);
    strcpy(s, temp);
}

void getFileType(char* str, int type){
    if (type == NormalFileType)
        strcpy(str, "         Normal");
    else if (type == DirFileType)
        strcpy(str, "      Directory");
    else if (type == LinkFileType)
        strcpy(str, "           Link");
    else
        strcpy(str, "        Unknown");
}
    
char lsbuf[100][100];
char buf[1024*1024*2];

void ls(char* pathname){
    int size = readdir(pathname, lsbuf, 100, 100);
    if (size < 0){
        printf("readdir failed\n");
        return;
    }
    char temp[100];
    char fileType[15];
    FileStat statbuf;
    int i;
    printf("%s\n", "           name           size           blocks         inode          links          type");
    for (i = 0;  i < size; i++){
        strcpy(temp, pathname);
        if (strcmp(temp, "/") != 0)
            strcat(temp, "/");
        strcat(temp, lsbuf[i]);
        if (stat(temp, &statbuf) == 0){
            extend(statbuf.name);
            getFileType(fileType, statbuf.type);
            printf("%s%15d%15d%15d%15d%s\n",  statbuf.name, statbuf.size, statbuf.sectors,
                   statbuf.inode, statbuf.links, fileType);
        } else printf("stat failed\n");
    }
}


void runline(char* line) {
    int pid, background, status;
   
    char args[BUFFERSIZE], prog[BUFFERSIZE];
    char *argv[MAXARGS];

    int argc = tokenizeCommand(line, MAXARGS, argv, args);
    if (argc <= 0)
	return;

    if (argc > 0 && strcmp(argv[argc-1], "&") == 0) {
	argc--;
	background = 1;
    }
    else {
	background = 0;
    }

    if (argc > 0) {
	if (strcmp(argv[0], "exit")==0) {
	    if (argc == 1) {
		exit(0);
	    }
	    else if (argc == 2) {
		exit(atoi(argv[1]));
	    }
	    else {
		printf("exit: Expression Syntax.\n");
		return;
	    }
	}
	else if (strcmp(argv[0], "halt")==0) {
	    if (argc == 1) {
		halt();
		printf("Not the root process!\n");
	    }
	    else {
		printf("halt: Expression Syntax.\n");
	    }
	    return;
	}
	else if (strcmp(argv[0], "join")==0) {
	    if (argc == 2) {
		pid = atoi(argv[1]);
	    }
	    else {
		printf("join: Expression Syntax.\n");
		return;
	    }
	}
	else if (strcmp(argv[0], "cd")==0 || strcmp(argv[0], "chdir")==0) {
	    if (argc == 2) {
            if (chdir(argv[1]) != 0)
		printf("cd failed\n");
            return;
	    }
	    else {
             char buf[100];
             getcwd(buf, 100);
             printf("%s\n", buf);
             return;
	    }
	}
	else if (strcmp(argv[0], "mkdir")==0 || strcmp(argv[0], "md")==0) {
	    if (argc == 2) {
            if (mkdir(argv[1]) != 0)
		printf("mkdir failed\n");
            return;
	    }
	    else {
		printf("mkdir: Expression Syntax.\n");
		return;
	    }
	}  else if (strcmp(argv[0], "rmdir")==0 || strcmp(argv[0], "rd")==0) {
	    if (argc == 2) {
            if (rmdir(argv[1]) != 0)
                printf("rmdir failed\n");
            return;
	    }
	    else {
		printf("rmdir: Expression Syntax.\n");
		return;
	    }
	} else if (strcmp(argv[0], "dir") == 0 || strcmp(argv[0], "ls")==0){
	    if (argc == 1) {
            ls(".");
            return;
	    }
	    else if (argc == 2){
            ls(argv[1]);
            return;
           }  else {
             printf("ls: Expression Syntax.\n");
		return;
	    }        
    } else if (strcmp(argv[0], "creat") == 0){
        if (argc == 2) {
            if (creat(argv[1]) < 0)
                printf("creat failed\n");
            return;
        }else if (argc == 3){
            int fp = creat(argv[1]);
            if (fp < 0){
                printf("creat failed\n");
                return;
            }
            int count = atoi(argv[2]);
            if (write(fp, buf, count) < 0){
                printf("write failed\n");
                return;
            }
            close(fp);
            return;
        }else {
		printf("creat: Expression Syntax.\n");
		return;
        }
    } else if (strcmp(argv[0], "link") == 0){
        if (argc == 3){
            if (link(argv[1], argv[2]) != 0)
                printf("link failed\n");
            return;
        } else {
            printf("link: Expression Syntax.\n");
            return;
        }
    } else if (strcmp(argv[0], "symlink") == 0){
        if (argc == 3){
            if (symlink(argv[1], argv[2]) != 0)
                printf("symlink failed\n");
            return;
        } else {
            printf("symlink: Expression Syntax.\n");
            return;
        }
    } else if (strcmp(argv[0], "unlink") == 0){
        if (argc == 2){
            if (unlink(argv[1]) != 0)
                printf("unlink failed\n");
            return;
        } else {
            printf("unlink: Expression Syntax.\n");
            return;
        }
    } else {
	    strcpy(prog, argv[0]);
	    strcat(prog, ".coff");

	    pid = exec(prog, argc, argv);
	    if (pid == -1) {
		printf("%s: exec failed.\n", argv[0]);
		return;
	    }
	}

	if (!background) {
	    switch (join(pid, &status)) {
	    case -1:
		printf("join: Invalid process ID.\n");
		break;
	    case 0:
		printf("\n[%d] Unhandled exception\n", pid);
		break;
	    case 1:
		printf("\n[%d] Done (%d)\n", pid, status);
		break;
	    }
	}
	else {
	    printf("\n[%d]\n", pid);
	}
    }
}

int main(int argc, char *argv[]) {
    char prompt[] = "nachos% ";

    char buffer[BUFFERSIZE];

    while (1) {
	printf("%s", prompt);

	readline(buffer, BUFFERSIZE);

	runline(buffer);
    }
}
