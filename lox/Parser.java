/*
#   ###################################################################################
#   #                                                                                 #
#   From Robert Nystrom's 'Crafting Interpreters' Section 6.2 Recursive Decsent Parsing
#   #                                                                                 #
#   ###################################################################################
*/

package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.Arrays;
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
        // check for 'for' keyword
        if (match(FOR)) return forStatement();
        // check for conditional branch logic
        if (match(IF)) return ifStatement();
        // check for 'while' loop keyword
        if (match(WHILE)) return whileStatement();
        // check for print statement case
        if (match(PRINT)) return printStatement();
        // check for statement as return statement
        if (match(RETURN)) return returnStatement();
        // check for block case and send new instance of block evaluation
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        // default option/fallthrough case
        return expressionStatement();
    }

    // evaluation for 'for' statement
    private Stmt forStatement() {
        // require open parenthesis
        consume(LEFT_PAREN, "Expect a '(' after 'for'.");

        // hold initializer of loop
        Stmt initializer;

        // check for no initiailizer, for (;...;...)
        if (match(SEMICOLON)) {
            // no evaluation
            initializer = null;
        }
        // check for initializer as new variable
        else if (match(VAR)) {
            // evaluate statement
            initializer = varDeclaration();
        }
        // assume existing variable in expression
        else {
            // evaluate as expression without declaration
            initializer = expressionStatement();
        }

        // hold conditional statement, default to none (infinite)
        Expr condition = null;

        // check for some expression given
        if (!check(SEMICOLON)) {
            // assign condition as given expression at current token
            condition = expression();
        }
        // require end of condition statement delimiter
        consume(SEMICOLON, "Expect ';' after loop condition.");

        // hold per-pass iteration variable value modification, default to none
        Expr increment = null;

        // check if some expression given
        if (!check(RIGHT_PAREN)) {
            // assign to expression at current token
            increment = expression();
        }
        // require closign parenthesis
        consume(RIGHT_PAREN, "Expect ')' after 'for' clauses.");

        // evaluate body on successful 'for' clause evaluation
        Stmt body = statement();

        // check if an expression for iteration incrementing was given for the block
        if (increment != null) {
            // reassign the body to include this increment per-pass in the block statement
            body = new Stmt.Block(
                Arrays.asList(
                    body,
                    new Stmt.Expression(increment)));
        }

        // check if no condition is given for loop termination and assign to true
        if (condition == null) condition = new Expr.Literal(true);
        // utilize existing 'while' statement for 'for' loop structure
            // 'for' loops are translated-to and interpreted as 'while' loops
        body = new Stmt.While(condition, body);

        // check if initializer given
        if (initializer != null) {
            // add initialization to block with single pass preceding loop
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        // send evaluation to caller on successful return
        return body;
    }

    // parse of 'if' statement
    private Stmt ifStatement() {
        // require open parenthesis for evaluation
        consume(LEFT_PAREN, "Expect a '(' after 'if'.");
        // hold conditional statement
        Expr condition = expression();  // what happens if no expression? if () ? primary() returns null?
        // enforce close parenthesis
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

    // parse a 'print' statement encountered
    private Stmt printStatement() {
        // hold expression to be displayed
        Expr printVal = expression();
        // pass end-of-statement semicolon
        consume(SEMICOLON, "Expect ';' after value.");
        // pass created object to caller
        return new Stmt.Print(printVal);
    }

    // parse logic for 'return' statement encountered
    private Stmt returnStatement() {
        // hold keyword for 'return''
        Token keyword = previous();
        // default to void return
        Expr value = null;

        // check if value given after 'return' keyword
        if (!check(SEMICOLON)) {
            // reassign return value with branch expansion
            value = expression();
        }

        // require semicolon
        consume(SEMICOLON, "Expect ';' after a 'return' value.");

        // pass created statement node to caller
        return new Stmt.Return(keyword, value);
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

    // parse evaluation after a 'while' keyword found
    private Stmt whileStatement() {
        // require open parenthesis
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        // evaluate expression for loop condition
        Expr condition = expression();
        // require close parenthesis delimiter
        consume(RIGHT_PAREN, "Expect ')' after condition");
        // evaluate loop body
        Stmt body = statement();

        // pass evaluated loop statement to caller
        return new Stmt.While(condition, body);
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

    // evaluate an encountered function declaration
    private Stmt.Function function(String kind) {   // argumen specifies function vs method
        // require identifier
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        // require open parenthesis
        consume(LEFT_PAREN, "Expect '(' after " + kind  + " name.");

        // buffer for parameters (token for lexeme and metadata)
        List<Token> parameters = new ArrayList<>();

        // check for end of parameters
        if (!check(RIGHT_PAREN)) {
            // iterate while parameters found
            do {
                // check if parameter limit exceeded
                if (parameters.size()>= 255) {  // <- should be constant ?
                    // log error
                    error(peek(), "Can't have more than 255 parameters.");
                }

                // add parameter to buffer if identifier given
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }

        // require closing parenthesis
        consume(RIGHT_PAREN, "Expect ')' after parameters.");

        // require a brace to open function block
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        // parse function body as a block
        List<Stmt> body = block();

        // pass parsed function as statement node to caller
        return new Stmt.Function(name, parameters, body);
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

    // assignment   -> IDENTIFIER "=" assignment | logic_or
    private Expr assignment() {
        // get assignment as expressions, check for fallback case
        Expr expr = or();

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

    // logic_or     -> logic_and | ( "or" logic_and )*
    private Expr or() {
        // check fallback case, hold left-side evaluation
        Expr expr = and();

        // check kleene rule for "or" evaluation
        while (match(OR)) {
            // keep operator
            Token operator = previous();
            // hold right-side evaluation
            Expr right = and();
            // update expression for current evaluation state
            expr = new Expr.Logical(expr, operator, right);
        }

        // pass evaluated expression to caller
        return expr;
    }

    // logic_and    -> equality | ( "and" equality )*
    private Expr and() {
        // evaluate left-side expression, fall-back case
        Expr expr = equality();

        // check kleene rule for "and" token in correct evaluation, don't pass precendence line
        while (match(AND)) {
            // hold passed matched operator after left-side evaluation
            Token operator = previous();
            // hold right-side evaluation
            Expr right = equality();
            // update expression evaluation to match current state
            expr = new Expr.Logical(expr, operator, right);
        }

        // pass rule evaluation to caller
        return expr;
    }

    // evaluation for declaration statement
    private Stmt declaration() {
        // successful logic
        try {
            // check for function declaration and pass grammar rule to caller
                // declaration -> funDecl
            if (match(FUN)) return function("function");    // specify kind as function
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

    // unary    -> ( "!" | "-" ) unary | call
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

        // apply unary -> call rule
        return call();
    }

    // helper function to expand arguments and verify call syntax
    private Expr finishCall(Expr callee) {
        // buffer for arguments given with call
        List<Expr> arguments = new ArrayList<>();

        // check for arguments given
        if (!check(RIGHT_PAREN)) {
            // iterate for arguments
            do {
                // apply a limit on arguments allow     <- should be a constant ???
                if (arguments.size() >= 255) {
                    // report limit exceeding
                    error(peek(), "Can't have more than 255 arguments.");
                }

                // expand expression and add as argument
                arguments.add(expression());

            } while (match(COMMA));
        }
        
        // require close parenthesis
        Token paren = consume(RIGHT_PAREN,
                            "Expect a ')' after arguments.");
                    
        // pass evaluated expression to call()
        return new Expr.Call(callee, paren, arguments);
    }

    // call     -> primary ( "(" arguments? ")" )*
    private Expr call() {
        // evaluate callee expression as primary, call -> primary
        Expr expr = primary();

        // infinite loop, allow for currying
        while (true) {
            // check for call made
            if (match(LEFT_PAREN)) {
                // check call
                expr = finishCall(expr);
            }
            // no call, exit kleene loop
            else break;
        }

        // pass expression back up to caller
        return expr;
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
            // enforce closing parenthesis
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
        boolean ok = check(type);

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
            // pass token
            advance();
        }

    }
}