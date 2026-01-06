/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#include <stdio.h>
#include <string.h>

#include "common.h"
#include "scanner.h"

// structure for scanning source code to tokens
typedef struct {
    const char *start;      // start of each token in source code
    const char *current;    // current pointer in word
    int line;               // corresponding line in source code
} Scanner;

// global access to single-instance scanner
Scanner scanner;

// set default state to scanner
void init_scanner(const char *source_code) {
    scanner.start = source_code;
    scanner.current = source_code;
    scanner.line = 1;
}