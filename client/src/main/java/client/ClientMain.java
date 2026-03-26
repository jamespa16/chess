package client;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import chess.ChessGame.TeamColor;
import model.AuthData;
import model.GameData;

public class ClientMain {
    private final static ServerFacade Server = new ServerFacade("http://127.0.0.1:8080");
    private final static List<GameData> GameList = new ArrayList<>();
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
                    "- register with 'register'.\n magical.");
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
            var auth = serverRequestHandler(() -> Server.login(user, password));
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
            var auth = serverRequestHandler(() -> Server.register(user, email, password));
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
                    "- observe a game with 'watch' and the number from the list\n magical.");
                    break;
                case "logout":
                    session = false;
                    serverRequestHandler(() -> {
                        Server.logout(user.authToken());
                        return null;
                    });
                    System.out.println("logged out!");
                    break;
                case "create":
                    System.out.printf("what do you want to call this game? >> ");
                    var name = scanner.nextLine().trim();
                    serverRequestHandler(() -> Server.createGame(name, user.authToken()));
                    break;
                case "list":
                    updateGameList(user);
                    if (GameList.size() == 0) {
                        System.out.println("no games currently on server!");
                    } else {
                        System.out.println();
                        System.out.println("id │ game name");
                        System.out.println("───┼───────────────");
                        for (int i = 0; i < GameList.size(); i++) {
                            System.out.println("#" + (i+1) + " │ " + GameList.get(i).gameName());
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
                    var selectedGame = GameList.get(id);
                    var joinedAsWhite = selectedGame.whiteUsername() != null && color.equals("WHITE") && selectedGame.whiteUsername().equals(user.username());
                    var joinedAsBlack = selectedGame.blackUsername() != null && color.equals("BLACK") && selectedGame.blackUsername().equals(user.username());
                    if (joinedAsWhite || joinedAsBlack) {
                        System.out.println("you've already joined that game!");
                    } else {
                        serverRequestHandler(() -> {
                            Server.joinGame(user.authToken(), selectedGame.gameID(), color);
                            return null;
                        });
                    }
                    gameScreen(user, selectedGame, scanner, color);
                    break;
                case "watch":
                    id = selectGameScreen(user, scanner);
                    gameScreen(user, GameList.get(id), scanner, "observer");
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
                if ((id > GameList.size() - 1) || (id < 0)) {
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
        var serverGames = serverRequestHandler(() -> Server.listGames(user.authToken()));
        for (GameData game : serverGames.games()) {
            if (!GameList.contains(game)) {
                GameList.add(game);
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
Naming:
	ConstantName:
		[ERROR] /client/src/main/java/client/ServerFacade.java:24:37: Constant name 'client' must match pattern '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$' (UPPER_SNAKE_CASE). [ConstantName] ✅
		[ERROR] /client/src/main/java/client/ClientMain.java:13:39: Constant name 'server' must match pattern '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$' (UPPER_SNAKE_CASE). [ConstantName] ✅
		[ERROR] /client/src/main/java/client/ClientMain.java:14:41: Constant name 'gameList' must match pattern '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$' (UPPER_SNAKE_CASE). [ConstantName] ✅
Code Decomposition:
	MethodLength:
		[ERROR] /client/src/main/java/client/Renderer.java:40:5: Method renderLine length is 103 lines (max allowed is 100). [MethodLength] ✅
Code Readability:
	NestingDepth:
		[ERROR] /client/src/main/java/client/Renderer.java:27:65: Code is too deeply nested. Current depth: 5 (max is 4) [NestingDepth] ✅
		[ERROR] /client/src/main/java/client/Renderer.java:29:36: Code is too deeply nested. Current depth: 5 (max is 4) [NestingDepth] ✅
		[ERROR] /client/src/main/java/client/ClientMain.java:157:56: Code is too deeply nested. Current depth: 5 (max is 4) [NestingDepth] ✅
	LineLength:
		[ERROR] /client/src/main/java/client/ClientMain.java:157: Line is longer than 150 characters (found 152). [LineLength] ✅
*/