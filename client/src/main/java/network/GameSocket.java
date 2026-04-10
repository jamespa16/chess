package network;

import com.google.gson.Gson;
import controllers.GameController;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnMessage;
import model.Notification;
import websocket.messages.ServerMessage;

@ClientEndpoint
public class GameSocket {
    GameController controller;

    public GameSocket(GameController controller) {
        this.controller = controller;
        receive("beans");
    }

    @OnMessage
    public void receive(String ctx) {
        System.out.println("msg: " + ctx);
        if (ctx.equals("beans")) {
            return;
        }
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
}
