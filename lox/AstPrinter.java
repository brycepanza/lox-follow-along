/*
#   ##########################################################################
#   #                                                                        #
#   From Robert Nystrom's 'Crafting Interpreters' Section 5. Representing Code
#   #                                                                        #
#   ##########################################################################
*/

package com.craftinginterpreters.lox;

// create class for displaying tree nodes from allowed classes via interface Visitor in lox/Expr.java
class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    // provide definitions for the interfaced classes

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";   // edge case
        return expr.value.toString();           // general
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    // parenthesize may accept variable number of expression arguments
    private String parenthesize(String name, Expr... exprs) {

        // buffer to append to new string space
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);

        // iterate for expressions given as arugments
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));   // type-specific name
        }

        builder.append(")");

        return builder.toString();
    }

    public static void main(String[] args) {

        Expr expression = new Expr.Binary(
            new Expr.Unary(
                new Token(TokenType.MINUS, "-", null, 1),
                new Expr.Literal(123)),
            new Token(TokenType.STAR, "*", null, 1),
            new Expr.Grouping(
                new Expr.Literal(45.67))
        );

        System.out.println(new AstPrinter().print(expression));
    }
}