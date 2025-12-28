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
            "Binary     : Exr left, Token operator, Expr right",
            "Grouping   : Expr expression",
            "Literal    : Object value",
            "Unary      : Token operator, Expr right"
        ));
    }

    private static void defineAst(
        String outputDir, String baseName, List<String> types) throws IOException {

        // determine path from parameters
        String path = outputDir + "/" + baseName + ".java";
        // create object for writing to file
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        // write reused boilerplate code
        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        // iterate for given fields in parameters
        for (String type : types) {
            // get subclass name from key-value parameter structure
            String className = type.split(":")[0].trim();
            // get values
            String fields = type.split(":")[1].trim();

            // create a new class based on the given parameters
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
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

        writer.println();

        // iterate for fields
        for (String field : fields) {
            // hold fields
            writer.println("        final " + field + ";");
        }

        writer.println("    }");
    }
}