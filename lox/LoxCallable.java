/*
#   ###################################################################
#   #                                                                 #
#   From Robert Nystrom's 'Crafting Interpreters' Section 10. Functions
#   #                                                                 #
#   ###################################################################
*/

package com.craftinginterpreters.lox;

import java.util.List;

// structure for a callable entity in Lox
interface LoxCallable {
    // must implement a method allowing the entity to be called using an interpreter and given arguments
    Object call(Interpreter interpreter, List<Object> arguments);
}