package service;

import dataaccess.UserDAO;
import model.LoginRequest;
import model.UserData;

import org.mindrot.jbcrypt.BCrypt;

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
            var secureUser = new UserData(user.username(), BCrypt.hashpw(user.password(), BCrypt.gensalt()), user.email());
            db.createUser(secureUser);
        } else {
            throw new UserAlreadyRegisteredError();
        }
    }

    public String loginUser(LoginRequest request) {
        if(request.username() == null || request.password() == null) {
            throw new JsonSyntaxException("");
        }
        UserData match = db.getUser(request.username());
        if (match != null && BCrypt.checkpw(request.password(), match.password())) {
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
