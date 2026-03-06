package service;

import dataaccess.UserDAO;
import model.LoginRequest;
import model.UserData;

import com.google.gson.JsonSyntaxException;

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

    public String loginUser(LoginRequest request) {
        if(request.username() == null || request.password() == null) {
            throw new JsonSyntaxException("");
        }
        UserData match = db.getUser(request.username());
        if (match != null && match.password().equals(request.password())) {
            return authService.createAuth(match);
        } else {
            throw new NotAuthorizedError();
        }
    }

    public void logoutUser(String authToken) {
        authService.deleteAuth(authToken);
    }

    public void clearDatabase() {
        db.clear();
    }
}
