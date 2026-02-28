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
        try {
            AuthData oldAuth =  getAuth(user);
            return oldAuth;
        } catch (DataAccessException e) { 
            AuthData newAuth = new AuthData(UUID.randomUUID(), user.username());
            this.db.add(newAuth);
            return newAuth;
        }
    }

    @Override
    public AuthData getAuth(UserData user) {
        return db.stream()
                .filter((AuthData auth) -> auth.username().equals(user.username()))
                .findFirst()
                .orElseThrow(DataAccessException::new);
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

    @Override
    public String getUsername(UUID authToken) {
        AuthData authData = db.stream()
                .filter((AuthData auth) -> auth.authToken() == authToken)
                .findFirst()
                .orElseThrow(RuntimeException::new);

        return authData.username();
    }
}
