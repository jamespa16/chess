package service;

public class GameAlreadyJoinedError extends RuntimeException {
    public GameAlreadyJoinedError() {
        super("User tried to join game as a player that has already joined");
    }
}
