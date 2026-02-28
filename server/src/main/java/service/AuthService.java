package service;

import dataaccess.AuthDAO;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class AuthService {
    private final AuthDAO db;

    public AuthService(AuthDAO db) {
        this.db = db;
    }

    public UUID createAuth(UserData user){
        return db.createAuth(user).authToken();
    }

    public UUID getAuth(UserData user){
        return db.getAuth(user).authToken();
    }

    public void deleteAuth(UUID authToken){
        db.deleteAuth(authToken);
    }

    public void clearDatabase() {
        db.clear();
    }
}
