package service;

import io.javalin.http.Context;
import java.util.UUID;

public class UserService {
    public UserService() {
    }

    public UUID registerUser(Context ctx) {
        throw new RuntimeException("not implemented");
    }

    public UUID loginUser(Context ctx) {
        throw new RuntimeException("not implemented");
    }

    public void logoutUser(Context ctx) {
        throw new RuntimeException("not implemented");
    }
}
