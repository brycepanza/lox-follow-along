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
    // manage class behavior
class LoxClass implements LoxCallable {
    final String name;

    // class for inheritance, require at instance creation
    final LoxClass superclass;

    // associate methods (as functions) with the class
    final Map<String, LoxFunction> methods;

    // associate with given name as identifier and methods as a map of functions
    LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    // check if self has a given method and return if found, return null otherwise
    LoxFunction findMethod(String name) {
        // check for method name in hash
        if (methods.containsKey(name)) {
            // pass hashed LoxFunction method object to caller
            return methods.get(name);
        }

        // check for inheritance
        if (superclass != null) {
            // check for method exists in superclass
            return superclass.findMethod(name);
        }

        // method not associated with class
        return null;
    }

    // make callable to return new instances
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // create new instance of the class
        LoxInstance instance = new LoxInstance(this);
        
        // define initializer for body of constructor associated with class
        LoxFunction initializer = findMethod("init");

        // check for constructor provided
        if (initializer != null) {
            // bind initializer to created instance and execute immediately
            initializer.bind(instance).call(interpreter, arguments);
        }

        // pass instance to caller
        return instance;
    }

    // required arity getter
    @Override
    public int arity() {
        // check for constructor associated with class
        LoxFunction initializer = findMethod("init");

        // check for no constructor
        if (initializer == null) return 0;

        // require same arguments as constructor for building state
        return initializer.arity();
    }

    // for printing
    @Override
    public String toString() {
        // class identified by name
        return name;
    }
}