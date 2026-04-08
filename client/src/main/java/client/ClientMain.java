package client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import RenderEngine.RenderEngine;
import chess.ChessPiece;
import chess.ChessGame.TeamColor;
import chess.ChessMove;
import chess.ChessPosition;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import model.AuthData;
import model.GameData;
import model.Notification;

public class ClientMain {
    private final static ServerFacade SERVER_CONNECTION = new ServerFacade("http://127.0.0.1:8080");
    
    public static void main(String[] args) {
        var running = true;
        var welcome = new WelcomeController(SERVER_CONNECTION);
        var session = welcome.welcomeScreen();
        while (session != null) {
            var game = new UserController(SERVER_CONNECTION, session).userScreen();
            if (game == null) session = welcome.welcomeScreen();
            new GameController(SERVER_CONNECTION, session, game).gameScreen();
        }
    }
}


