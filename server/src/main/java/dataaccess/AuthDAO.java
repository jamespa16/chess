package dataaccess;

import java.util.UUID;

public interface AuthDAO {
    UUID createAuth(UserData user);
    UUID getAuth(UserData user);
    void deleteAuth(UUID authToken);
}
