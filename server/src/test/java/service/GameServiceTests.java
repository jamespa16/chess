package service;

import dataaccess.*;
import model.JoinRequest;
import model.LoginRequest;
import model.UserData;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.UUID;

import static chess.ChessGame.TeamColor.*;
import static org.junit.jupiter.api.Assertions.*;

// TESTS NOT FULLY IMPLEMENTED!!
public class GameServiceTests {
    private final String username = "bob";
    private final String password = "1234";
    private final String email = "bob@boingo.com";
    private final UserData user = new UserData(username, password, email);
    private final AuthDAO authDB = new MemoryAuthDAO();
    private final AuthService authService = new AuthService(authDB);

    @Test
    void newGameTest() {
        var gameService = setup();
        var authToken = getAuthToken();
        assertEquals(0, gameService.newGame(authToken));
    }

    @Test
    void listGamesTest() {
        var authToken = getAuthToken();
        var gameService = setup();
        var gameList = new HashSet<>();
        gameList.add(gameService.newGame(authToken));
        gameList.add(gameService.newGame(authToken));
        gameList.add(gameService.newGame(authToken));

        for (var game : gameService.listGames(authToken)) {
            assertTrue(gameList.contains(game.gameID()));
        }
    }

    @Test
    void joinGamesTest() {
        // setup user service
        var userDB = new MemoryUserDAO();
        var authDB = new MemoryAuthDAO();
        var authService = new AuthService(authDB);
        var userService = new UserService(userDB, authService);

        // setup user one
        var username = "bob";
        var password = "1234";
        var email = "bob@boingo.com";
        var user = new UserData(username, password, email);
        userService.registerUser(user);
        var authToken = userService.loginUser(new LoginRequest(username, password));

        // setup user two
        var username2 = "dole";
        var password2 = "abcd";
        var email2 = "dole@boingo.com";
        var user2 = new UserData(username2, password2, email2);
        userService.registerUser(user2);

        // setup game service & game
        var gameDB = new MemoryGameDAO();
        var gameService = new GameService(gameDB, authService);
        var game = gameService.newGame(authToken);

        // user two joins game
        var joinRequest = new JoinRequest(BLACK, game);
        assertDoesNotThrow(() -> gameService.joinGame(joinRequest, username2));
    }

    @Test
    void clearDatabase() {
        // setup user
        var authToken = getAuthToken();

        // setup games
        var gameService = setup();
        gameService.newGame(authToken);
        gameService.newGame(authToken);
        gameService.newGame(authToken);

        // clear & test
        gameService.clearDatabase();
        var list = gameService.listGames(authToken);
        assertTrue(list.isEmpty());
    }

    private GameService setup() {
        var gameDB = new MemoryGameDAO();
        return new GameService(gameDB, authService);
    }

    private UUID getAuthToken() {
        var db = new MemoryUserDAO();
        var userService = new UserService(db, authService);
        userService.registerUser(user);
        return userService.loginUser(new LoginRequest(username, password));
    }
}
