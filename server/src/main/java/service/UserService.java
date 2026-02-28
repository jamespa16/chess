package service;

import dataaccess.UserDAO;
import model.LoginRequest;
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

    public UUID loginUser(LoginRequest request) {
        UserData match = db.getUser(request.username());
        if (match.password() == request.password()) {
            return authService.createAuth(match);
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
}
