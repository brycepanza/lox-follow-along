/*
#   ###################################################################
#   #                                                                 #
#   From Robert Nystrom's 'Crafting Interpreters' Section 10. Functions
#   #                                                                 #
#   ###################################################################
*/

package com.craftinginterpreters.lox;

import java.util.List;

// callable entity for runtime access to functions
    // accessed during interpretation, not parsing
class LoxFunction implements LoxCallable {
    // hold parsed function declaration
    private final Stmt.Function declaration;

    // scope of function declaration, allows local functions
        // enforces scope inheritance of declaration, not call location
    private final Environment closure;
    
    // function instance creation concerned with declaration and closure
    LoxFunction(Stmt.Function declaration, Environment closure) {
        this.closure = closure;
        this.declaration = declaration;
    }

    // implement required call trait
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // hold function declaration scope
            // global scope if not nested
        Environment environment = new Environment(closure);

        // iterate over parameters of function call
        for (int i = 0; i < declaration.params.size(); i++) {
            // add argument to scope visible to the function
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        // use provided interpreter to execute the function using its specific scope
        try {
            interpreter.executeBlock(declaration.body, environment);
        }
        // escape on Return RuntimeException generated
        catch (Return returnVal) {
            // exit call immediately and pass value to caller
            return returnVal.value;
        }

        // void, function pass value to caller (null translated to nil)
        return null;
    }

    // implement required check on argument count
    @Override
    public int arity() {
        // pass size of given parameter list
        return declaration.params.size();
    }

    // implement required string conversion
    @Override
    public String toString() {
        // describe with function name
        return "<fn " + declaration.name.lexeme + ">";
    }
}