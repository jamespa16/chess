package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private Collection<AuthData> db;

    public MemoryAuthDAO() {
        this.db = new HashSet<>();
    }

    @Override
    public AuthData createAuth(UserData user) {
        AuthData oldAuth = getAuth(user);
        if (oldAuth == null) {
            AuthData newAuth = new AuthData(new UUID(32, 8), user.username());
            this.db.add(newAuth);
            return newAuth;
        } else {
            return oldAuth;
        }
    }

    @Override
    public AuthData getAuth(UserData user) {
        return db.stream()
                .filter((AuthData auth) -> auth.username().equals(user.username()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void deleteAuth(UUID authToken) {
        db.removeIf((AuthData auth) -> authToken == auth.authToken());
    }

    @Override
    public void clear() {
        db.clear();
    }

    @Override
    public boolean verify(UUID authToken) {
        return db.stream().map(AuthData::authToken).anyMatch((UUID token) -> token == authToken);
    }
}
