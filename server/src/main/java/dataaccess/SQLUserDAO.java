package dataaccess;

import model.UserData;

import java.sql.SQLException;

public class SQLUserDAO implements UserDAO{
    public SQLUserDAO() {
        var query = "CREATE TABLE IF NOT EXISTS UserTable (username VARCHAR(255), password VARCHAR(255), email VARCHAR(255))";
        DatabaseManager.createDatabase();
        DatabaseManager.runSQLCommand(query, (command)->{
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
    public void createUser(UserData user) {
        var userAlreadyExists = false;
        try {
            getUser(user.username());
            userAlreadyExists = true;
        } catch (DataAccessException e) {

        }
        if (userAlreadyExists) {
            throw new DataAccessException("user already exists");
        }
        var query = "INSERT INTO UserTable (username, password, email) VALUES(?,?,?)";
        DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                command.setString(1, user.username());
                command.setString(2, user.password());
                command.setString(3, user.email());
                command.executeUpdate();
                return 0;
            } catch (Exception e) {
                throw new DataAccessException("create user failed");
            }
        });
    }

    @Override
    public UserData getUser(String username) {
        var query = "SELECT * FROM UserTable WHERE username=?";
        return DatabaseManager.runSQLCommand(query, (command) -> {
            try{
                command.setString(1, username);
                var result = command.executeQuery();
                result.next();
                return new UserData(result.getString("username"), result.getString("password"), result.getString("email"));
            } catch (Exception e) {
                throw new DataAccessException("getUser failed");
            }
        });
    }

    @Override
    public void clear() {
        var query = "TRUNCATE TABLE UserTable";
        DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                command.executeUpdate();
            } catch (SQLException e) {
                throw new DataAccessException("clear failed");
            }
            return 0;
        });
    }
}
