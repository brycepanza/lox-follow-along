/*
#   ############################################################################
#   #                                                                          #
#   From Robert Nystrom's "Crafting Interpreters" Section 4.2 Lexemes and Tokens
#   #                                                                          #
#   ############################################################################
*/

// specify logical grouping
package com.craftinginterpreters.lox;

// types of tokens that can be recognized by the interpreter
enum  TokenType {
    // single-character
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // one- or two- character tokens, mostly logical operators
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // literals - interpreter converts strings to recognized runtime objects
    IDENTIFIER, STRING, NUMBER,

    // keywords
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    EOF
}