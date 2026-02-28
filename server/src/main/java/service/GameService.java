package service;

import static chess.ChessGame.TeamColor.*;
import dataaccess.GameDAO;
import model.GameData;
import model.JoinRequest;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

public class GameService {
    private final GameDAO db;
    private final AuthService authService;

    public GameService(GameDAO db, AuthService authService){
        this.db = db;
        this.authService = authService;
    }

    public Collection<GameData> listGames(UUID authToken) {
        return secure(authToken, db::listGames);
    }

    public int newGame(UUID authToken) {
        return secure(authToken, db::createGame);
    }

    public void joinGame(JoinRequest joinRequest, String user) {
        GameData game = db.getGame(joinRequest.gameID());
        if (joinRequest.color() == WHITE) {
            if (game.whiteUsername().isEmpty()) {
                db.updateGame(new GameData(
                        game.gameID(),
                        user,
                        game.blackUsername(),
                        game.gameName(),
                        game.game()));
            } else {
                throw new GameAlreadyJoinedError();
            }
        } else {
            if (game.blackUsername().isEmpty()) {
                db.updateGame(new GameData(
                        game.gameID(),
                        game.whiteUsername(),
                        user,
                        game.gameName(),
                        game.game()));
            } else {
                throw new GameAlreadyJoinedError();
            }
        }
    }

    public void clearDatabase() {
        db.clear();
    }

    private <T> T secure(UUID authToken, Supplier<T> secureCall) {
        if (authService.verify(authToken)) {
            return secureCall.get();
        } else {
            throw new NotAuthorizedError();
        }
    }
}
