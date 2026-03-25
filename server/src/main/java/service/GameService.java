package service;

import dataaccess.GameDAO;
import model.GameData;
import model.JoinRequest;

import java.util.Collection;
import java.util.function.Supplier;

import com.google.gson.JsonSyntaxException;

public class GameService {
    private final GameDAO db;
    private final AuthService authService;

    public GameService(GameDAO db, AuthService authService){
        this.db = db;
        this.authService = authService;
    }

    public Collection<GameData> listGames(String authToken) {
        return secure(authToken, db::listGames);
    }

    public int newGame(String authToken, String gameName) {
        return secure(authToken, () -> {return db.createGame(gameName);});
    }

    public void joinGame(String authToken, JoinRequest joinRequest, String user) {
        if (!authService.verify(authToken)) {
            throw new NotAuthorizedError();
        }

        if (joinRequest.gameID() == null || joinRequest.playerColor() == null) {
            throw new JsonSyntaxException("bad req: null in joinReq");
        }

        GameData game = db.getGame(joinRequest.gameID());
        if (joinRequest.playerColor().equals("WHITE")) {
            if (game.whiteUsername() == null) {
                db.updateGame(new GameData(
                        game.gameID(),
                        user,
                        game.blackUsername(),
                        game.gameName(),
                        game.game()));
            } else {
                throw new UserAlreadyRegisteredError();
            }
        } else if (joinRequest.playerColor().equals("BLACK")) {
            if (game.blackUsername() == null) {
                db.updateGame(new GameData(
                        game.gameID(),
                        game.whiteUsername(),
                        user,
                        game.gameName(),
                        game.game()));
            } else {
                throw new UserAlreadyRegisteredError();
            }
        } else {
            throw new JsonSyntaxException("invalid color");
        }


    }

    public void clearDatabase() {
        db.clear();
    }

    private <T> T secure(String authToken, Supplier<T> secureCall) {
        if (authService.verify(authToken)) {
            return secureCall.get();
        } else {
            throw new NotAuthorizedError();
        }
    }
}
