/*
#   ###############################################################################
#   #                                                                             #
#   From Robert Nystrom's 'Crafting Interpreters' Section 7. Evaluating Expressions
#   #                                                                             #
#   ###############################################################################
*/

package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

// allow class to interpret expression and statementtypes
class Interpreter implements Expr.Visitor<Object>,
                             Stmt.Visitor<Void> {

    // create global scope
    final Environment globals = new Environment();
    // hold instance of Environment class for scoping and set to globals
        // interpreter ownership of scoping maintained while interpreter is running
    private Environment environment = globals;

    // define native functions in global space on instance creation
    Interpreter() {
        // returns time since unix epoch in seconds
            // represent as variable that implements a LoxCallable interface
        globals.define("clock", new LoxCallable() {
            // no parameters
            @Override
            public int arity() { return 0; }

            // logic to call function
            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                // send back time on system clock as double
                return (double)System.currentTimeMillis() / 1000.0;
            }

            // on attempt to print
            @Override
            public String toString() { return "<native fn>"; }
        });
    }

    // public api interface - takes statements and applies interpreter's functionality
    void interpret(List<Stmt> statements) {
        // attempt to evaluate given expression
        try {
            // iterate for created statements
            for (Stmt statement : statements) {
                // execute each statement
                execute(statement);
            }
        }
        // anticipate errors from interpreting
        catch (RuntimeError error) {
            // pass error to Lox class to display/handle
            Lox.runtimeError(error);
        }
    }

    // apply to Literal expression, tree-node leafs
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        // send runtime value as object
        return expr.value;
    }

    // evaluation of logical and/or operators
    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        // hold left-side rule evaluation in parsed instance
        Object left = evaluate(expr.left);

        // check parsed type for 'or' operator
        if (expr.operator.type == TokenType.OR) {
            // check for implicit truthful left-side evaluation and short-circuit right-side evaluation
            if (isTruthy(left)) return left;
        }
        // interpret as 'and' operator
        else {
            // evaluate left-side expression and short-circuit right-side if condition false
            if (!isTruthy(left)) return left;
        }

        // evaluate right-side expression if not short-circuited
        return evaluate(expr.right);
    }

    // recognize unary expressions
    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        // generic hold value after unary operator
        Object right = evaluate(expr.right);    // value evaluated before operator

        // compare type of value held in generic object
        switch (expr.operator.type) {           // operator evaluated after value, post-order
            
            case BANG:
                // implicit compare operator
                return !isTruthy(right);

            case MINUS:
                // check for valid input
                checkNumberOperand(expr.operator, right);
                // recognize as number, casting allows dynamic typing
                return -(double)right;
        }

        // invalid, unreachable
        return null;
    }

    // check for variable reference
    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        // pass hashed value
        return environment.get(expr.name);
    }

    // checks for value matches number type
    private void checkNumberOperand(Token operator, Object operand) {
        // check for valid type and escape
        if (operand instanceof Double) return;

        // create error in reference to passed operator
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    // compare values in binary operation for valid type
    private void checkNumberOperands(Token operator,
                                    Object left, Object right) {

    // check for both as numbers and exit call
    if (left instanceof Double && right instanceof Double) return;

    // generate error if not both numbers in reference of generating operator
    throw new RuntimeError(operator, "Operands must be numbers");
}

    // test entity/value association with true
    private boolean isTruthy(Object object) {
        // check for absence
        if (object == null) return false;
        // check if object as a boolean direct
        if (object instanceof Boolean) return (Boolean)object;
        // associate any presence with truth
        return true;
    }

    // generic equality-test that accepts two different types
    private boolean isEqual(Object a, Object b) {
        // check for both null
        if (a == null && b == null) return true;
        // check for a as null
        if (a == null) return false;
        // apply equality check after edge-case testing
        return a.equals(b);
    }

    // format a given object as a string to pass to caller
    private String stringify(Object object) {
        // check for nil type
        if (object == null) return "nil";

        // check for number type
        if (object instanceof Double) {
            // cast as a string
            String text = object.toString();
            // check for redundant float display
            if (text.endsWith(".0")) {
                // ignore trailing float format
                text = text.substring(0, text.length() - 2);
            }
            // pass generated string to caller
            return text;
        }
        // direct conversion without issues
        return object.toString();
    }

    // recognize grouping symbols
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        // recursively evaluate subexpression
        return evaluate(expr.expression);
    }

    // calls to Visitor and gets back Visitor's implementation as an object
    private Object evaluate(Expr expr) {
        // get back expression's visitor implementation
        return expr.accept(this);
    }

    // execution
    private void execute(Stmt stmt) {
        // call statement execution
        stmt.accept(this);
    }

    // evaluation of a block of statements
    void executeBlock(List<Stmt> statements,
                      Environment environment) {
    
        // get enclosing scope
        Environment previous = this.environment;
        // check for 

        try {
            // update current scope to that given in call for variable lifetime evaluation
            this.environment = environment;

            // execute all statements in block
            for (Stmt statement : statements) {
                execute(statement);
            }
        }
        // collapse scope after block evaluation
        finally {
            this.environment = previous;
        }
    
    }

    // interpret block statements
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        // evaluate block with new local scope to stack
        executeBlock(stmt.statements, new Environment(environment));
        // no value returned
        return null;
    }

    // interpret expression statements
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        // generic evaluation call to inner expression
        evaluate(stmt.expression);
        // statements produce no values
        return null;
    }

    // interpret function
    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        // interpret statement as a function
        LoxFunction function = new LoxFunction(stmt);
        // add to scope with instance as value
        environment.define(stmt.name.lexeme, function);
        // statements produce no values
        return null;
    }

    // interpret encountered conditional statement
    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        // check for implicit evaluation of 'if' branch
        if (isTruthy(evaluate(stmt.condition))) {
            // pursue corresponding branch
            execute(stmt.thenBranch);
        }
        // check for an 'else' branch specified
        else if (stmt.elseBranch != null) {
            // pursue 'else' branch before joining similar code
            execute(stmt.elseBranch);
        }

        // statements produce no values
        return null;
    }

    // interpret print as a statement
    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        // hold internal expression to print
        Object printVal = evaluate(stmt.expression);
        // log expression to monitor
        System.out.println(stringify(printVal));
        // no value produced
        return null;
    }

    // interpret variable declarations for AST requirements
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        // buffer populated on variable initialization
        Object initVal = environment.UNINITIALIZED;
        // check for initialization made
        if (stmt.initializer != null) {
            // get value
            initVal = evaluate(stmt.initializer);
        }

        // track new value in hash
        environment.define(stmt.name.lexeme, initVal);
        // no value produced
        return null;
    }

    // interpret While statement in the AST
    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        // iterate while instance's condition is true
        while (isTruthy(evaluate(stmt.condition))) {    // per-loop evaluation, slow
            // act on body of code
            execute(stmt.body);
        }
        // no value produced
        return null;
    }

    // interpret assignment
    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        // get result of assignment evaluation attempt
        Object value = evaluate(expr.value);
        // apply evaluated value to map
        environment.assign(expr.name, value);
        // pass evaluation to caller
        return value;
    }

    // evaluate binary operations left-to-right
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        // hold literals involved
        Object left = evaluate(expr.left);  // visitBinaryExpr
        Object right = evaluate(expr.right);

        // evaluate operator after literals (parent after children)
        switch (expr.operator.type) {

            // comparison operators that apply to numbers only
            case GREATER:
                // check for valid input
                checkNumberOperands(expr.operator, left, right);
                // apply
                return (double)left > (double)right;
            case GREATER_EQUAL:
                // check for valid input
                checkNumberOperands(expr.operator, left, right);
                // apply
                return (double)left >= (double)right;
            case LESS:
                // check for valid input
                checkNumberOperands(expr.operator, left, right);
                // apply
                return (double)left < (double)right;
            case LESS_EQUAL:
                // check for valid input
                checkNumberOperands(expr.operator, left, right);
                // apply
                return (double)left <= (double)right;
            
            // equality operators - allow comparison between different types
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);

            // subtraction applies to numbers only
            case MINUS:
                // check for valid input
                checkNumberOperands(expr.operator, left, right);
                // evaluate literals as numbers
                return (double)left - (double)right;

            // addition applies to numbers and strings
            case PLUS:
                // check for valid case as both numbers
                if (left instanceof Double && right instanceof Double) {
                    // allow operation
                    return (double)left + (double)right;
                }
                // check for valid case as both strings
                if (left instanceof String && right instanceof String) {
                    // allow operation as concatenation
                    return (String)left + (String)right;
                }

                // generate error on invalid input in reference to operator - collapses stack
                throw new RuntimeError(expr.operator,
                    "Operands must be two numbers or two strings.");

            // division applies to numbers
            case SLASH:
                // check for valid input
                checkNumberOperands(expr.operator, left, right);
                // evaluate/case numbers
                return (double)left / (double)right;

            // multiplication applies to numbers
            case STAR:
                // check for valid input
                checkNumberOperands(expr.operator, left, right);
                // numbers cast as doubles
                return (double)left * (double)right;
        }

        // default to failure
        return null;
        
    }

    // node has call expression attached
    @Override
    public Object visitCallExpr(Expr.Call expr) {
        // evaluate expression to be called
        Object callee = evaluate(expr.callee);

        // buffer for arguments associated with node
        List<Object> arguments = new ArrayList<>();
        // iterate for associate arguments
        for (Expr argument : expr.arguments) {
            // evaluate expression and copy value to local buffer
            arguments.add(evaluate(argument));
        }

        // check if called expression has is not allowed as a callable
        if (!(callee instanceof LoxCallable)) {
            // generate error
            throw new RuntimeError(expr.paren,
                "Can only call functions and classes.");
        }

        // translate evaluated callee expression to a callable object
        LoxCallable function = (LoxCallable)callee;

        // check for incorrect amount of arguments given
        if (arguments.size() != function.arity()) {
            // do not allow execution
            throw new RuntimeError(expr.paren, "Expected " +
                function.arity() + " arguments but got " +
                arguments.size() + ".");
        }

        // pass result of call
        return function.call(this, arguments);
    }
}