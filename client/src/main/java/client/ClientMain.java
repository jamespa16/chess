package client;

import java.util.Scanner;

import chess.*;
import model.AuthData;
import model.GameData;

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
                        userScreen(user, scanner);
                    }
                    break;
                case "register":
                    register(scanner);
                    break;
            }
        }
    }

    private static AuthData login (Scanner scanner) {
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

    private static AuthData register (Scanner scanner) {
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

    private static void userScreen(AuthData user, Scanner scanner) {
        System.out.println("hello " + user.username() + "!");
        var session = true;
        while(session) {
            System.out.printf("WHAT DO YOU WISH ->> ");
            String input = scanner.nextLine();
            switch (input) {
                case "h":
                case "help":
                    System.out.println("You can get this help with 'help'\n" +
                    "- logout with 'logout'" +
                    "- create a new game with 'create'"+
                    "- list games on the server with 'list'"+
                    "- join a game with 'play' and the number from the list"+
                    "- observe a game with 'watch' and the number from the list\n magical.");
                    break;
                case "logout":
                    session = false;
                    server.logout(user);
                    break;
                case "create":
                    System.out.printf("what do you want to call this game? ->>");
                    var name = scanner.nextLine();
                    server.createGame(name);
                    break;
                case "list":
                    var games = server.listGames();
                    for (GameData game : games) {
                        System.out.println("#" + game.gameID() +
                        ": " + game.gameName() +
                        " with white as: " + game.whiteUsername() +
                        " and black as: " + game.blackUsername());
                    }
                    break;
                case "play":
                    System.out.printf("which game? hint: you can get the game ID with 'list' ->>");
                    var id = scanner.nextLine();
                    var game = server.joinGame(user, id);
                    gameScreen(user, game);
                    break;
                case "watch":
                    System.out.printf("which game? hint: you can get the game ID with 'list' ->>");
                    var id = scanner.nextLine();
                    var game = server.joinGame(user, id);
                    gameScreen(user, game, scanner);
                    break;
                }
        }
    }

    private static void gameScreen(AuthData user, GameData game, Scanner scanner) {
        while(true) {
            render(game.game());
            System.out.printf("command ->> ");
            var command = scanner.nextLine();
            if (command == "q" || command == "quit") {
                return;
            }
        }
    }

    private static void render(ChessGame game) {
        var board = game.getBoard();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                System.out.printf("" + board.getPiece(new ChessPosition(i, j)));
            }
            System.out.printf("\n");
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
    - help ✅
        provides a list of all commands
    - logout ✅
        calls LOGOUT, then returns to start screen
    - create game ✅
        takes a new name, then calls CREATE. (does not join)
    - list games ✅
        calls LIST, then gives a list of games. DOES NOT use id numbers.
        [#, name, players]
    - play ✅
        takes game number from list & color, then JOIN and enters CHESSBOARD screen, uses internal numbering instead of server numbers.
    - observe ✅
        takes game number from list & enters CHESSBOARD view 
*/