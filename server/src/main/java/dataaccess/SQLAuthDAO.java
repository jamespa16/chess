package dataaccess;

import model.AuthData;
import model.UserData;
import service.NotAuthorizedError;

import java.sql.SQLException;
import java.util.UUID;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() {
        var query = "CREATE TABLE IF NOT EXISTS AuthTable (username VARCHAR(255), authToken VARCHAR(255));";
        DatabaseManager.createDatabase();
        DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                command.executeUpdate();
                return 0;
            } catch (SQLException e) {
                throw new DataAccessException("table creation failed");
            }
        });
        this.clear();
    }

    @Override
    public AuthData createAuth(UserData user) {
        var auth = new AuthData(UUID.randomUUID().toString(), user.username());
        var query = "INSERT INTO AuthTable (username, authToken) VALUES (?,?)";
        DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                command.setString(1, auth.username());
                command.setString(2, auth.authToken());
                command.executeUpdate();
                return 0;
            } catch (SQLException e) {
                throw new DataAccessException("create auth failed");
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
                result.next();
                return new AuthData(result.getString("authToken"),
                        result.getString("username"));
            } catch (SQLException e) {
                throw new NotAuthorizedError();
            }
        });
    }

    @Override
    public void deleteAuth(String authToken) {
        getUsername(authToken);
        var query = "DELETE FROM AuthTable WHERE authToken=?";
        DatabaseManager.runSQLCommand(query, (command) -> {
           try {
               command.setString(1, authToken);
               command.executeUpdate();
               return 0;
           } catch (SQLException e) {
               throw new NotAuthorizedError();
           }
        });
    }

    @Override
    public void clear() {
        var query = "TRUNCATE TABLE AuthTable";
        DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                command.executeUpdate();
                return 0;
            } catch (SQLException e) {
                throw new DataAccessException("clear failed");
            }
        });
    }

    @Override
    public boolean verify(String authToken) {
        return this.getUsername(authToken) != null;
    }

    @Override
    public String getUsername(String authToken) {
        var query = "SELECT * FROM AuthTable WHERE authToken=?";
        return DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                command.setString(1, authToken);
                var result = command.executeQuery();
                result.next();
                return result.getString("username");
            } catch (SQLException e) {
                throw new NotAuthorizedError();
            }
        });
    }


}
