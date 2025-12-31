/*
#   ###################################################################
#   #                                                                 #
#   From Robert Nystrom's 'Crafting Interpreters' Section 10. Functions
#   #                                                                 #
#   ###################################################################
*/

package com.craftinginterpreters.lox;

// class wrapped in RuntimeException class to collapse call stack on lox function return
class Return extends RuntimeException {
    // hold value of source return statement
    final Object value;

    // instance creation concerned with value only
    Return(Object value) {
        // settings used to reference RuntimeException class
        super(null, null, false, false);
        // assign value to be returned
        this.value = value;
    }
}