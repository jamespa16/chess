package service;

import chess.ChessGame;
import dataaccess.*;
import model.GameData;
import model.JoinRequest;
import model.UserData;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import static chess.ChessGame.TeamColor.*;
import static org.junit.jupiter.api.Assertions.*;

// TESTS NOT FULLY IMPLEMENTED!!
public class GameServiceTests {
    @Test
    void newGameTest() {
        UUID authToken = new UUID(32,8);
        GameDAO gameDB = new MemoryGameDAO();
        GameService gameService = new GameService(gameDB, new AuthService(new MemoryAuthDAO()));
        GameData game = new GameData(0, "", "", "game0", new ChessGame());
        assertEquals(game, gameService.newGame(authToken));
    }

    @Test
    void listGamesTest() {
        UUID authToken = new UUID(32, 8);
        GameDAO gameDB = new MemoryGameDAO();
        GameService gameService = new GameService(gameDB, new AuthService(new MemoryAuthDAO()));
        Collection<Integer> gameList = new HashSet<>();
        gameList.add(gameService.newGame(authToken));
        gameList.add(gameService.newGame(authToken));
        gameList.add(gameService.newGame(authToken));

        for (GameData game : gameService.listGames(authToken)) {
            assertTrue(gameList.contains(game.gameID()));
        }
    }

    @Test
    void joinGamesTest() {
        // setup user service
        UserDAO userDB = new MemoryUserDAO();
        AuthDAO authDB = new MemoryAuthDAO();
        AuthService authService = new AuthService(authDB);
        UserService userService = new UserService(userDB, authService);

        // setup user one
        String username = "bob";
        String password = "1234";
        String email = "bob@boingo.com";
        UserData user = new UserData(username, password, email);
        userService.registerUser(user);
        UUID authToken = userService.loginUser(user);

        // setup user two
        String username2 = "dole";
        String password2 = "abcd";
        String email2 = "dole@boingo.com";
        UserData user2 = new UserData(username2, password2, email2);
        userService.registerUser(user2);
        UUID authToken2 = userService.loginUser(user);

        // setup game service & game
        GameDAO gameDB = new MemoryGameDAO();
        GameService gameService = new GameService(gameDB, authService);
        int game = gameService.newGame(authToken);

        // user two joins game
        JoinRequest joinRequest = new JoinRequest(BLACK, game);
        assertDoesNotThrow(() -> gameService.joinGame(joinRequest, user2));
    }

    @Test
    void clearDatabase() {
        // setup user
        UUID authToken = new UUID(32, 8);

        // setup games
        GameDAO gameDB = new MemoryGameDAO();
        GameService gameService = new GameService(gameDB, new AuthService(new MemoryAuthDAO()));
        gameService.newGame(authToken);
        gameService.newGame(authToken);
        gameService.newGame(authToken);

        // clear & test
        gameService.clearDatabase();
        Collection<GameData> list = gameService.listGames(authToken);
        assertTrue(list.isEmpty());
    }

}
