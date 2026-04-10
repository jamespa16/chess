package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashSet;
import com.google.gson.JsonSyntaxException;

public class MemoryGameDAO implements GameDAO {
    private final Collection<GameData> gameList;

    public MemoryGameDAO() {
        gameList = new HashSet<>();
    }

    @Override
    public int createGame(String gameName) {
        if (gameName == null) {
            throw new JsonSyntaxException("");
        }
        int id = gameList.size() + 1;
        gameList.add(new GameData(id, null, null, gameName, new ChessGame()));
        return id;
    }

    @Override
    public GameData getGame(int gameID) {
        GameData[] list = gameList.toArray(new GameData[gameList.size()]);
        for (GameData data : list) {
            if (data.gameID() == gameID) {
                return data;
            }
        }
        throw new DataAccessException();
    }

    @Override
    public Collection<GameData> listGames() {
        return gameList;
    }

    @Override
    public void updateGame(GameData newGameState) {
        gameList.removeIf(gameData -> gameData.gameID() == newGameState.gameID());
        gameList.add(newGameState);
    }

    @Override
    public void clear() {
        gameList.clear();
    }
}
