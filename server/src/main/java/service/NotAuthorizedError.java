package service;

public class NotAuthorizedError extends RuntimeException {
    public NotAuthorizedError() {
        super("Not Authorized");
    }
}
