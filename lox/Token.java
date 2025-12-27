/*
#   ############################################################################
#   #                                                                          #
#   From Robert Nystrom's "Crafting Interpreters" Section 4.2 Lexemes and Tokens
#   #                                                                          #
#   ############################################################################
*/

package com.craftinginterpreters.lox;

class Token {
    // constants on instance creation
    final TokenType type;   // type recognized by interpreter
    final String lexeme;    // base unit from source code
    final Object literal;
    final int line;         // track location in file

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return " " + lexeme + " " + literal;
    }   
}