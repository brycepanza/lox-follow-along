package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public Class Lox {
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
}