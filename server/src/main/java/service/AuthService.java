package service;

import dataaccess.AuthDAO;
import model.UserData;

public class AuthService {
    private final AuthDAO db;

    public AuthService(AuthDAO db) {
        this.db = db;
    }

    public String createAuth(UserData user){
        return db.createAuth(user).authToken();
    }

    public String getAuth(UserData user){
        return db.getAuth(user).authToken();
    }

    public void deleteAuth(String authToken){
        db.deleteAuth(authToken);
    }

    public void clearDatabase() {
        db.clear();
    }

    public boolean verify(String authToken) {
        return db.verify(authToken);
    }

    public String getUsername(String authToken) {
        return db.getUsername(authToken);
    }
}
