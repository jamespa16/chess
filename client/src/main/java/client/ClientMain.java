package client;

import java.util.Scanner;

import chess.*;

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
                    login(scanner);
                    break;
                case "register":
                    register(scanner);
                    break;
            }
        }
    }
}

/*
    TODO:
    START SCREEN:
    - help ✅
    - quit ✅
    - login 
        calls LOGIN with username & password, then if successful, enters USER SCREEN
    - register 
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