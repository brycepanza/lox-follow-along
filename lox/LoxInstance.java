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
    private LoxClass loxClass;

    // require instance type specification at creation
    LoxInstance(LoxClass loxClass) {
        this.loxClass = loxClass;
    }

    // associate with class name on stringify
    @Override
    public String toString() {
        return loxClass.name + " instance";
    }
}