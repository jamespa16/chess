package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

public class SQLGameDAO implements GameDAO {
    public SQLGameDAO() {
        var query = """
                CREATE TABLE IF NOT EXISTS GameTable (
                gameID INT AUTO_INCREMENT,
                whiteUsername VARCHAR(255),
                blackUsername VARCHAR(255),
                gameName VARCHAR(255),
                game JSON,
                PRIMARY KEY (gameID));""";
        DatabaseManager.createDatabase();
        DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                command.executeUpdate();
                return 0;
            } catch (SQLException e) {
                throw new DataAccessException("table creation failed");
            }
        });
    }

    @Override
    public int createGame(String gameName) {
        var query = "INSERT INTO GameTable (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        return DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                command.setString(1, "");
                command.setString(2, "");
                command.setString(3, gameName);
                command.setString(4, new Gson().toJson(new ChessGame()));
                command.executeUpdate();

                var result = command.getGeneratedKeys();
                if(result.next()) {
                    return result.getInt(1);
                }

                throw new DataAccessException("create game failed");
            } catch (SQLException e) {
                throw new DataAccessException(e.getMessage());
            }
        });
    }

    @Override
    public GameData getGame(int gameID) {
        var query = "SELECT * FROM GameTable WHERE gameID =?";
        return DatabaseManager.runSQLCommand(query, (command) -> {
           try {
               command.setInt(1, gameID);
               var result = command.executeQuery();
               if(result.next()) {
                   return resultToGameData(result);
               }
               throw new DataAccessException("create game failed");
           } catch (SQLException e) {
               throw new DataAccessException(e.getMessage());
           }
        });
    }

    @Override
    public Collection<GameData> listGames() {
        var query = "SELECT * FROM GameTable";
        return DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                var result = command.executeQuery();
                var list = new HashSet<GameData>();
                while(result.next()) {
                    list.add(resultToGameData(result));
                }
                return list;
            } catch (SQLException e) {
                throw new DataAccessException("list games failed");
            }
        });
    }

    @Override
    public void updateGame(GameData newGameState) {
        var query = "UPDATE GameTable SET whiteUsername=?, blackUsername=?, gameName=?, game=? WHERE gameID=?";
        DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                command.setString(1, newGameState.whiteUsername());
                command.setString(2, newGameState.blackUsername());
                command.setString(3, newGameState.gameName());
                command.setString(4, new Gson().toJson(newGameState.game()));
                command.setInt(5, newGameState.gameID());

                command.executeUpdate();
            } catch (SQLException e) {
                throw new DataAccessException(e.getMessage());
            }
            return 0;
        });
    }

    @Override
    public void clear() {
        var query = "TRUNCATE TABLE GameTable";
        DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                command.executeUpdate();
            } catch (SQLException e) {
                throw new DataAccessException("clear failed");
            }
            return 0;
        });
    }

    private GameData resultToGameData(ResultSet result) throws SQLException {
        return new GameData(result.getInt("gameID"),
                result.getString("whiteUsername"),
                result.getString("blackUsername"),
                result.getString("gameName"),
                new Gson().fromJson(result.getString("game"), ChessGame.class));
    }
}
