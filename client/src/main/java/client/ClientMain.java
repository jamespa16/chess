package client;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

import chess.ChessGame.TeamColor;
import model.AuthData;
import model.GameData;

public class ClientMain {
    private final static ServerFacade server = new ServerFacade("http://127.0.0.1:8080");
    private final static List<GameData> gameList = new ArrayList<>();
    public static void main(String[] args) {
        System.out.println("chess moment \n + you are logged out. log in with 'login', or 'register'!");
        var running = true;
        while (running) {
            System.out.printf("command >> ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();
            switch (input) {
                case "h":
                case "help":
                    System.out.println("You can get this help with 'help'\n" +
                    "- quit with 'quit'\n" +
                    "- login with 'login'\n"+
                    "- register with 'register'.\n magical.");
                    break;
                case "q":
                case "quit":
                    running = false;
                    System.out.println("goodbye 👋");
                    break;
                case "login":
                    var session = login(scanner);
                    if (session != null) {
                        userScreen(session, scanner);
                    }
                    break;
                case "register":
                    var user = register(scanner);
                    if (user != null) {
                        userScreen(user, scanner);
                    }
                    break;
                case "burn":
                    serverRequestHandler(() -> {server.deleteDB(); return null;});
                    gameList.clear();
            }
        }
    }

    private static AuthData login (Scanner scanner) {
        var attempting = true;
        while (attempting) {
            System.out.printf("username >> ");
            var user = scanner.nextLine().trim();
            System.out.printf("password >> ");
            var password = scanner.nextLine().trim();
            var auth = serverRequestHandler(() -> server.login(user, password));
            if (auth != null) {
                return auth;
            } else {
                System.out.printf("try again? [y/n] >>");
                String tryAgain = scanner.nextLine().trim();
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
        return null;
    }

    private static AuthData register (Scanner scanner) {
        var attempting = true;
        while (attempting) {
            System.out.printf("username: >> ");
            var user = scanner.nextLine().trim();
            System.out.printf("password: >> ");
            var password = scanner.nextLine().trim();
            System.out.printf("email: >> ");
            var email = scanner.nextLine().trim();
            var auth = serverRequestHandler(() -> server.register(user, email, password));
            if (auth != null) {
                return auth;
            } else {
                System.out.printf("registration failed, try again? [y/n] >>");
                String tryAgain = scanner.nextLine().trim();
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
        return null;
    }

    private static void userScreen(AuthData user, Scanner scanner) {
        System.out.println("hello " + user.username() + "!");
        var session = true;
        while(session) {
            System.out.printf("[" + user.username() + "] game command >> ");
            String input = scanner.nextLine().trim();
            switch (input) {
                case "h":
                case "help":
                    System.out.println("You can get this help with 'help'\n" +
                    "- logout with 'logout'\n" +
                    "- create a new game with 'create'\n"+
                    "- list games on the server with 'list'\n"+
                    "- join a game with 'play' and the number from the list\n"+
                    "- observe a game with 'watch' and the number from the list\n magical.");
                    break;
                case "logout":
                    session = false;
                    serverRequestHandler(() -> {server.logout(user.authToken()); return null;});
                    System.out.println("logged out!");
                    break;
                case "create":
                    System.out.printf("what do you want to call this game? >>");
                    var name = scanner.nextLine().trim();
                    serverRequestHandler(() -> server.createGame(name, user.authToken()));
                    break;
                case "list":
                    getServerGames(user);
                    break;
                case "play":
                    System.out.println("select a game by id:");
                    getServerGames(user);
                    System.out.printf(">> ");
                    try {
                        var gameId = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        if ((gameId > gameList.size() - 1) || (gameId < 0)) {
                            throw new NumberFormatException();
                        }
                        System.out.printf("as which player? >> ");
                        var color = scanner.nextLine().trim().toUpperCase();
                        while (!color.equals("WHITE") && !color.equals("BLACK")) {
                            System.out.println("try either 'WHITE' or 'BLACK'");
                            System.out.printf(">> ");
                            color = scanner.nextLine().trim().toUpperCase();
                        }
                        var selectedColor = color;
                        serverRequestHandler(() -> {server.joinGame(user.authToken(), gameList.get(gameId).gameID(), selectedColor); return null;});
                        gameScreen(user, gameList.get(gameId), scanner, selectedColor);
                    } catch (NumberFormatException e) {
                        System.out.println("that isn't a game id!");
                    }
                    break;
                case "watch":
                    System.out.println("select a game by id:");
                    getServerGames(user);
                    System.out.printf(">> ");
                    try {
                        var watchId = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        if (watchId > gameList.size() -1 || watchId < 0) {
                            throw new NumberFormatException();
                        }
                        serverRequestHandler(()->{server.watchGame(user.authToken(), watchId); return null;});
                        gameScreen(user, gameList.get(watchId), scanner, "observer");
                    } catch (NumberFormatException e) {
                        System.out.println("that isn't a game id!");
                    }
                    break;
                }
        }
    }

    private static void getServerGames(AuthData user) {
        var serverGames = serverRequestHandler(() -> server.listGames(user.authToken()));
        for (GameData game : serverGames.games()) {
            if (!gameList.contains(game)) {
                gameList.add(game);
            }
        }
        for (int i = 0; i < gameList.size(); i++) {
            System.out.println("#" + (i+1) + " - " + gameList.get(i).gameName());
        }
    }

    private static void gameScreen(AuthData user, GameData game, Scanner scanner, String color) {
        var session = true;
        var perspective = TeamColor.WHITE;
        if (color.equals("BLACK")) {
            perspective = TeamColor.BLACK;
        }
        while(session) {
            System.out.print("\u001b[H\u001b[2J");
            Renderer.render(game.game(), perspective);
            System.out.printf("[" + game.gameName() + "]" + " control >> ");
            var command = scanner.nextLine().trim();
            if (command.equals("q") || command.equals("quit")) {
                session = false;
            }
        }
    }

    private static <T> T serverRequestHandler(Callable<T> request) {
        try {
            return request.call();
        } catch (Throwable e) {
            var err = e.getMessage();
            if (err.contains("400")) {
                System.out.println("client failure ...");
            } else if (err.contains("401")) {
                System.out.println("authorization failed!");
            } else if (err.contains("403")) {
                System.out.println("someone else already took that!");
            } else if (err.contains("500")) {
                System.out.println("server failure!"); 
            } else {
                System.out.println("something went terribly wrong!"); 
            }
            return null;
        }
    }


}

/*
    TODO:
    - negative test cases
*/