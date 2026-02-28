package service;

import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import model.UserData;

import java.util.UUID;

public class UserService {
    private final UserDAO db;
    private final AuthService authService;

    public UserService(UserDAO db, AuthService authService) {
        this.db = db;
        this.authService = authService;
    }

    public void registerUser(UserData user) {
        if(db.getUser(user.username()) == null) {
            db.createUser(user);
        } else {
            throw new UserAlreadyRegisteredError();
        }
    }

    public UUID loginUser(UserData user) {
        UserData match = db.getUser(user.username());
        if (match == user) {
            return authService.createAuth(user);
        } else {
            throw new NotAuthorizedError();
        }
    }

    public void logoutUser(UUID authToken) {
        authService.deleteAuth(authToken);
    }

    public void clearDatabase() {
        db.clear();
    }

    public UserData getUser(String username) {
        return db.getUser(username);
    }
}
