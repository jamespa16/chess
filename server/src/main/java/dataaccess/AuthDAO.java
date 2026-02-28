package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.UUID;

public interface AuthDAO {
    AuthData createAuth(UserData user);
    AuthData getAuth(UserData user);
    void deleteAuth(UUID authToken);
    public void clear();
}
