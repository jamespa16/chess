package client;

import java.util.Scanner;

import chess.*;
import model.AuthData;

public class ClientMain {
    public static void main(String[] args) {
        System.out.println("WELCOME TO CHESS");
        var running = true;
        while (running) {
            System.out.printf("WHAT DO YOU WISH ->> ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            switch (input) {
                case "h":
                case "help":
                    System.out.println("You can get this help with 'help'\n" +
                    "- quit with 'quit'" +
                    "- login with 'login'"+
                    "- register with 'register'.\n magical.");
                    break;
                case "q":
                case "quit":
                    running = false;
                    System.out.println("goodbye 👋");
                    break;
                case "login":
                    var user = login(scanner);
                    if (user != null) {
                        userScreen(user);
                    }
                    break;
                case "register":
                    register(scanner);
                    break;
            }
        }
    }

    private AuthData login (Scanner scanner) {
        var attempting = true;
        while (attempting) {
            System.out.printf("username ->> ");
            var user = scanner.nextLine();
            System.out.printf("password ->> ");
            var password = scanner.nextLine();
            var auth = server.login(user, password);
            if (auth != null) {
                return auth;
            } else {
                System.out.printf("authorization failed, try again? [y/n] ->>");
                String tryAgain = scanner.nextLine();
                switch (tryAgain) {
                    case "y":
                    case "yes":
                        break;
                    case "n":
                    case "no":
                        attempting = false;
                }
            }
        }
    }

    private AuthData register (Scanner scanner) {
        var attempting = true;
        while (attempting) {
            System.out.printf("username ->> ");
            var user = scanner.nextLine();
            System.out.printf("password ->> ");
            var password = scanner.nextLine();
            var auth = server.register(user, password);
            if (auth != null) {
                return auth;
            } else {
                System.out.printf("registration failed, try again? [y/n] ->>");
                String tryAgain = scanner.nextLine();
                switch (tryAgain) {
                    case "y":
                    case "yes":
                        break;
                    case "n":
                    case "no":
                        attempting = false;
                }
            }
        }
    }
}

/*
    TODO:
    START SCREEN:
    - help ✅
    - quit ✅
    - login ✅
        calls LOGIN with username & password, then if successful, enters USER SCREEN
    - register ✅
        calls REGISTER, then if successsful, enters USER SCREEN
    USER SCREEN:
    - help 
        provides a list of all commands
    - logout
        calls LOGOUT, then returns to start screen
    - create game
        takes a new name, then calls CREATE. (does not join)
    - list games
        calls LIST, then gives a list of games. DOES NOT use id numbers.
        [#, name, players]
    - play
        takes game number from list & color, then JOIN and enters CHESSBOARD screen, uses internal numbering instead of server numbers.
    - observe
        takes game number from list & enters CHESSBOARD view 
*/