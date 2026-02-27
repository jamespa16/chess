package service;

public class UserAlreadyRegisteredError extends RuntimeException {
    public UserAlreadyRegisteredError(String message) {
        super(message);
    }
}
