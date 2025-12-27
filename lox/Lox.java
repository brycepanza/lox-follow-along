/*
#   #################################################################
#   #                                                               #
#   From Robert Nystrom's "Crafting Interpreters" Section 4. Scanning
#   #                                                               #
#   #################################################################
*/


package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

// main class for program
public class Lox {

    // execution state variable - prevent instruction execution on error
    static boolean hadError = false;
    // ### TODO - track if the last instruction was an error - only report blobs of errors ###

    // main entry point
    public static void main(String[] args) throws IOException {
        // check for improper arguments
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);    // exit status UNIX sysexit.h
        }
        // execute on proper argument count
        else if (args.length == 1) {
            runFile(args[0]);
        }
        else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        // read all source code from path from cmdline
        byte[] readBytes = Files.readAllBytes(Paths.get(path));
        // execute program with bytes converted to string
        run(new String(readBytes, Charset.defaultCharset()));

        // check execute status and provide exit code for error
        if (hadError) System.exit(65);
    }

    private static void runPrompt() throws IOException {
        // allow reading from stdin
        InputStreamReader input = new InputStreamReader(System.in);
        // buffer input read from stdin
        BufferedReader buffReader = new BufferedReader(input);

        // read from input until end-of-file given
        for (;;) {
            System.out.println("> ");
            // hang on user input
            String line = buffReader.readLine();
            // check for end-of-file and exit loop
            if (line == null) break;
            // execute line as parameter
            run(line);

            // reset state to allow for continued scripting session
            hadError = false;
        }
    }

    static private void run(String source) {
        // create new Scanner instance from passed source code
        Scanner scanner = new Scanner(source);
        // invoke scanner on source code to generate tokens
        List<Token> tokens = scanner.scanTokens();

        // iterate for returned tokens
        for (Token token : tokens) {
            System.out.println(token);  // display for debugging
        }
    }

    // basic error reporting
    static void error(int line, String message) {
        report(line, "", message);
    }

    static void report(int line, String where, String message) {
        // display format to stderr
        System.err.println(
            "[line " + line + "] Error" + where + ": " + message);
        // update state
        hadError = true;
    }
}