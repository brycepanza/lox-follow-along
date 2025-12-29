/*
#   ###############################################################################
#   #                                                                             #
#   From Robert Nystrom's 'Crafting Interpreters' Section 7. Evaluating Expressions
#   #                                                                             #
#   ###############################################################################
*/

package com.craftinginterpreters.lox;

// allow class to interpret expression types
class Interpreter implements Expr.Visitor<Object> {

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
                // recognize as number, casting allows dynamic typing
                return -(double)right;
        }

        // invalid, unreachable
        return null;
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
                return (double)left > (double)right;
            case GREATER_EQUAL:
                return (double)left >= (double)right;
            case LESS:
                return (double)left < (double)right;
            case LESS_EQUAL:
                return (double)left <= (double)right;
            
            // equality operators - allow comparison between different types
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);

            // subtraction applies to numbers only
            case MINUS:
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

                // escape on invalid operation
                break;

            // division applies to numbers
            case SLASH:
                // evaluate/case numbers
                return (double)left / (double)right;

            // multiplication applies to numbers
            case STAR:
                // numbers cast as doubles
                return (double)left * (double)right;
        }

        // default to failure
        return null;
        
    }
}