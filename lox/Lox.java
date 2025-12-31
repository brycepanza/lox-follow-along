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

    // interpreter instance to be executed
    private static final Interpreter interpreter = new Interpreter();

    // execution state variable - prevent instruction execution on error
    static boolean hadError = false;
    // check for errors made during interpreter execution
    static boolean hadRuntimeError = false;
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
        // check for error during interpreter execution
        if (hadRuntimeError) System.exit(70);
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

        Parser parser = new Parser(tokens);
        // attempt get statements
        List<Stmt> statements = parser.parse();

        // check for error and exit call
        if (hadError) return;

        // create Resolver instance for variable binding and provide with reference to interpreter
        Resolver resolver = new Resolver(interpreter);
        // single-pass evaluate variable bindings before interpretation
        resolver.resolve(statements);

        // run interpreter on statements
        interpreter.interpret(statements);
    }

    // basic error reporting (scanning)
    static void error(int line, String message) {
        report(line, "", message);
    }

    // parse-level error handling
    static void error(Token token, String message) {
        // check for incorrect termination
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        }
        // default
        else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    // handle errors recevied at runtime
    static void runtimeError(RuntimeError error) {
        // log formatted message
        System.out.println(error.getMessage() +
            "\n[line " + error.token.line + "]");
        // update state on event
        hadRuntimeError = true;
    }

    static void report(int line, String where, String message) {
        // display format to stderr
        System.err.println(
            "[line " + line + "] Error" + where + ": " + message);
        // update state
        hadError = true;
    }
}