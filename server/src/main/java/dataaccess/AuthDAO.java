package dataaccess;

import model.AuthData;
import model.UserData;

public interface AuthDAO {
    AuthData createAuth(UserData user);
    AuthData getAuth(UserData user);
    void deleteAuth(String authToken);
    public void clear();
    boolean verify(String authToken);
    String getUsername(String authToken);
}
