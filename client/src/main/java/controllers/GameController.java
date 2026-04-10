package controllers;

import chess.ChessGame.TeamColor;
import chess.ChessPosition;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import network.GameSocket;
import renderengine.RenderEngine;

import java.net.URI;
import java.util.Scanner;

public class GameController {
    private final Scanner scanner;
    private final GameSocket connection;
    private final Session session;

    public GameController(GameSocket socket, String url) throws Exception {
        this.connection = socket;
        var container = ContainerProvider
                .getWebSocketContainer();
        this.session = container.connectToServer(GameSocket.class, URI.create(url));
        this.scanner = new Scanner(System.in);
    }

    public void gameScreen(TeamColor perspective, String name) {
        var engine = new RenderEngine();

        while (true) {
            var game = connection.getGame();
            if (game != null) {
                engine.updateGame(connection.getGame());
                engine.updateNotifications(connection.getNotifications());
                engine.render(perspective);
            }

            System.out.printf("[ " + name + " ] game control >> ");
            var command = scanner.nextLine().trim();
            switch (command) {
                case "help":
                    System.out.print("available commands:\n" +
                            " - help: brings up this help\n" +
                            " - quit: exits the game screen\n" +
                            " - redraw: refreshes board\n" +
                            " - move: allows you to input a move\n" +
                            " - resign: resigns from the game\n" +
                            " - highlight: shows legal moves for the piece on that square");
                    break;
                case "quit":
                    return;
                case "redraw":
                    break;
                case "move":
                    var start = getPiece("which piece?");
                    var destination = getPiece("to where?");
                    makeMove(start, destination);
                    break;
                case "resign":
                    // resign(user);
                    break;
                case "highlight":
                    var selection = getPiece("which piece?");
                    engine.highlightPiece(selection);
                    break;
            }
        }
    }

    private ChessPosition getPiece(String message) {
        System.out.printf(message + " >> ");
        var input = scanner.nextLine().trim();
        int row = input.codePointAt(0) - 'a' + 1;
        int col = input.codePointAt(1) - '0' + 1;
        return new ChessPosition(row, col);
    }

    private void makeMove(ChessPosition start, ChessPosition end) {
    }
}

