package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.interpreter.Interpreter;
import com.craftinginterpreters.lox.parser.Parser;
import com.craftinginterpreters.lox.parser.Scanner;
import com.craftinginterpreters.lox.types.Token;
import com.craftinginterpreters.lox.types.TokenType;
import com.craftinginterpreters.lox.resolver.Resolver;
import com.craftinginterpreters.lox.types.Stmt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox
{
    private static final Interpreter interpretor = new Interpreter();

    public static boolean hadError = false;
    public static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException
    {
        if (args.length > 1)
        {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1)
        {
            runFile(args[0]);
        } else
        {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException
    {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException
    {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true)
        {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source)
    {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hadError) return;

        Resolver resolver = new Resolver(interpretor);
        resolver.resolve(statements);

        if (hadError) return;

        interpretor.interpret(statements);
    }

    public static void error(int line, String message)
    {
        report(line, "", message);
    }

    public static void error(Token token, String message)
    {
        if (token.type == TokenType.EOF)
        {
            report(token.line, " at end", message);
        }
        else
        {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    public static void runtimeError(RuntimeError error)
    {
        System.err.println(error.getMessage() + " [line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message)
    {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
