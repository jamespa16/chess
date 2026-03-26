package client;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import chess.ChessGame.TeamColor;
import model.AuthData;
import model.GameData;

public class ClientMain {
    private final static ServerFacade SERVER_CONNECTION = new ServerFacade("http://127.0.0.1:8080");
    private final static List<GameData> GAME_LIST = new ArrayList<>();
    public static void main(String[] args) {
        System.out.println("  ╭─────╮ ╭─╮ ╭─╮ ╭─────╮ ╭─────╮ ╭─────╮  ");
        System.out.println("  │ ╭───╯ │ │ │ │ │ ╭───╯ │ ╭───╯ │ ╭───╯  ");
        System.out.println("  │ │     │ ╰─╯ │ │ │     │ ╰───╮ │ ╰───╮  ");
        System.out.println("  │ │     │ ╭─╮ │ │ ╰─╮   ╰───╮ │ ╰───╮ │  ");
        System.out.println("  │ ╰───╮ │ │ │ │ │   ╰─╮ ╭───╯ │ ╭───╯ │  ");
        System.out.println("  ╰─────╯ ╰─╯ ╰─╯ ╰─────╯ ╰─────╯ ╰─────╯  ");
        System.out.println("log in with 'login', or 'register' to play!");
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
                    "- register with 'register'.");
                    break;
                case "q":
                case "quit":
                    running = false;
                    System.out.println("goodbye 👋");
                    break;
                case "login":
                    var session = loginScreen(scanner);
                    if (session != null) {
                        userScreen(session, scanner);
                    }
                    break;
                case "register":
                    var user = registerScreen(scanner);
                    if (user != null) {
                        userScreen(user, scanner);
                    }
                    break;
            }
        }
    }

    private static AuthData loginScreen(Scanner scanner) {
        var attempting = true;
        while (attempting) {
            System.out.printf("username >> ");
            var user = scanner.nextLine().trim();
            System.out.printf("password >> ");
            var password = scanner.nextLine().trim();
            var auth = serverRequestHandler(() -> SERVER_CONNECTION.login(user, password));
            if (auth != null) {
                return auth;
            } else {
                attempting = tryAgainScreen(scanner, attempting);
            }
        }
        return null;
    }

    private static AuthData registerScreen(Scanner scanner) {
        var attempting = true;
        while (attempting) {
            System.out.printf("username: >> ");
            var user = scanner.nextLine().trim();
            System.out.printf("password: >> ");
            var password = scanner.nextLine().trim();
            System.out.printf("email: >> ");
            var email = scanner.nextLine().trim();
            var auth = serverRequestHandler(() -> SERVER_CONNECTION.register(user, email, password));
            if (auth != null) {
                return auth;
            } else {
                System.out.printf("as a result, registration failed! ");
                attempting = tryAgainScreen(scanner, attempting);
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
                    "- observe a game with 'watch' and the number from the list\n");
                    break;
                case "logout":
                    session = false;
                    serverRequestHandler(() -> {
                        SERVER_CONNECTION.logout(user.authToken());
                        return null;
                    });
                    System.out.println("logged out!");
                    break;
                case "create":
                    System.out.printf("what do you want to call this game? >> ");
                    var name = scanner.nextLine().trim();
                    serverRequestHandler(() -> SERVER_CONNECTION.createGame(name, user.authToken()));
                    break;
                case "list":
                    updateGameList(user);
                    if (GAME_LIST.size() == 0) {
                        System.out.println("no games currently on server!");
                    } else {
                        System.out.println();
                        System.out.println("id │ game name");
                        System.out.println("───┼───────────────");
                        for (int i = 0; i < GAME_LIST.size(); i++) {
                            System.out.println("#" + (i+1) + " │ " + GAME_LIST.get(i).gameName());
                        }
                        System.out.println();
                    }
                    break;
                case "play":
                    var id = selectGameScreen(user, scanner);
                    if (id == -1) {
                        break;
                    }
                    var color = selectColorScreen(scanner);
                    var selectedGame = GAME_LIST.get(id);
                    var joinedAsWhite = selectedGame.whiteUsername() != null &&
                                        color.equals("WHITE") && selectedGame.whiteUsername().equals(user.username());
                    var joinedAsBlack = selectedGame.blackUsername() != null && color.equals("BLACK") &&
                                        selectedGame.blackUsername().equals(user.username());
                    if (joinedAsWhite || joinedAsBlack) {
                        System.out.println("you've already joined that game!");
                    } else {
                        serverRequestHandler(() -> {
                            SERVER_CONNECTION.joinGame(user.authToken(), selectedGame.gameID(), color);
                            return null;
                        });
                    }
                    gameScreen(user, selectedGame, scanner, color);
                    break;
                case "watch":
                    id = selectGameScreen(user, scanner);
                    gameScreen(user, GAME_LIST.get(id), scanner, "observer");
                    break;
                }
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

    private static boolean tryAgainScreen(Scanner scanner, boolean attempting) {
        System.out.printf("try again? [y/n] >> ");
        String tryAgain = scanner.nextLine().trim();
        switch (tryAgain) {
            case "y":
            case "yes":
                break;
            case "n":
            case "no":
                attempting = false;
                break;
        }
        return attempting;
    }

    private static int selectGameScreen(AuthData user, Scanner scanner) {
        updateGameList(user);
        var running = true;
        var id = -1;
        while(running) {
            System.out.printf("select a game by id: >> ");
            try {
                id = Integer.parseInt(scanner.nextLine().trim()) - 1;
                if ((id > GAME_LIST.size() - 1) || (id < 0)) {
                    id = -1;
                    throw new NumberFormatException();
                } else {
                    running = false;
                }
            } catch (NumberFormatException e) {
                System.out.println("that isn't a valid ID!");
                running = tryAgainScreen(scanner, running);
            }
        }
        return id;
    }

    private static String selectColorScreen(Scanner scanner) {
        var color = "";
        var attempting = true;
        while (attempting) {
            System.out.printf("as which player? >> ");
            color = scanner.nextLine().trim().toUpperCase();
            if (color.equals("WHITE") || color.equals("BLACK")) {
                attempting = false;
            } else {
                attempting = tryAgainScreen(scanner, attempting);
            }
        }
        return color;
    }

    private static void updateGameList(AuthData user) {
        var serverGames = serverRequestHandler(() -> SERVER_CONNECTION.listGames(user.authToken()));
        if (serverGames != null) {
            for (GameData game : serverGames.games()) {
                if (!GAME_LIST.contains(game)) {
                    GAME_LIST.add(game);
                }
            }
        }
    }

    

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    private static <T> T serverRequestHandler(ThrowingSupplier<T> request) {
        try {
            return request.get();
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
SOME THOUGHTS FROM THE AUTOGRADER:
*/