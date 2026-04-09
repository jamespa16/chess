package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.GameDAO;
import model.GameData;
import model.JoinRequest;

import java.util.Collection;
import java.util.function.Supplier;

import com.google.gson.JsonSyntaxException;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;

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

    public String makeMove(String auth, int gameID, ChessMove move) throws NotAuthorizedError {
        return secure(auth, () -> {
            var game = db.getGame(gameID).game();
            try {
                game.makeMove(move);
            } catch (InvalidMoveException e) {
                throw new RuntimeException(e);
            }

            String result = null;
            if (game.isInCheck(WHITE)) {
                result = "white check";
            }
            if (game.isInCheck(BLACK)) {
                result = "black check";
            }
            if (game.isInCheckmate(WHITE)) {
                result = "white checkmate. black wins!";
            }
            if (game.isInCheckmate(BLACK)) {
                result = "black checkmate. white wins!";
            }
            if (game.isInStalemate(WHITE)) {
                result = "white is out of moves";
            }
            if (game.isInStalemate(BLACK)) {
                result = "black is out of moves";
            }
            return result;
        });
    }

    public ChessGame getGame(String auth, int gameID) {
        return secure(auth, () -> {
            return db.getGame(gameID).game();
        });
    }

    private <T> T secure(String authToken, Supplier<T> secureCall) {
        if (authService.verify(authToken)) {
            return secureCall.get();
        } else {
            throw new NotAuthorizedError();
        }
    }
}
