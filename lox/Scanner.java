/*
#   #############################################################################
#   #                                                                           #
#   From Robert Nystrom's "Crafting Interpreters" Section 4.4 The Scanner Class
#                                                 Sectoin 4.5 Recognizing Lexemes
                                                  Section 4.6 Longer Lexemes
#   #                                                                           #
#   #############################################################################
*/

package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// grab from TokenType direct
import static com.craftinginterpreters.lox.TokenType.*;

class Scanner {
    // hold raw source code
    private final String sourceCode;
    // empty array to hold generated tokens
    private final List<Token> tokens = new ArrayList<>();

    // track valid keywords
    private static final Map<String, TokenType> keywords;

    // populate
    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    // lexeme-scanning variables
    private int start = 0;      // start index of a lexeme (first char scanned)
    private int current = 0;    // current file index
    private int line = 1;       // track line of lexem for location data

    Scanner (String sourceCode) {
        this.sourceCode = sourceCode;
    }

    // method to populate array of tokens
    List<Token> scanTokens() {
        // loop for all lexemes in source code
        while (!isAtEnd()) {
            // moved to next lexeme
            start = current;
            // consume and create token from lexeme (populate data)
            scanToken();
        }

        // track end-of-file reached
        tokens.add(new Token(EOF, "", null, line));
        // send created array of tokens to caller
        return tokens;
    }

    private void scanToken() {
        // get next character
        char c = advance();

        // check for type as existing single-char lexeme
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;

            // check for two-character commands by matching next character
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : EQUAL);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : EQUAL);
                break;

            case '/':
                // check for symbol used as a comment
                if (match('/')) {
                    // pass entire commented line
                    while (peek() != '\n' && !isAtEnd()) advance();
                }
                // default to division
                else {
                    addToken(SLASH);
                }
                break;
            
            // handle meaningless lexemes - no significance to interpreter
            case ' ':
            case '\r':
            case '\t':
                // ignore whitespace
                break;
            
            case '\n':
                // account for line location change
                line++;
                // lexeme holds no significance, no token
                break;

            // check for string literal
            case '"': string(); break;

            // handle unrecognized symbols
            default:
                // check for number literal
                if (isDigit(c)) {
                    number();
                }

                // check for reserved word
                else if (isAlpha(c)) {
                    identifier();
                }

                // unrecognized symbol
                else {
                    // generate error and pass to parent to display
                    Lox.error(line, "Unexpected character.");
                }
                
                break;
        }
    }

    // handle identifiers (keywords, var names, etc)
    private void identifier() {
        // read entire word
        while (isAlphaNumeric(peek())) advance();

        // get current identifier as a string
        String text = sourceCode.substring(start, current);
        // get value from mapped keywords
        TokenType type = keywords.get(text);
        
        // check if no type recognized and recognized as an identifier
        if (type == null) type = IDENTIFIER;

        // append new token without a literal
        addToken(type);
    }

    // handle number-literal encountered
    private void number() {
        
        // increment for consecutive digits
        while (isDigit(peek())) advance();

        // check for decimal
        if (peek() == '.' && isDigit(peekNext())) {
            // consume decimal point
            advance();

            // pass further digits beyond decimal point
            while (isDigit(peek())) advance();
        }

        // add found literal - interpreter only recognizes floating-point numbers, convert type
        addToken(NUMBER, Double.parseDouble(sourceCode.substring(start, current)));
    }

    // handle string-literal found in source code
    private void string() {

        // loop for string contents
        while (peek() != '"' && !isAtEnd()) {
            // check for line number increment
            if (peek() == '\n') line++;
            // move to next character
            advance();
        }

        // check for exit due to unterminated string
        if (isAtEnd()) {
            // generate error and pass to caller
            Lox.error(line, "Unterminated string.");
            return;
        }

        // pass closing quotation mark
        advance();

        // strip quotation marks from value
        String value = sourceCode.substring(start + 1, current - 1);
        // append string-value token
        addToken(STRING, value);
    }



    // check if a given character is the same as the next character in this file
        // conditional advance on successful match made - consume matched pair
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        // check for incorrect match
        if (sourceCode.charAt(current) != expected) return false;

        // successful match consumes character
        current++;
        return true;
    }

    // check the next value past current position
        // do not advance cursor/traversal
    private char peek() {
        // check for end of file
        if (isAtEnd()) return '\0';
        // safe to send next character
        return sourceCode.charAt(current);
    }

    // check for character value after current pointer location
    private char peekNext() {
        // check if check is out of range
        if (current + 1 >= sourceCode.length()) return '\0';

        // provide caller with requested character
        return sourceCode.charAt(current + 1);
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // helper method for scanning progression
    private boolean isAtEnd() {
        return current >= sourceCode.length();
    }

    // progress reading index and return character at new index
    private char advance() {
        return sourceCode.charAt(current++);
    }

    // add a token without a literal
    private void addToken(TokenType type) {
        // call generic function anticipating a literal - provide none
        addToken(type, null);
    }

    // add a token with a literal (may be passed as NULL)
    private void addToken(TokenType type, Object literal) {
        // get lexeme from source code
        String text = sourceCode.substring(start, current);
        // append to running array of tokens
        tokens.add(new Token(type, text, literal, line));
    }
}