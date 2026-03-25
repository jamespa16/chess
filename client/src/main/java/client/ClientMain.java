package client;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.jupiter.api.function.ThrowingSupplier;

import chess.*;
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
            String input = scanner.nextLine();
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
            var user = scanner.nextLine();
            System.out.printf("password >> ");
            var password = scanner.nextLine();
            var auth = serverRequestHandler(() -> server.login(user, password));
            if (auth != null) {
                return auth;
            } else {
                System.out.printf("authorization failed, try again? [y/n] >>");
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
        return null;
    }

    private static AuthData register (Scanner scanner) {
        var attempting = true;
        while (attempting) {
            System.out.printf("username ->> ");
            var user = scanner.nextLine();
            System.out.printf("password ->> ");
            var password = scanner.nextLine();
            System.out.printf("email ->> ");
            var email = scanner.nextLine();
            var auth = serverRequestHandler(() -> server.register(user, email, password));
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
        return null;
    }

    private static void userScreen(AuthData user, Scanner scanner) {
        System.out.println("hello " + user.username() + "!");
        var session = true;
        while(session) {
            System.out.printf("[" + user.username() + "] command >> ");
            String input = scanner.nextLine();
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
                    break;
                case "create":
                    System.out.printf("what do you want to call this game? >>");
                    var name = scanner.nextLine();
                    serverRequestHandler(() -> server.createGame(name, user.authToken()));
                    break;
                case "list":
                    getServerGames(user);
                    break;
                case "play":
                    System.out.println("which game?");
                    getServerGames(user);
                    System.out.printf(">> ");
                    var gameId = Integer.parseInt(scanner.nextLine()) - 1;
                    System.out.printf("as which player? >> ");
                    var color = scanner.nextLine();
                    serverRequestHandler(() -> {server.joinGame(user.authToken(), gameList.get(gameId).gameID(), color); return null;});
                    gameScreen(user, gameList.get(gameId), scanner);
                    break;
                case "watch":
                    System.out.println("which game?");
                    getServerGames(user);
                    System.out.printf(">> ");
                    var watchId = Integer.parseInt(scanner.nextLine()) - 1;
                    serverRequestHandler(()->{server.watchGame(user.authToken(), watchId); return null;});
                    gameScreen(user, gameList.get(watchId), scanner);
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

    private static void gameScreen(AuthData user, GameData game, Scanner scanner) {
        while(true) {
            Renderer.render(game.game());
            System.out.printf("command >> ");
            var command = scanner.nextLine();
            if (command == "q" || command == "quit") {
                return;
            }
        }
    }

    private static <T> T serverRequestHandler(Callable<T> request) {
        try {
            return request.call();
        } catch (Throwable e) {
            System.out.println("something terrible has happened..." + e.getMessage());
            return null;
        }
    }


}

/*
    TODO:
    - pretty renderer
    - error handling & real state
    - negative test cases
*/