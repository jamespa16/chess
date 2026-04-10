package network;

import com.google.gson.Gson;
import controllers.GameController;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import model.Notification;
import websocket.messages.ServerMessage;

@ClientEndpoint
public class GameSocket {
    GameController controller;

    public GameSocket(GameController controller) {
        this.controller = controller;
    }

    @OnOpen
    public void open() {
        System.out.println("connected to server ...");
    }

    @OnMessage
    public void receive(String ctx) {
        System.out.println("msg: " + ctx);
        var msg = new Gson().fromJson(ctx, ServerMessage.class);
        switch (msg.getServerMessageType()) {
            case LOAD_GAME -> {
                controller.game = msg.game;
            }
            case ERROR -> {
                controller.notifications.add(new Notification("error", msg.message));
            }
            case NOTIFICATION -> {
                controller.notifications.add(new Notification("server", msg.message));
            }
        }
    }

    @OnClose
    public void close() {
    }

}
