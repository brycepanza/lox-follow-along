/*
#   ###################################################################################
#   #                                                                                 #
#   From Robert Nystrom's "Crafting Interpreters" Section 5.2 Implementing Syntax Trees
#   #                                                                                 #
#   ###################################################################################
*/

package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

// automate generation of AST classes
public class GenerateAst {

    public static void main(String[] args) throws IOException {

        // check if incorrect argument count given
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }

        // get name for new directory
        String outputDir = args[0];

        // pass type descriptions to generator
        defineAst(outputDir, "Expr", Arrays.asList(
            "Assign     : Token name, Expr value",
            "Binary     : Expr left, Token operator, Expr right",
            "Call       : Expr callee, Token paren, List<Expr> arguments",
            "Get        : Expr object, Token name",
            "Grouping   : Expr expression",
            "Literal    : Object value",
            "Set        : Expr object, Token name, Expr value",
            "This       : Token keyword",
            "Logical    : Expr left, Token operator, Expr right",
            "Unary      : Token operator, Expr right",
            "Variable   : Token name"
        ));

        // accepted expressions following grammar's rules
        defineAst(outputDir, "Stmt", Arrays.asList(
            "Block      : List<Stmt> statements",
            "Class      : Token name, Expr.Variable superclass, List<Stmt.Function> methods",
            "Expression : Expr expression",
            "Function   : Token name, List<Token> params, List<Stmt> body",
            "If         : Expr condition, Stmt thenBranch," +
                        " Stmt elseBranch",
            "Print      : Expr expression",
            "Return     : Token keyword, Expr value",
            "Var        : Token name, Expr initializer",
            "While      : Expr condition, Stmt body"
        ));
    }

    private static void defineAst(
        String outputDir, String baseName, List<String> types) throws IOException {

        // determine path from parameters
        String path = outputDir + "/" + baseName + ".java";
        // create object for writing to file
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("/*");
        writer.println("#   #############################################");
        writer.println("#   #                                           #");
        writer.println("#   From Robert Nystrom's 'Crafting Interpreters'");
        writer.println("#   #                                           #");
        writer.println("#   #############################################");
        writer.println("*/");

        // write reused boilerplate code
        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        // iterate for given fields in parameters
        for (String type : types) {
            // get subclass name from key-value parameter structure
            String className = type.split(":")[0].trim();
            // get values
            String fields = type.split(":")[1].trim();

            // create a new class based on the given parameters
            defineType(writer, baseName, className, fields);
        }

        // base accept() method - polymorphic, interfaced
        writer.println();
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(
        PrintWriter writer, String baseName, List<String> types) {

        writer.println("    interface Visitor<R> {");

        // iterate for types of classes
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            // write out class to be interfaced
            writer.println("        R visit" + typeName  + baseName + "(" + 
                typeName + " " + baseName.toLowerCase() + ");");
            
        }
        writer.println("    }");
    }

    // writes out the definition for a class
    private static void defineType(
        PrintWriter writer, String baseName,
        String className, String fieldList) {

        writer.println("    static class " + className + " extends "
                        + baseName + " {");
        
        // open constructor scope
        writer.println("        " + className + "(" + fieldList + ") {");

        // hold separate parameters in array
        String[] fields = fieldList.split(", ");

        // iterate for fields in the array
        for (String field : fields) {
            // get value held in the field
            String name = field.split(" ")[1];
            // write constructor method
            writer.println("            this." + name + " = " + name + ";");
        }

        // close constructor
        writer.println("        }");

        // visitor pattern
        writer.println();
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");       // each type should receive the generic accept() method
        writer.println("            return visitor.visit" +
                                        className + baseName + "(this);");  // return/get type-specific accept() method
        writer.println("        }");

        // fields
        writer.println();
        // iterate for fields
        for (String field : fields) {
            // hold fields
            writer.println("        final " + field + ";");
        }

        writer.println("    }");
    }
}