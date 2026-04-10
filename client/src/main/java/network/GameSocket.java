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
import java.util.Collection;

import static chess.ChessGame.TeamColor.*;
import static websocket.commands.UserGameCommand.CommandType.CONNECT;

@ClientEndpoint
public class GameSocket {
    private ChessGame game = null;
    private Collection<Notification> notifications = new ArrayList<>();
    String auth;
    int gameID;


    public GameSocket(String url, String authToken, int gameID) throws Exception {
        this.auth = authToken;
        this.gameID = gameID;
    }

    @OnOpen
    public void open(Session session) {
        try {
            send(session, new UserGameCommand(CONNECT, auth, gameID));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.game = getGame();
        this.notifications = getNotifications();
    }

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

    public void send(Session session, UserGameCommand command) throws Exception {
        session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    @OnClose
    public void close() {}

    public ChessGame getGame() { return game; }

    public Collection<Notification> getNotifications() { return notifications; }

}
