package dataaccess;

import model.AuthData;
import model.UserData;
import service.NotAuthorizedError;

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
            AuthData newAuth = new AuthData(UUID.randomUUID().toString(), user.username());
            this.db.add(newAuth);
            return newAuth;
        } catch (Exception e) {
            throw new DataAccessException();
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
    public void deleteAuth(String authToken) {
        var token = db.stream()
            .filter((AuthData auth) -> authToken.equals(auth.authToken()))
            .findFirst()
            .orElseThrow(NotAuthorizedError::new);

        db.remove(token);
    }

    @Override
    public void clear() {
        db.clear();
    }

    @Override
    public boolean verify(String authToken) {
        return db.stream().map(AuthData::authToken).anyMatch((String token) -> token.equals(authToken));
    }

    @Override
    public String getUsername(String authToken) {
        AuthData authData = db.stream()
                .filter((AuthData auth) -> auth.authToken().equals(authToken))
                .findFirst()
                .orElseThrow(NotAuthorizedError::new);

        return authData.username();
    }
}
