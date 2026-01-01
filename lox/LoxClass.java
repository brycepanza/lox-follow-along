/*
#   #################################################################
#   #                                                               #
#   From Robert Nystrom's 'Crafting Interpreters' Section 12. Classes
#   #                                                               #
#   #################################################################
*/

package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

// instance of a class in Lox, callable to create new instances
    // attributes added dynamically
class LoxClass implements LoxCallable {
    final String name;

    // associate methods (as functions) with the class
    final Map<String, LoxFunction> methods;

    // associate with given name as identifier and methods as a map of functions
    LoxClass(String name, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    // check if self has a given method and return if found, return null otherwise
    LoxFunction findMethod(String name) {
        // check for method name in hash
        if (methods.containsKey(name)) {
            // pass hashed LoxFunction method object to caller
            return methods.get(name);
        }

        // method not associated with class
        return null;
    }

    // make callable to return new instances
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // create new instance of the class
        LoxInstance instance = new LoxInstance(this);
        // pass instance to caller
        return instance;
    }

    // required arity getter
    @Override
    public int arity() {
        // no constructor - no arguments
        return 0;
    }

    // for printing
    @Override
    public String toString() {
        // class identified by name
        return name;
    }
}