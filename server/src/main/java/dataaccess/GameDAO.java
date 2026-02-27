package dataaccess;

import java.util.UUID;

public interface GameDAO {
    UUID createGame();
    GameData getGame(UUID gameID);
    Collection<GameData> listGames();
    void updateGame(UUID gameID);
}
