/*
#   ########################################################################
#   #                                                                      #
#   From Robert Nystrom's 'Crafting Interpreters' Section 7.3 Runtime Errors
#   #                                                                      #
#   ########################################################################
*/

package com.craftinginterpreters.lox;

class RuntimeError extends RuntimeException {
    final Token token;

    // associated with an error-generating token and error message
    RuntimeError(Token token, String message) {
        // inherit message from RuntimeException parent
        super(message);
        // take token as parameter
        this.token = token;
    }
}