package dataaccess;

import model.UserData;

import java.sql.SQLException;

public class SQLUserDAO implements UserDAO{
    @Override
    public void createUser(UserData user) {
        var query = "INSERT INTO UserTable (username, email, password) VALUES(?,?,?)";
        DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                command.setString(1, user.username());
                command.setString(2, user.email());
                command.setString(3, user.password());
                command.executeQuery();
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
                return new UserData(result.getString("username"), result.getString("email"), result.getString("password"));
            } catch (Exception e) {
                throw new DataAccessException("getUser failed");
            }
        });
    }

    @Override
    public void clear() {
        var query = "DROP * FROM UserTable";
        DatabaseManager.runSQLCommand(query, (command) -> {
            try {
                command.executeQuery();
            } catch (SQLException e) {
                throw new DataAccessException("clear failed");
            }
            return 0;
        });
    }
}
