package dataaccess;

import model.AuthData;
import model.UserData;

public interface AuthDAO {
    AuthData createAuth(UserData user);
    AuthData getAuth(UserData user);
    void deleteAuth(AuthData authData);
    public void clear();
}
