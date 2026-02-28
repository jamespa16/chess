package dataaccess;

import chess.ChessGame;
import model.GameData;

import javax.swing.text.html.Option;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class MemoryGameDAO implements GameDAO {
    private Collection<GameData> gameList;

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
        return game.ifPresentOrElse(null);
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
