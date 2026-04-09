package network;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import model.GameData;
import model.Notification;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.util.ArrayList;

import static chess.ChessGame.TeamColor.*;

@ClientEndpoint
public class GameSocket {
    private ChessGame game = null;
    private ArrayList<Notification> notifications = new ArrayList<>();
    Session session;

    public GameSocket(String url) throws Exception {
        this.session = ContainerProvider
                .getWebSocketContainer()
                .connectToServer(this, URI.create(url));
    }

    @OnOpen
    public void open() {}

    @OnMessage
    public void receive(String ctx) {
        var msg = new Gson().fromJson(ctx, ServerMessage.class);
        switch(msg.getServerMessageType()) {
            case LOAD_GAME -> {
                this.game = msg.game;
            }
            case ERROR -> {

            }
            case NOTIFICATION -> {
                notifications.add(new Notification("server", msg.message));
            }
        }
    }

    public void send(UserGameCommand command) {}

    @OnClose
    public void close() {}

    public ChessGame getGame() { return game; }

    public Notification[] getNotifications() { return (Notification[]) notifications.toArray(); }

    public void makeMove(ChessMove move) {}

}
