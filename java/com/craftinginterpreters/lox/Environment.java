/*
#   #####################################################################
#   #                                                                   #
#   From Robert Nystrom's 'Crafting Interpreters' Section 8.3 Environment
#   #                                                                   #
#   #####################################################################
*/

package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

// handle scoping
class Environment {

    // track parent scope
    final Environment enclosing;
    // create a hash for mapping identifiers to objects
    private final Map<String, Object> values = new HashMap<>();

    // object for separating nulled variables from uninitialized ones
        // should be visible to callers
    public static final Object UNINITIALIZED = new Object();

    // initial scope creation
    Environment() {
        // no parent scope
        enclosing = null;
    }
    // parent scope exists
    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    // access an object at an enclosing environment a specified distance away from current
    Object getAt(int distance, String name) {
        // pass hashed value from environment as a 'distance' number of parents
        return ancestor(distance).values.get(name);
    }

    // track variable assignment at a specific environment
    void assignAt(int distance, Token name, Object value) {
        // make assignment to nth ancestor with distance argument
        ancestor(distance).values.put(name.lexeme, value);
    }

    // access the value held by an identifier
    Object get(Token name) {
        // check if object in hashmap
        if (values.containsKey(name.lexeme)) {
            // hold return value
            Object val = values.get(name.lexeme);
            // check for unititialized
            if (val == UNINITIALIZED) {
                // generate error
                throw new RuntimeError(name,
                    "Uninitialized variable '" + name.lexeme + "'.");
            }

            // pass bucket contents on initialized var found
            return val;
        }

        // recursive check for variable in parent scopes
        if (enclosing != null) return enclosing.get(name);

        // generate error on access request to undefined var
            // evaluate at runtime to allow variable reference before creation
        throw new RuntimeError(name,
            "Undefined variable '" + name.lexeme + "'.");
    }

    // attempt to assign an existing variable
        // generate error if assignment not possible
    void assign(Token name, Object value) {
        // check for variable exists in scope
        if (values.containsKey(name.lexeme)) {
            // replace existing value at key
            values.put(name.lexeme, value);
            // exit call
            return;
        }

        // check for a parent scope exists
        if (enclosing != null) {
            // recursive check parent for identifier
            enclosing.assign(name, value);
            // exit call on successful return without error throw
            return;
        }

        // generate error on identifier not found
        throw new RuntimeError(name,
            "Undefined variable '" + name.lexeme + "'.");
    }

    // identifier assignment
        // applies to new variables and existing, same effect
    void define(String name, Object value) {
        // new insertion to environment hash
        values.put(name, value);
    }

    // helper method to return an environment as a specified depth of enclosings
    Environment ancestor(int distance) {
        // default state as current environment
        Environment environment = this;

        // iterate for a depth specified in arguments
        for (int i = 0; i < distance; i++) {
            // update correct environment to parent
            environment = environment.enclosing;
        }

        // pass reference to correct environment to caller
        return environment;
    }
}