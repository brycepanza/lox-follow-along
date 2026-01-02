/*
#   ###############################################################################
#   #                                                                             #
#   From Robert Nystrom's 'Crafting Interpreters' Section 11. Resolving and Binding
#   #                                                                             #
#   ###############################################################################
*/

package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

// variable resolution, separate pass
    // must visit all nodes in AST, access to Expr and Stmt nodes
class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    
    // reference an interpreter
    private final Interpreter interpreter;

    // require interpreter given at instance creation
    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    // constants for types of function-evaluation states
    private enum FunctionType {
        NONE,           // scope not currently resolving function
        FUNCTION,       // around function
        INITIALIZER,    // class initializer
        METHOD          // standard class method
    }

    // constants for class states
    private enum ClassType {
        NONE,
        CLASS
    }

    // track resolved scopes using stack structure
        // bool tracks use-ready state for variables at scope
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    // track resolve state in reference to function scopes
    private FunctionType currentFunction = FunctionType.NONE;

    // track state of class access for methods being resolved
    private ClassType currentClass = ClassType.NONE;

    // iteratively resolve all grouped statements in a buffer
    void resolve(List<Stmt> statements) {
        // iterate for statements given
        for (Stmt statement : statements) {
            // resolve current individual statement
            resolve(statement);
        }
    }

    // overload method for single statement variable resolving
    private void resolve(Stmt stmt) {
        // single-pass evaluate with passed structure
        stmt.accept(this);
    }

    // overload method for expression resolving
    private void resolve(Expr expr) {
        // single-pass evaluate resolution
        expr.accept(this);
    }

    // resolve all variables function should have access to, bind to function identifier's scope
        // function parameters and local variables
    private void resolveFunction(Stmt.Function function, FunctionType type) {

        // use instance's state variable to check if enclosed by a function and set local state variable
        FunctionType enclosingFunction = currentFunction;
        // update instance's state variable to pursue given function's scope
        currentFunction = type;

        // create new scope for function
        beginScope();

        // iterate for parameters associated with function statement
        for (Token param : function.params) {
            // declare parameter for function's scope
            declare(param);
            // set for ready-to-use in same scope
            define(param);
        }

        // associate local variables with current function scope only
        resolve(function.body);

        // close scope after body resolution
        endScope();

        // revert state variable to enclosing state after function's scope collapse
        currentFunction = enclosingFunction;
    }

    // called to open new scope for variable binding
    private void beginScope() {
        // insert new scope instance for resolution
        scopes.push(new HashMap<String, Boolean>());
    }

    // discard most recently fully-resolved scope
    private void endScope() {
        // remove from structure
        scopes.pop();
    }

    // insert a variable declaration to inner-most scope as not-yet-ready for use
    private void declare(Token name) {
        // check for invalid insertion
        if (scopes.isEmpty()) return;

        // reference current scope
        Map<String, Boolean> scope = scopes.peek();

        // check if declaration already made in current scope
        if (scope.containsKey(name.lexeme)) {
            // interpret action as error
            Lox.error(name,
                "Already a variable with this name in this scope.");
        }
        
        // insert new value as not ready for use
        scope.put(name.lexeme, false);
    }

    // sets a given variable in a scope to be in a use-ready state
        // separation from declaration allows definitions to be contained to scopes aside from declaration
    private void define(Token name) {
        // check for invalid scope state
        if (scopes.isEmpty()) return;

        // set token to be ready for use in the current scope only
        scopes.peek().put(name.lexeme, true);
    }

    // resolve all variables in an expression for a given scope
        // should only make modifications to current scope
    private void resolveLocal(Expr expr, Token name) {
        // iterate reverse-order for scopes in program
            // most recent to global scope
        for (int i = scopes.size() - 1; i >= 0; i--) {

            // check for variable exists in scope
            if (scopes.get(i).containsKey(name.lexeme)) {

                // resolve for inner-most scope ONLY
                interpreter.resolve(expr, scopes.size() - 1 - i);
                // exit call, don't bleed into earlier scopes
                return;
            }
        }

    }

    // anticipate block statement for variable binding
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        // create new scope for block
        beginScope();
        // resolve all variables in block
        resolve(stmt.statements);
        // close binding to scope
        endScope();

        // no value produced
        return null;
    }

    // anticipate class statement
    @Override
    public Void visitClassStmt(Stmt.Class stmt) {

        // remember state for resolution on class statement encountered
        ClassType enclosingClass = currentClass;
        // set resolver state variable to understand current class resolution
        currentClass = ClassType.CLASS;

        // declare in current scope
        declare(stmt.name);
        // define in same scope
        define(stmt.name);

        // check for class inherit from self
        if (stmt.superclass != null &&
            stmt.name.lexeme.equals(stmt.superclass.name.lexeme)) {
                // raise error before runtime
                Lox.error(stmt.superclass.name,
                    "A class can't inherit from itself.");
        }

        // check for valid class inheritance
        if (stmt.superclass != null) {
            // resolve superclass as variable expression
            resolve(stmt.superclass);
        }

        // create new scope for class
        beginScope();
        // manually insert "this" as a recognized identifier and make accessible immediately
        scopes.peek().put("this", true);

        // iterate for methods found by parser
        for (Stmt.Function method : stmt.methods) {
            // set type as method
            FunctionType declaration = FunctionType.METHOD;

            // check for constructor keyword
            if (method.name.lexeme.equals("init")) {
                // associate method as a constructor
                declaration = FunctionType.INITIALIZER;
            }

            // resolve function binding as a method
            resolveFunction(method, declaration);
        }

        // discard surrounding scope
        endScope();

        // revert Resolver state variable
        currentClass = enclosingClass;

        // no value produced
        return null;
    }

    // anticipate expression statements to contain variable references
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        // resolve entire expression
        resolve(stmt.expression);

        // no value produced by statement
        return null;
    }

    // anticipate function binding
    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        // declare function identifier to current scope <- allows proper nested function scope behavior
        declare(stmt.name);
        // define function initializer for same declared scope
        define(stmt.name);

        // bind parameters to function scope and specify as a function
        resolveFunction(stmt, FunctionType.FUNCTION);

        // no value produced
        return null;
    }

    // anticipate conditionals, must resolve all branches before runtime
    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        // resolve expression for condition
        resolve(stmt.condition);

        // resolve branch statement
        resolve(stmt.thenBranch);

        // resolve fallback statement if given, pursue all branches 
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);

        // no value produced
        return null;
    }

    // anticipate variables in print statements
    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        // resolve variables in expression to display
        resolve(stmt.expression);

        // no value produced
        return null;
    }

    // anticipate variable reference in return statements
    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {

        // check if current scope is not inside a function
        if (currentFunction == FunctionType.NONE) {
            // invalid 'return' usage
            Lox.error(stmt.keyword, "Can't return from tope-level code.");
        }

        // check for not void
        if (stmt.value != null) {
            // check for constructor
            if (currentFunction == FunctionType.INITIALIZER) {
                // disallow statements from constructors
                Lox.error(stmt.keyword,
                    "Can't return a value from an initializer.");
            }
            
            // resolve any variables in return's exprsesion
            resolve(stmt.value);
        }

        // no value produced
        return null;
    }

    // anticipate variable declaration for new variable binding
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        // add variable to inner-most scope, variable state not ready for use yet
        declare(stmt.name);

        // check if initialized
        if (stmt.initializer != null) {
            // resolve variable for value binding at current scope
            resolve(stmt.initializer);
        }

        // set ready for use after evaluating initializer
        define(stmt.name);

        // no value produced
        return null;
    }

    // anticipate variable use in 'while' statement found
    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        // resolve variables used in conditional statement
        resolve(stmt.condition);
        // resolve variables used in loop body
        resolve(stmt.body);

        // no value produced
        return null;
    }

    // anticipate resolve action for variable assignment expressions
    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        // resolve expression to handle all references to other variables in assignment
        resolve(expr.value);
        // resolve value to most appropriate scope
        resolveLocal(expr, expr.name);

        // no value produced
        return null;
    }

    // handle binary expressions
    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        // handle by expression pieces
        resolve(expr.left);
        resolve(expr.right);

        // no value produced in resolution
        return null;
    }

    // handle variables used in call expressions
    @Override
    public Void visitCallExpr(Expr.Call expr) {
        // check calling expression
        resolve(expr.callee);

        // resolve all variables used in argument expressions
        for (Expr argument : expr.arguments) {
            resolve(argument);
        }

        // no value produced
        return null;
    }

    // class property getter
    @Override
    public Void visitGetExpr(Expr.Get expr) {
        // resolve expression with provided instance
        resolve(expr.object);

        // no value
        return null;
    }

    // handle grouped expressions with variables
    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);

        // no val produced
        return null;
    }

    // variable resolution for literals (primitive)
    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        // no variables allowed, exit immediately
        return null;
    }

    // handle variable resolution for entire conditional expression
    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        // evaluate parts
        resolve(expr.left);
        resolve(expr.right);

        // no value produced
        return null;
    }

    // handle class property setter expressions
    @Override
    public Void visitSetExpr(Expr.Set expr) {
        // recurse on subexpressions
        resolve(expr.value);
        resolve(expr.object);

        // no value created
        return null;
    }

    // "this" resolution for instance reference
    @Override
    public Void visitThisExpr(Expr.This expr) {

        // check for state not in class
        if (currentClass == ClassType.NONE) {
            // raise error for invalid state
            Lox.error(expr.keyword,
                "Can't use 'this' outside of a class.");
            // exit call
            return null;
        }

        // resolve instance in local scopes only
        resolveLocal(expr, expr.keyword);

        // exit
        return null;
    }

    // variable resolution for unary expressions
    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        // variables may only be contained in one field
        resolve(expr.right);

        // no value produced
        return null;
    }

    // anticipate resolving expressions with variables
    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        // check for variable referenced during own initialization, ex var a = a;
        if (!scopes.isEmpty() &&
            scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {

            // raise error to prevent behavior
            Lox.error(expr.name,
                "Can't read local variable in its own initializer");
        }

        // resolve variables in expression for a given scope
        resolveLocal(expr, expr.name);

        // no value produced
        return null;
    }
}