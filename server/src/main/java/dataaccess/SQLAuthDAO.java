package dataaccess;

import model.AuthData;
import model.UserData;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Function;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() {
        DatabaseManager.createDatabase();
        try (var db = DatabaseManager.getConnection()) {
            var createAuthTable = "CREATE TABLE IF NOT EXISTS AuthTable(username VARCHAR, authToken VARCHAR)";
            try (var command = db.prepareStatement(createAuthTable)){
                command.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("table creation failed");
        }
    }

    @Override
    public AuthData createAuth(UserData user) {
        try (var db = DatabaseManager.getConnection()) {
            var auth = new AuthData(UUID.randomUUID(), user.username());
            var setAuth = "INSERT INTO AuthTable (username, authToken) VALUES (?,?)";
            try (var command = db.prepareStatement(setAuth)) {
                command.setString(1, auth.username());
                command.setString(2, auth.authToken().toString());
                command.executeUpdate();
            }
            return auth;
        }  catch (SQLException e) {
            throw new DataAccessException("table creation failed");
        }
    }

    @Override
    public AuthData getAuth(UserData user) {
        var query = "SELECT * FROM AuthTable WHERE username=?";
        return sqlQuery((db) -> {
            try (var command = db.prepareStatement(query)) {
                command.setString(1, user.username());
                try (var result = command.executeQuery()) {
                    return new AuthData(UUID.fromString(result.getString("authToken")),
                            result.getString("username"));
                }
            } catch (SQLException e) {
                throw new DataAccessException("auth not found");
            }
        });
    }

    @Override
    public void deleteAuth(UUID authToken) {

    }

    @Override
    public void clear() {

    }

    @Override
    public boolean verify(UUID authToken) {
        return false;
    }

    @Override
    public String getUsername(UUID authToken) {
        return "";
    }

    private <T> T sqlQuery(Function<Connection, T> exec) {
        try (var db = DatabaseManager.getConnection()) {
            return exec.apply(db);
        } catch (SQLException e) {
            throw new DataAccessException("SQL query failed");
        }
    }
}
