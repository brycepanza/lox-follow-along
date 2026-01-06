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

// helper to determine if at end of scanner's string
static bool is_at_end() {
    return *scanner.current == '\0';
}

// helper to create a new token with scanner's current state
Token make_token(TokenType type) {
    Token new_token;
    // type assigned by caller
    new_token.type = type;
    // assign lexeme data by scanner state
    new_token.start = scanner.start;        // start of lexeme
    new_token.length = (int)(scanner.current - scanner.start);
    new_token.line = scanner.line;

    return new_token;
}

// helper to generate an error token
static Token error_token(const char *message) {
    Token token;

    token.type = TOKEN_ERROR;
    // point to given message - outside of scanner
    token.start = message;
    token.length = (int)strlen(message);
    token.line = scanner.line;

    return token;
}

// attempt scan next token with scanner's string state
Token scan_token() {
    // adjust pointer to correct position
    scanner.start = scanner.current;

    // check for reached end of file
    if (is_at_end()) return make_token(TOKEN_EOF);

    // assume error
    return error_token("Unexpected character.");
}