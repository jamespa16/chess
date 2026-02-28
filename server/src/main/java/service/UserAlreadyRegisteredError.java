package service;

public class UserAlreadyRegisteredError extends RuntimeException {
    public UserAlreadyRegisteredError() {
        super("User Already Registered");
    }
}
