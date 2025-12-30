/*
#   ###################################################################################
#   #                                                                                 #
#   From Robert Nystrom's 'Crafting Interpreters' Section 6.2 Recursive Decsent Parsing
#   #                                                                                 #
#   ###################################################################################
*/

package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

// access token types direct
import static com.craftinginterpreters.lox.TokenType.*;

class Parser {

    // internal parse-level error structure, persistent execution on instance
    private static class ParseError extends RuntimeException {}

    // constant list of tokens generated from source code
    private final List<Token> tokens;
    // for token traversal
    private int current = 0;

    // assign list at creation
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // library method for class functionality
    List<Stmt> parse() {
        // buffer to hold all statements
        List<Stmt> statements = new ArrayList<>();
        // iterate for tokens
        while (!isAtEnd()) statements.add(declaration());

        // pass all found statements to caller
        return statements;
    }

    // evaluate type of statement expression pass result of execution
    private Stmt statement() {
        // check for conditional branch logic
        if (match(IF)) return ifStatement();
        // check for print statement case
        if (match(PRINT)) return printStatement();
        // check for block case and send new instance of block evaluation
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        // default option/fallthrough case
        return expressionStatement();
    }

    private Stmt ifStatement() {
        // require open parentheses for evaluation
        consume(LEFT_PAREN, "Expect a '(' after 'if'.");
        // hold conditional statement
        Expr condition = expression();  // what happens if no expression? if () ? primary() returns null?
        // enforce close parentheses
        consume(RIGHT_PAREN, "Expect a ')' after 'if' condition.");

        // check for evaluation branch on true
        Stmt thenBranch = statement();
        // default to no 'else' branch, revert to shared branch
        Stmt elseBranch = null;
        
        // check for else branch given
        if (match(ELSE)) {
            // get execution block
            elseBranch = statement();
        }

        // pass statement evaluation to caller
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    // evaluation for a print statement encountered
    private Stmt printStatement() {
        // hold expression to be displayed
        Expr printVal = expression();
        // pass end-of-statement semicolon
        consume(SEMICOLON, "Expect ';' after value.");
        // pass created object to caller
        return new Stmt.Print(printVal);
    }

    // evaluation for variable declaration
    private Stmt varDeclaration() {
        // get identifier used for variable
        Token name = consume(IDENTIFIER, "Expect variable name.");

        // buffer to hold to hold value if initialized
        Expr initializer = null;
        // check for correct initialization syntax
        if (match(EQUAL)) {
            // hold expression value
            initializer = expression();
        }

        // verify proper statement closing syntax
        consume(SEMICOLON, "Expect ';' after variable declaration");
        // pass created statement to caller with nullable initialization
        return new Stmt.Var(name, initializer);
    }

    // evaluation for expression statement
    private Stmt expressionStatement() {
        // hold statement's expression as a returned instance
        Expr expr = expression();
        // check for valid statement end
        consume(SEMICOLON, "Expect ';' after value.");
        // pass created Stmt instance to caller
        return new Stmt.Expression(expr);
    }

    // evaluate a block of statements
    private List<Stmt> block() {
        // buffer to hold all statements in the block
        List<Stmt> statements = new ArrayList<>();

        // iterate until block close or end of input
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            // evaluate statement and add to buffer
            statements.add(declaration());
        }

        // check for closing block symbol and pass
        consume(RIGHT_BRACE, "Expect '}' after block.");
        // pass all statements to caller
        return statements;
    }

    // assignment   -> IDENTIFIER "=" assignment | equality
    private Expr assignment() {
        // get assignment as expressions
        Expr expr = equality();

        // check for current token as assignment operator
        if (match(EQUAL)) {
            // get passed l-value
            Token equals = previous();
            // allow kleene recursive evaluation, right-associative
            Expr value = assignment();

            // check for proper equality evaluation
            if (expr instanceof Expr.Variable) {
                // get name of returned equality evaluation
                Token name = ((Expr.Variable)expr).name;
                // pass assignment to caller
                return new Expr.Assign(name, value);
            }

            // produce error on incorrect request
            error(equals, "Invalid assignment target.");
        }

        // pass evaluated assignment to caller, basis
        return expr;
    }

    // evaluation for declaration statement
    private Stmt declaration() {
        // successful logic
        try {
            // check for keyword used and pass status of declaration attempt
                // declaration -> varDecl
            if (match(VAR)) return varDeclaration();

            // non-declarative statement, pass execution return to caller
                // declaration -> statement
            return statement();
        }
        // anticipate evaluation error
        catch (ParseError error) {
            // state fix
            synchronize();
            // failed parse, exit
            return null;
        }
    }

    // expression   -> equality rule
        // starting rule S -> expression()
    private Expr expression() {
        // apply rule recursively
            // assignment -> equality
        return assignment();
    }

    // equality     -> comparison ( (  "!=" | "==" ) comparison )*
    private Expr equality() {
        // recursive evaluate the leading comparison nonterminal
        Expr expr = comparison();

        // eval kleen star segment
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();    // hold operator segment
            Expr right = comparison();      // hold right side of evaluation
            // append to building left-side evaluation
            expr = new Expr.Binary(expr, operator, right);  // recursive nonterminal rule
        }

        // send back built rule
        return expr;
    }

    // comparison   -> term ( ( ">" | ">=" | "<" | "<=" ) term )*
    private Expr comparison() {
        // enforce term rule
        Expr expr = term();

        // iterate for kleene term - each true must fully match rule apply
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();    // enforce operator rule
            Expr right = term();            // enforce term rule
            // concatenate
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // term     -> factor ( ( "+" | "-") factor )*
    private Expr term() {
        // enforce factor found
        Expr expr = factor();

        // kleene
        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = factor();    // term recurse

            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // factor   -> unary ( ( "/" | "*" ) unary )*
    private Expr factor() {
        // enforce factor found
        Expr expr = unary();

        // kleene
        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();    // term recurse

            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // unary    -> ( "!" | "-" ) unary | primary
    private Expr unary() {
        // check for matching recurse condition
        if (match(BANG, MINUS)) {
            // hold unary term
            Token operator = previous();
            // recursive rule enforce
            Expr right = unary();
            // recursive concatenate
            return new Expr.Unary(operator, right);
        }

        // apply unary -> primary rule
        return primary();
    }

    // primary  -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")"
    private Expr primary() {
        // check for primitives and create Expression Literal
        if (match(FALSE)) return new Expr.Literal(false);   // primary -> "false"
        if (match(TRUE)) return new Expr.Literal(true);     // primary -> "true"
        if (match(NIL)) return new Expr.Literal(null);      // primary -> "nil"

        // check for token as primitive type
        if (match(NUMBER, STRING)) {
            // create literal containing token value
            return new Expr.Literal(previous().literal);
        }

        // check for variable access
        if (match(IDENTIFIER)) {
            // evaluate expression and pass result
            return new Expr.Variable(previous());
        }

        // check for recursive rule
        if (match(LEFT_PAREN)) {
            // recurse on expression, enforce rule structure
            Expr expr = expression();
            // enforce closing parentheses
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            // send grouped expression recursed on
            return new Expr.Grouping(expr);
        }

        // generate error on no matching primary found - incorrect syntax
        throw error(peek(), "Expect expression.");
    }




    // ##### utility methods #####

    // check if a given Token matches any of the specified types
    private boolean match(TokenType... types) {
        // iterate through all given types
        for (TokenType type : types) {
            // check for a match found
            if (check(type)) {
                // consume token
                advance();
                // send success
                return true;
            }
        }

        return false;
    }

    // verifies the next token is as expected and eats it
        // throws error if not
    private Token consume(TokenType type, String message) {
        // check for current token as anticipated
        if (check(type)) return advance();

        // generate error on incorrect token sequence
        throw error(peek(), message);
    }



    // determine if a token is at a given type
    private boolean check(TokenType type) {
        // check for no token
        if (isAtEnd()) return false;
        // compare current token varlue with token given as argument
        return peek().type == type;
    }

    // consume token, returns passed token and adjusts index
    private Token advance() {
        if (!isAtEnd()) current++;    // move to next token if valid
        return previous();          // send passed node to caller
    }

    // check if at end of token reading
    private boolean isAtEnd() {
        return peek().type == EOF;  // check closure node for token reading (from scanning)
    }

    // get value held at current index
    private Token peek() {
        return tokens.get(current);
    }

    // check last node
    private Token previous() {
        return tokens.get(current - 1); // safe at current = 0 ?
    }

    // report errors in parsing phase
    private ParseError error(Token token, String message) {
        // pass error to main entry point
        Lox.error(token, message);
        // send to caller
        return new ParseError();
    }

    // state recovery for panic-mode entry
    private void synchronize() {
        // pass error-generating token
        advance();

        // iterate for tokens
        while (!isAtEnd()) {
            // check for semicolon passed and exit recovery sequence
                // statement end check, incorrect logic for (;;)
            if (previous().type == SEMICOLON) return;

            // check current value
            switch (peek().type) {
                // new keyword found
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return; // end of problematic statement found
            }
        }

        // pass final token
        advance();

    }
}