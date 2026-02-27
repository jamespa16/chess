package dataaccess;

import model.GameData;
import java.util.Collection;

public interface GameDAO {
    int createGame();
    GameData getGame(int gameID);
    Collection<GameData> listGames();
    void updateGame(int gameID);
    public void clear();
}
