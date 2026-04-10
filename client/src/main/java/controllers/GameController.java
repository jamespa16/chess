package controllers;

import RenderEngine.RenderEngine;
import chess.ChessGame;
import chess.ChessGame.TeamColor;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import model.Notification;
import network.GameSocket;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import static chess.ChessGame.TeamColor.WHITE;
import static chess.ChessPiece.PieceType;
import static chess.ChessPiece.PieceType.*;
import static websocket.commands.UserGameCommand.CommandType.*;

public class GameController {
    private final Scanner scanner;
    private final Session session;
    public String auth;
    public int gameID;
    public ChessGame game = null;
    public Collection<Notification> notifications = new ArrayList<>();

    public GameController(String url, String auth, int gameID) throws Exception {
        this.auth = auth;
        this.gameID = gameID;
        var container = ContainerProvider
                .getWebSocketContainer();
        this.session = container.connectToServer(new GameSocket(this), URI.create(url));
        var connectionMessage = new UserGameCommand(CONNECT, auth, gameID);
        session.getBasicRemote().sendText(new Gson().toJson(connectionMessage));
        this.scanner = new Scanner(System.in);
    }

    public void gameScreen(TeamColor perspective, String name) {
        var engine = new RenderEngine();

        while (true) {
            if (game != null) {
                engine.updateGame(game);
                engine.updateNotifications(notifications);
                engine.render(perspective);
            } else {
                System.out.println("waiting for server ...");
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
                    var msg = new UserGameCommand(RESIGN, auth, gameID);
                    try {
                        session.getBasicRemote().sendText(new Gson().toJson(msg));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
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
        var piece = game.getBoard().getPiece(start);
        ChessMove move = null;
        PieceType promo = null;
        if (piece.getPieceType() == PAWN) {
            if (piece.getTeamColor() == WHITE) {
                if (end.getRow() == 8) {
                    promo = getPromo();
                }
            } else {
                if (end.getRow() == 1) {
                    promo = getPromo();
                }
            }
        }

        move = new ChessMove(start, end, promo);
        var msg = new UserGameCommand(MAKE_MOVE, auth, gameID, move);
        try {
            session.getBasicRemote().sendText(new Gson().toJson(msg));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PieceType getPromo() {
        var promo = PAWN;
        System.out.print("what promotion? >> ");
        switch (scanner.nextLine().toLowerCase().trim()) {
            case "rook":
                promo = ROOK;
                break;
            case "bishop":
                promo = BISHOP;
                break;
            case "knight":
                promo = KNIGHT;
                break;
            case "queen":
                promo = QUEEN;
                break;
            default:
                System.out.println("you are getting a pawn lol");
        }
        return promo;
    }
}

