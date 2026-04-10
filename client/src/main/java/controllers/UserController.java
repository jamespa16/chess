package controllers;

import chess.ChessGame;
import model.AuthData;
import model.ClientGameRequest;
import model.GameData;
import network.HttpHelper;
import network.ServerFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;

public class UserController {
    private final ServerFacade connection;
    private final AuthData session;
    private final Scanner scanner;
    private final List<GameData> gameList;

    public UserController(ServerFacade connection, AuthData session) {
        this.connection = connection;
        this.session = session;
        this.scanner = new Scanner(System.in);
        this.gameList = new ArrayList<>();
    }

    public ClientGameRequest userScreen() {
        var scanner = new Scanner(System.in);
        System.out.println("hello " + session.username() + "!");
        while (true) {
            System.out.printf("[" + session.username() + "] game command >> ");
            String input = scanner.nextLine().trim();
            switch (input) {
                case "h":
                case "help":
                    printUserHelp();
                    break;
                case "logout":
                    logout();
                    return null;
                case "create":
                    createGame();
                    break;
                case "list":
                    listGames();
                    break;
                case "play":
                    return joinGame(true);
                case "watch":
                    return joinGame(false);
            }
        }
    }

    private void printUserHelp() {
        System.out.println("You can get this help with 'help'\n" +
                "- logout with 'logout'\n" +
                "- create a new game with 'create'\n" +
                "- list games on the server with 'list'\n" +
                "- join a game with 'play' and the number from the list\n" +
                "- observe a game with 'watch' and the number from the list\n");
    }

    private void logout() {
        HttpHelper.serverRequestHandler(() -> connection.logout(session.authToken()));
        System.out.println("logged out!");
    }

    private void createGame() {
        System.out.print("what do you want to call this game? >> ");
        var name = scanner.nextLine().trim();
        HttpHelper.serverRequestHandler(() -> connection.createGame(name, session.authToken()));
    }

    private void listGames() {
        updateGameList();
        if (gameList.size() == 0) {
            System.out.println("no games currently on server!");
        } else {
            System.out.println();
            System.out.println("id │ game name - white - black");
            System.out.println("───┼───────────────────────────────");
            for (int i = 0; i < gameList.size(); i++) {
                var entry = gameList.get(i);
                System.out.println("#" + (i + 1) + " │  " +
                        entry.gameName() + " -  " +
                        entry.whiteUsername() + " -  " +
                        entry.blackUsername());
            }
            System.out.println();
        }
    }

    private void updateGameList() {
        var serverGames = HttpHelper.serverRequestHandler(() -> connection.listGames(session.authToken()));
        if (serverGames != null) {
            for (GameData game : serverGames.games()) {
                if (!gameList.contains(game)) {
                    gameList.add(game);
                }
            }
        }
    }

    private ClientGameRequest joinGame(boolean isPlaying) {
        var id = selectGame();
        if (id == -1) {
            return null;
        }

        var selectedGame = gameList.get(id);
        if (isPlaying) {
            var color = selectColor();
            if (color == null) {
                return null;
            }
            var joinedAsWhite = selectedGame.whiteUsername() != null &&
                    color.equals(WHITE) && selectedGame.whiteUsername().equals(session.username());
            var joinedAsBlack = selectedGame.blackUsername() != null && color.equals(WHITE) &&
                    selectedGame.blackUsername().equals(session.username());
            if (joinedAsWhite || joinedAsBlack) {
                System.out.println("you've already joined that game!");
            } else {

                HttpHelper.serverRequestHandler(() -> connection.joinGame(session.authToken(), selectedGame.gameID(), color));
            }
            return new ClientGameRequest(color, selectedGame.gameName(), id);
        }
        return new ClientGameRequest(WHITE, selectedGame.gameName(), id);
    }

    private int selectGame() {
        updateGameList();
        var attempting = true;
        while (attempting) {
            System.out.print("select a game by id: >> ");
            try {
                var id = Integer.parseInt(scanner.nextLine().trim()) - 1;
                if ((id > gameList.size() - 1) || (id < 0)) {
                    throw new NumberFormatException();
                } else {
                    return id;
                }
            } catch (NumberFormatException e) {
                System.out.println("that isn't a valid ID!");
                attempting = ControllerHelper.tryAgain(scanner);
            }
        }
        return -1;
    }

    private ChessGame.TeamColor selectColor() {
        var color = "";
        var attempting = true;
        while (attempting) {
            attempting = false;
            System.out.print("as which player? >> ");
            color = scanner.nextLine().trim().toUpperCase();
            if (color.equals("WHITE")) {
                return WHITE;
            } else if (color.equals("BLACK")) {
                return BLACK;
            } else {
                System.out.println("enter WHITE or BLACK");
                attempting = ControllerHelper.tryAgain(scanner);
            }
        }
        return null;
    }
}