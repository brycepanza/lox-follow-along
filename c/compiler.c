/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#include <stdio.h>
#include <stdlib.h>

#include "common.h"
#include "compiler.h"
#include "scanner.h"

// check for debug bytecode macro
#ifdef DEBUG_PRINT_CODE
// access logging code
#include "debug.h"
#endif

// structure to hold single-instance parser fields
typedef struct {
    Token current;  // value of token currently held by parser
    Token previous; // value of previous evaluated token
    bool had_error; // state variable for error detected
    bool panic_mode;// state variable to prevent incorrect cascading errors on error ecnountered
} Parser;

// types of precedence from least to greatest
typedef enum {
    PREC_NONE,
    PREC_ASSIGNMENT,    // =
    PREC_OR,            // or
    PREC_AND,           // and
    PREC_EQUALITY,      // == !=
    PREC_COMPARISON,    // < > <= >=
    PREC_TERM,          // + -
    PREC_FACTOR,        // * /
    PREC_UNARY,         // ! -
    PREC_CALL,          // . ()
    PREC_PRIMARY
} Precedence;

// pointers to functions for compile rules as void type with no arguments
    // based entirely off global parser state
typedef void (*ParseFn)();

// parse rules as rows in the parser table
typedef struct {
    ParseFn prefix;         // pointer to function to evaluate prefix operator
    ParseFn infix;          // pointer to function to evaluate infix operator
    Precedence precedence;  // precedence on infix operator
} ParseRule;

// single-instance global parser
Parser parser;
// single-instance global pointer to bytecode chunk being compiled
Chunk *compiling_chunk;

// interface access to chunk being compiled currently
static Chunk *current_chunk() {
    // default single-instance global
    return compiling_chunk;
}

// log error with associated token - interfaced by error() and error_at_current()
static void error_at(Token *token, const char *message) {

    // ignore cascading errors if in panic mode
    if (parser.panic_mode) return;

    // update global parser state
    parser.panic_mode = true;

    // log error location with given token
    fprintf(stderr, "[line %d] Error", token->line);

    // check for locational logging message in line
    if (token->type == TOKEN_EOF) fprintf(stderr, " at end");
    else if (token->type == TOKEN_ERROR) {}
    else fprintf(stderr, " at '%.*s'", token->length, token->start);

    // log passed message
    fprintf(stderr, ": %s\n", message);
    // update global parser state
    parser.had_error = true;
}

// error encountered at last consumed token
static void error(const char *message) {
    // log error with last consumed token
    error_at(&parser.previous, message);
}

// error logging for error at current token
static void error_at_current(const char *message) {
    // logging for current token with given error
    error_at(&parser.current, message);
}

// helper to walk through scanned tokens
static void advance() {
    // stash old token - update parse state
    parser.previous = parser.current;

    // parse loop
    for (;;) {
        // get next token
        parser.current = scan_token();
        // check for valid token and exit loop
        if (parser.current.type != TOKEN_ERROR) break;

        // leave error if conditional failed
        error_at_current(parser.current.start);
    }
}

// utility function for checking for an expected token type
    // generates error if not matching
static void consume(TokenType check_type, const char *error_message) {
    // check for current parser token matches
    if (parser.current.type == check_type) {
        // pass checked token
        advance();
        // exit call
        return;
    }

    // match not made generate error
    error_at_current(error_message);
}

// append a single byte to a bytecode chunk
static void emit_byte(uint8_t new_byte) {
    // call library function to write to global chunk to be compiled
    write_chunk(current_chunk(), new_byte, parser.previous.line);
}

// utility function for two logically associated writes
static void emit_bytes(uint8_t byte1, uint8_t byte2) {
    // append bytes in given order
    emit_byte(byte1);
    emit_byte(byte2);
}

// append a return to current bytecode code
static void emit_return() {
    // write correct code to current bytecode chunk
    emit_byte(OP_RETURN);
}

// helper function to add a constant to bytecode values array and pass to caller
static uint8_t make_constant(Value value) {
    // add to values array associated with current bytecode chunk and check size
    int index = add_constant(current_chunk(), value);

    // check for limit exceeded
    if (index > UINT8_MAX) {
        error("Too many constants in one chunk.");
        return 0;
    }

    // pass cast size to caller
    return (uint8_t)index;
}

// append bytecode for a constant value encountered
    // access constant by index associated with value in bytecode values array
static void emit_constant(Value constant) {
    // append instruction and index of value in values array
    emit_bytes(OP_CONSTANT, make_constant(constant));
}

// utility function for end of compile
static void end_compiler() {
    // cap bytecode with return
    emit_return();
// check for macro for bytecode dump
#ifdef DEBUG_PRINT_CODE
    // check for no error
    if (!parser.had_error) {
        // log
        disassemble_chunk(current_chunk(), "code");
    }
#endif
}

// make prototypes visible for expression parsing
static void expression();
static ParseRule *get_rule(TokenType type);
static void parse_precedence(Precedence precedence);

// parse binary expression (infix operator)
static void binary() {
    // hold passed infix operator
    TokenType operator_type = parser.previous.type;
    // get rule associated with operator
    ParseRule *rule = get_rule(operator_type);
    // parse tokens of higher precedence only
    parse_precedence((Precedence)(rule->precedence + 1));

    // check for bytecode to generate
    switch (operator_type) {
        case TOKEN_PLUS:        emit_byte(OP_ADD); break;
        case TOKEN_MINUS:       emit_byte(OP_SUBTRACT); break;
        case TOKEN_STAR:        emit_byte(OP_MULTIPY); break;
        case TOKEN_SLASH:       emit_byte(OP_DIVIDE); break;
        // unreachable
        default: return;
    }
}

// expression logical evaluation
static void expression() {
    // parse all precedences
    parse_precedence(PREC_ASSIGNMENT);
}

// encountered prefix '(' as grouping expression
static void grouping() {
    // recurse on grouped expression in global parser
    expression();
    // parse required closing parenthesis
    consume(TOKEN_RIGHT_PAREN, "Expect ')' after expression.");
}

static void number() {
    // hold value of passed token
    double value = strtod(parser.previous.start, NULL);
    // append constant as lox number structure
    emit_constant(NUMBER_VAL(value));
}

// parse a unary token in global parser
static void unary() {
    // get type of unary operator passed by parser
    TokenType operator = parser.previous.type;

    // recurse on current token in parser state for current or higher precedence
    parse_precedence(PREC_UNARY);

    // check for type of operator and apply to expression
    switch (operator) {
        // add instruction to make negative at interpretation and exit
        case TOKEN_MINUS: emit_byte(OP_NEGATE); break;
        // unreachable/undefined
        default: return;
    }
}

// all rules targeting tokens of a given type
ParseRule rules[] = {
    // token for mapping       prefix      infix     infix precedence
    [TOKEN_LEFT_PAREN]      = {grouping,    NULL,       PREC_NONE},
    [TOKEN_RIGHT_PAREN]     = {NULL,        NULL,       PREC_NONE},
    [TOKEN_LEFT_BRACE]      = {NULL,        NULL,       PREC_NONE},
    [TOKEN_RIGHT_BRACE]     = {NULL,        NULL,       PREC_NONE},
    [TOKEN_COMMA]           = {NULL,        NULL,       PREC_NONE},
    [TOKEN_DOT]             = {NULL,        NULL,       PREC_NONE},
    [TOKEN_MINUS]           = {unary,       binary,     PREC_TERM},
    [TOKEN_PLUS]            = {NULL,        binary,     PREC_TERM},
    [TOKEN_SEMICOLON]       = {NULL,        NULL,       PREC_NONE},
    [TOKEN_SLASH]           = {NULL,        binary,     PREC_FACTOR},
    [TOKEN_STAR]            = {NULL,        binary,     PREC_FACTOR},
    [TOKEN_BANG]            = {NULL,        NULL,       PREC_NONE},
    [TOKEN_BANG_EQUAL]      = {NULL,        NULL,       PREC_NONE},
    [TOKEN_EQUAL]           = {NULL,        NULL,       PREC_NONE},
    [TOKEN_EQUAL_EQUAL]     = {NULL,        NULL,       PREC_NONE},
    [TOKEN_GREATER]         = {NULL,        NULL,       PREC_NONE},
    [TOKEN_GREATER_EQUAL]   = {NULL,        NULL,       PREC_NONE},
    [TOKEN_LESS]            = {NULL,        NULL,       PREC_NONE},
    [TOKEN_LESS_EQUAL]      = {NULL,        NULL,       PREC_NONE},
    [TOKEN_IDENTIFIER]      = {NULL,        NULL,       PREC_NONE},
    [TOKEN_STRING]          = {NULL,        NULL,       PREC_NONE},
    [TOKEN_NUMBER]          = {number,      NULL,       PREC_NONE},
    [TOKEN_AND]             = {NULL,        NULL,       PREC_NONE},
    [TOKEN_CLASS]           = {NULL,        NULL,       PREC_NONE},
    [TOKEN_ELSE]            = {NULL,        NULL,       PREC_NONE},
    [TOKEN_FALSE]           = {NULL,        NULL,       PREC_NONE},
    [TOKEN_FOR]             = {NULL,        NULL,       PREC_NONE},
    [TOKEN_FUN]             = {NULL,        NULL,       PREC_NONE},
    [TOKEN_IF]              = {NULL,        NULL,       PREC_NONE},
    [TOKEN_NIL]             = {NULL,        NULL,       PREC_NONE},
    [TOKEN_OR]              = {NULL,        NULL,       PREC_NONE},
    [TOKEN_PRINT]           = {NULL,        NULL,       PREC_NONE},
    [TOKEN_RETURN]          = {NULL,        NULL,       PREC_NONE},
    [TOKEN_SUPER]           = {NULL,        NULL,       PREC_NONE},
    [TOKEN_THIS]            = {NULL,        NULL,       PREC_NONE},
    [TOKEN_TRUE]            = {NULL,        NULL,       PREC_NONE},
    [TOKEN_VAR]             = {NULL,        NULL,       PREC_NONE},
    [TOKEN_WHILE]           = {NULL,        NULL,       PREC_NONE},
    [TOKEN_ERROR]           = {NULL,        NULL,       PREC_NONE},
    [TOKEN_EOF]             = {NULL,        NULL,       PREC_NONE},
};

// start at parse token state and parse all expressions at a given precedence or higher
static void parse_precedence(Precedence precedence) {
    // progress global parser
    advance();

    // check for prefix operator using passed token
    ParseFn prefix_rule = get_rule(parser.previous.type)->prefix;

    // check for no rule
    if (!prefix_rule) {
        // exit with error
        error("Expected expression.");
        return;
    }

    // apply parse function associated with rule - default to number
    prefix_rule();

    // iterate while current token is in precedence scope
    while (precedence <= get_rule(parser.current.type)->precedence) {
        // move to next token
        advance();
        // recognize passed token as infix operator and get rule associated with it
        ParseFn infix_rule = get_rule(parser.previous.type)->infix;
        // apply parse function associated with returned rule
        infix_rule();
    }
}

// get rule associated with a token type
static ParseRule *get_rule(TokenType type) {
    // index table of rules and pass address of matching struct to caller
    return &rules[type];
}

// attempts to compile given source code
    // populate Chunk* output parameter
    // return bool for compilation successfully made
bool compile(const char *source_code, Chunk *fill_chunk) {
    // apply default state to global scanner
    init_scanner(source_code);

    // set global compile state
    compiling_chunk = fill_chunk;

    // set default error state
    parser.had_error = false;
    parser.panic_mode = false;
    
    advance();
    // currently handles expressions only
    expression();

    // at end of source code, check for token
    consume(TOKEN_EOF, "Expected end of expressions.\n");

    // end with clean state
    end_compiler();

    // send back status of execution without error
    return !parser.had_error;
}