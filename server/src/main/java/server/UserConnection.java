package server;

import io.javalin.websocket.WsContext;

public record UserConnection(String auth, String username, int gameID, ConnectionType type, WsContext connection) {
}
