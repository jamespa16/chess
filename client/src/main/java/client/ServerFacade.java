package client;

import java.util.Collection;

import model.AuthData;
import model.GameData;

public class ServerFacade {
    String serverURL;

    public ServerFacade(){
        serverURL = "https://127.0.0.1:8080";
    }

    public AuthData login(String user, String password) {
        return null;
    }

    public AuthData register(String user, String password) {
        var auth = HttpHandler.post("/user", user, password, AuthData.class);
        return auth;
    }

    public void logout(String authToken) {
        HttpHandler.delete("/session", authToken, null);
    }

    public void createGame(String name, String authToken){
        HttpHandler.post("/game", authToken, name, null);
    }

    public Collection<GameData> listGames(String authToken) {
        return HttpHandler.get("/game", authToken, null, null);
    }

    public GameData joinGame(String authToken, String id) {
        return HttpHandler.put("/game", authToken, id, null);
    }
}