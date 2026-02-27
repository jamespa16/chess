package service;

import dataaccess.GameDAO;
import io.javalin.http.Context;
import model.GameData;
import model.JoinRequest;

import java.util.Collection;
import java.util.UUID;

public class GameService {
    GameDAO db;
    public GameService(GameDAO db){
        this.db = db;
    }

    public Collection<GameData> listGames(UUID authToken) {
        throw new RuntimeException("not implemented");
    }

    public GameData newGame(UUID authToken) {
        throw new RuntimeException("not implemented");
    }

    public void joinGame(JoinRequest joinRequest) {
        throw new RuntimeException("not implemented");
    }

    public void clearDatabase() {
        throw new RuntimeException("not implemented");
    }
}
