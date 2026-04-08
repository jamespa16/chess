package network;

import chess.ChessGame;
import chess.ChessMove;
import jakarta.websocket.*;
import model.Notification;

import java.net.URI;

import static chess.ChessGame.TeamColor.*;

@ClientEndpoint
public class GameSocket {
    Session session;

    public GameSocket(String url) throws Exception {
        this.session = ContainerProvider
                .getWebSocketContainer()
                .connectToServer(this, URI.create(url));
    }

    @OnOpen
    public void open() {}

    @OnMessage
    public void receive() {}

    @OnClose
    public void close() {}

    public ChessGame.TeamColor color() { return WHITE; }

    public ChessGame getGame() { return null; }

    public Notification[] getNotifications() { return null; }

    public void makeMove(ChessMove move) {}

}
