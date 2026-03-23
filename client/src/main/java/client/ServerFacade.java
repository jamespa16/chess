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
        return null;
    }

    public void logout(AuthData user) {
        
    }

    public void createGame(String name){

    }

    public Collection<GameData> listGames() {
        return null;
    }

    public GameData joinGame(AuthData user, String id) {
        return null;
    }
}