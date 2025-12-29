/*
#   ###############################################################################
#   #                                                                             #
#   From Robert Nystrom's 'Crafting Interpreters' Section 7. Evaluating Expressions
#   #                                                                             #
#   ###############################################################################
*/

package com.craftinginterpreters.lox;

import java.util.List;

// allow class to interpret expression and statementtypes
class Interpreter implements Expr.Visitor<Object>,
                             Stmt.Visitor<Void> {

    // hold instance of Environment class for scoping,
        // interpreter ownership of scoping maintained while interpreter is running
    private Environment environment = new Environment();

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

    // interpret expression statements
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        // generic evaluation call to inner expression
        evaluate(stmt.expression);
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
        Object initVal = null;
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
}