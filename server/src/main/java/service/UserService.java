package service;

import dataaccess.UserDAO;
import model.UserData;

import java.util.UUID;

public class UserService {
    private UserDAO db;

    public UserService(UserDAO db) {
        this.db = db;
    }

    public UserData registerUser(UserData user) {
        throw new RuntimeException("not implemented");
    }

    public UUID loginUser(UserData user) {
        throw new RuntimeException("not implemented");
    }

    public void logoutUser(UUID authToken) {
        throw new RuntimeException("not implemented");
    }

    public void clearDatabase() { throw new RuntimeException("not implemented"); }
}
