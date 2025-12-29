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
    // create a hash for mapping identifiers to objects
    private final Map<String, Object> values = new HashMap<>();

    // access the value held by an identifier
    Object get(Token name) {
        // check if object in hashmap
        if (values.containsKey(name.lexeme)) {
            // pass bucket contents
            return values.get(name.lexeme);
        }

        // generate error on access request to undefined var
            // evaluate at runtime to allow variable reference before creation
        throw new RuntimeError(name,
            "Undefined variable '" + name.lexeme + "'.");
    }

    // identifier assignment
        // applies to new variables and existing, same effect
    void define(String name, Object value) {
        // new insertion to environment hash
        values.put(name, value);
    }
}