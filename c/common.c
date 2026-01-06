/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "common.h"
#include "chunk.h"
#include "debug.h"
#include "vm.h"

static void repl();
static void run_file(const char *path);
static char *read_file(const char *path);

int main(int argc, const char *argv[]) {

    // initialize environment for bytecode execution
    init_vm();

    // check for no filepath and run in cmdline
    if (argc == 1) repl();
    // check for argument given
    else if (argc == 2) run_file(argv[1]);
    // invalid arguments
    else {
        fprintf(stderr, "Usage: clox [path]\n");
        exit(64);   // exit with error
    }

    free_vm();

    return 0;
}

// interactive execution through command line
static void repl() {
    char line_buff[1024];   // input size limited

    for (;;) {
        printf("> ");

        // check for no read - EOF or failure
        if (!fgets(line_buff, sizeof(line_buff), stdin)) {
            printf("\n");
            break;  // exit loop
        }
        // interpret sinlge line
        interpret(line_buff);
    }
}

// helper function for reading from a file
    // return pointer to source code as string
static char *read_file(const char *path) {
    // open file for reading
    FILE *file = fopen(path, "rb");

    // check for failed operation
    if (!file) {
        fprintf(stderr, "Could not open file \"%s\".\n", path);
        exit(74);   // exit with error
    }

    fseek(file, 0L, SEEK_END);      // adjust to EOF
    size_t file_size = ftell(file); // use offset at EOF to get file size
    rewind(file);                   // revert to start of file

    char *buffer = (char *)malloc(file_size + 1);   // allocate proper size
    // check for allocation failure
    if (!buffer) {
        fprintf(stderr, "Not enough memory to read \"%s\".\n", path);
        exit(74);   // exit with error
    }

    // copy over file to buffer
    size_t bytes_read = fread(buffer, sizeof(char), file_size, file);
    // check for invalid read size
    if (bytes_read < file_size) {
        fprintf(stderr, "Error reading file \"%s\".\n", path);
        exit(74);   // exit with error
    }
    buffer[bytes_read] = '\0';  // fix string state

    // release allocated resource
    fclose(file);
    // pass filled buffer to caller
    return buffer;
} 

// takes in a path to a source file and executes
static void run_file(const char *path) {
    // get source code as string
    char *source_code = read_file(path);

    // pass to interpreter
    InterpretResult result = interpret(source_code);
    free(source_code);  // lifetime expired - free source code

    // check for exit conditions and call exit failure status codes
    if (result == INTERPRET_COMPILE_ERROR) exit(65);
    if (result == INTERPRET_RUNTIME_ERROR) exit(70);
}