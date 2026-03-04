package dataaccess;

import model.AuthData;
import model.UserData;

import java.sql.SQLException;
import java.util.UUID;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() {
        var query = "CREATE TABLE IF NOT EXISTS AuthTable(username VARCHAR, authToken VARCHAR)";
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
    public AuthData createAuth(UserData user) {
        var auth = new AuthData(UUID.randomUUID(), user.username());
        var query = "INSERT INTO AuthTable (username, authToken) VALUES (?,?)";
        DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                command.setString(1, auth.username());
                command.setString(2, auth.authToken().toString());
                command.executeUpdate();
                return 0;
            } catch (SQLException e) {
                throw new DataAccessException("table creation failed");
            }
        });
        return auth;
    }

    @Override
    public AuthData getAuth(UserData user) {
        var query = "SELECT * FROM AuthTable WHERE username=?";
        return DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                command.setString(1, user.username());
                var result = command.executeQuery();
                return new AuthData(UUID.fromString(result.getString("authToken")),
                        result.getString("username"));
            } catch (SQLException e) {
                throw new DataAccessException("auth not found");
            }
        });
    }

    @Override
    public void deleteAuth(UUID authToken) {
        var query = "DELETE * FROM AuthTable WHERE username=?";
        DatabaseManager.runSQLCommand(query, (command) -> {
           try {
               command.setString(1, authToken.toString());
               command.executeQuery();
               return 0;
           } catch (SQLException e) {
               throw new DataAccessException("auth not found");
           }
        });
    }

    @Override
    public void clear() {
        var query = "DELETE * FROM AuthTable";
        DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                command.executeQuery();
                return 0;
            } catch (SQLException e) {
                throw new DataAccessException("clear failed");
            }
        });
    }

    @Override
    public boolean verify(UUID authToken) {
        return this.getUsername(authToken) != null;
    }

    @Override
    public String getUsername(UUID authToken) {
        var query = "SELECT * FROM AuthTable WHERE authToken=?";
        return DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                command.setString(1, authToken.toString());
                var result = command.executeQuery();
                return result.getString("username");
            } catch (SQLException e) {
                throw new DataAccessException("auth not found");
            }
        });
    }


}
