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

static bool is_digit(char c) {
    return c >= '0' && c <= '9';
}

// helper to determine if at end of scanner's string
static bool is_at_end() {
    return *scanner.current == '\0';
}

// helper function to send current character and progress pointer
static char advance() {
    return *scanner.current++;
}

// pass current value in scanner
static char peek() {
    return *scanner.current;
}

// send next value character in scanner state to caller
static char peek_next() {
    if (is_at_end()) return '\0';
    // send by offset from current
    return scanner.current[1];
}

// check if given character is same at scanner current
static bool match(char expected) {
    // invalid check
    if (is_at_end()) return false;
    // incorrect match
    if (*scanner.current != expected) return false;

    // pass matched character
    scanner.current++;
    // match success
    return true;
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

// helper function to pass white space to reach token
static void skip_whitespace() {
    for (;;) {
        char c = peek();
        switch (c) {
            case ' ':
            case '\r':
            case '\t':
                advance();  // pass
                break;
            case '\n':
                // fix state
                scanner.line++;
                advance();  // pass
                break;
            // check for comments
            case '/':
                // check for comment using next character
                if (peek_next() == '/') {
                    // pass line
                    while (peek() != '\n' && !is_at_end()) advance();
                }
                // symbol not for comment
                else return;
                break;
            default:
                // exit on significant character found
                return;
        }
    }
}

// scans a number in current state and sends token
static Token number() {
    // read entire number
    while (is_digit(peek())) advance();

    // check for number as fraction
    if (peek() == '.' && is_digit(peek_next())) {
        // consume decimal point
        advance();
        
        // read fractional part
        while (is_digit(peek())) advance();
    }

    return make_token(TOKEN_NUMBER);
}

// check for valid string structure and return new token
static Token string() {
    // iterate until end of string
    while (peek() != '"' && !is_at_end()) {
        // check for state fix
        if (peek() == '\n') scanner.line++;
        advance();
    }

    // check for exit with invalid state
    if (is_at_end()) return error_token("Unterminated string.");

    // pass enclosing quotation mark
    advance();
    // pass valid token to caller
    return make_token(TOKEN_STRING);
}

// attempt scan next token with scanner's string state
Token scan_token() {

    // ignore whitespace
    skip_whitespace();

    // adjust pointer to correct position
    scanner.start = scanner.current;

    // check for reached end of file
    if (is_at_end()) return make_token(TOKEN_EOF);

    // get current character
    char c = advance();

    // check for number and send new number token to caller
    if (is_digit(c)) return number();

    // check for type
    switch (c) {
        case '(': return make_token(TOKEN_LEFT_PAREN);
        case ')': return make_token(TOKEN_RIGHT_PAREN);
        case '{': return make_token(TOKEN_LEFT_BRACE);
        case '}': return make_token(TOKEN_RIGHT_BRACE);
        case ';': return make_token(TOKEN_SEMICOLON);
        case ',': return make_token(TOKEN_COMMA);
        case '.': return make_token(TOKEN_DOT);
        case '-': return make_token(TOKEN_MINUS);
        case '+': return make_token(TOKEN_PLUS);
        case '/': return make_token(TOKEN_SLASH);
        case '*': return make_token(TOKEN_STAR);
        case '!':
            return make_token(
                match('=') ? TOKEN_BANG_EQUAL : TOKEN_BANG);
        case '=':
            return make_token(
                match('=') ? TOKEN_EQUAL_EQUAL : TOKEN_EQUAL);
        case '<':
            return make_token(
                match('=') ? TOKEN_LESS_EQUAL : TOKEN_LESS);
        case '>':
            return make_token(
                match('=') ? TOKEN_GREATER_EQUAL : TOKEN_GREATER);
        case '"': return string();  // helper function to handle encountered strings
    }

    // assume error
    return error_token("Unexpected character.");
}