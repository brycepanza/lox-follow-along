/*
#   #################################################################
#   #                                                               #
#   From Robert Nystrom's 'Crafting Interpreters' Section 12. Classes
#   #                                                               #
#   #################################################################
*/

package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

// structure for runtime instances of a class
class LoxInstance {
    // hold instance type
    private LoxClass klass;
    // track properties associated with individual instances
    private final Map<String, Object> fields = new HashMap<>();

    // require instance type specification at creation
    LoxInstance(LoxClass loxClass) {
        this.klass = klass;
    }

    // allow indiscriminate access to properties
    Object get(Token name) {
        // check for property name associated with instance
        if (fields.containsKey(name.lexeme)) {
            // pass value to caller
            return fields.get(name.lexeme);
        }

        // create error if property does not exist
        throw new RuntimeError(name,
            "Undefined property '" + name.lexeme + "'.");
    }

    // associate with class name on stringify
    @Override
    public String toString() {
        return klass.name + " instance";
    }
}