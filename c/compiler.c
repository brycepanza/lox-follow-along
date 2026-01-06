/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#include <stdio.h>

#include "common.h"
#include "compiler.h"
#include "scanner.h"

void compile(const char *source_code) {
    // apply default state to global scanner
    init_scanner(source_code);
    int line = 1;

    for (;;) {
        // get current token
        Token token = scan_token();
        // check for next-line found
        if (line != token.line) {
            printf("%4d ", token.line);
            line = token.line;
        }
        // default display
        else printf("\t| ");

        // log token
        printf("%2d '%.*s'\n", token.type, token.length, token.start);

        // check for exit
        if (token.type == TOKEN_EOF) break;
    }
}