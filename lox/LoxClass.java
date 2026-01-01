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

    // associate with given name as identifier
    LoxClass(String name) {
        this.name = name;
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