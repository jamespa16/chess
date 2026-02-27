package dataaccess;

import model.GameData;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class MemoryGameDAO implements GameDAO {
    private Collection<GameData> gameList;

    public MemoryGameDAO() {
        gameList = new HashSet<>();
    }

    @Override
    public int createGame() {
        return gameList.size();
    }

    @Override
    public GameData getGame(int gameID) {
        return gameList.stream()
                .filter(gameData -> gameData.gameID() == gameID)
                .findFirst()
                .orElse(null);
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
