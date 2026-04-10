package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.JsonSyntaxException;
import dataaccess.GameDAO;
import model.GameData;
import model.JoinRequest;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;

public class GameService {
    private final GameDAO db;
    private final AuthService authService;

    public GameService(GameDAO db, AuthService authService) {
        this.db = db;
        this.authService = authService;
    }

    public Collection<GameData> listGames(String authToken) {
        return secure(authToken, db::listGames);
    }

    private <T> T secure(String authToken, Supplier<T> secureCall) {
        if (authService.verify(authToken)) {
            return secureCall.get();
        } else {
            throw new NotAuthorizedError();
        }
    }

    public int newGame(String authToken, String gameName) {
        return secure(authToken, () -> db.createGame(gameName));
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

    public String makeMove(String auth, int gameID, ChessMove move) throws InvalidMoveException {

        var moveResult = secure(auth, () -> {
            var gameData = db.getGame(gameID);
            var game = gameData.game();

            var white = gameData.whiteUsername();
            var black = gameData.blackUsername();
            var user = authService.getUsername(auth);

            var pieceColor = game.getBoard().getPiece(move.getStartPosition()).getTeamColor();
            ChessGame.TeamColor userColor = null;

            if (Objects.equals(user, white)) {
                userColor = WHITE;
            } else if (Objects.equals(user, black)) {
                userColor = BLACK;
            }

            if (userColor == null || pieceColor != userColor) {
                return "invalid";
            }


            try {
                game.makeMove(move);
                db.updateGame(new GameData(gameID, white, black, gameData.gameName(), game));
            } catch (InvalidMoveException e) {
                return "invalid";
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

        if (moveResult != null && moveResult.equals("invalid")) {
            throw new InvalidMoveException();
        } else {
            return moveResult;
        }
    }

    public ChessGame getGame(String auth, int gameID) {
        return secure(auth, () -> db.getGame(gameID).game());
    }

    public GameData getData(String auth, int gameID) {
        return secure(auth, () -> db.getGame(gameID));
    }

    public void resign(String auth, int gameID) {
        secure(auth, () -> {
            var gameData = db.getGame(gameID);
            var user = authService.getUsername(auth);
            if (Objects.equals(gameData.whiteUsername(), user)) {
                db.updateGame(new GameData(gameID, null, gameData.blackUsername(), gameData.gameName(), gameData.game()));
            } else if (Objects.equals(gameData.blackUsername(), user)) {
                db.updateGame(new GameData(gameID, gameData.whiteUsername(), null, gameData.gameName(), gameData.game()));
            }

            return null;
        });
    }
}
