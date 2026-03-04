package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.UUID;

public class SQLAuthDAO implements AuthDAO {
    @Override
    public AuthData createAuth(UserData user) {
        return null;
    }

    @Override
    public AuthData getAuth(UserData user) {
        return null;
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
}
