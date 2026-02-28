package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class MemoryGameDAO implements GameDAO {
    private final Collection<GameData> gameList;

    public MemoryGameDAO() {
        gameList = new HashSet<>();
    }

    @Override
    public int createGame() {
        int id = gameList.size();
        gameList.add(new GameData(id, "", "", "", new ChessGame()));
        return id;
    }

    @Override
    public GameData getGame(int gameID) {
        Optional<GameData> game = gameList.stream()
                .filter(gameData -> gameData.gameID() == gameID)
                .findFirst();
        AtomicReference<GameData> result = new AtomicReference<>();
        game.ifPresent(result::set);
        return result.get();
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
