package service;

import chess.ChessGame;
import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
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
        UserDAO userDB = new MemoryUserDAO();
        UserService userService = new UserService(userDB);
        String username = "bob";
        String password = "1234";
        String email = "bob@boingo.com";
        UserData user = new UserData(username, password, email);
        userService.registerUser(user);
        UUID authToken = userService.loginUser(user);
        GameDAO gameDB = new MemoryGameDAO();
        GameService gameService = new GameService(gameDB);
        GameData game = new GameData(0, "bob", "", "game0", new ChessGame());
        assertEquals(game, gameService.newGame(authToken));
    }

    @Test
    void listGamesTest() {
        UserDAO userDB = new MemoryUserDAO();
        UserService userService = new UserService(userDB);
        String username = "bob";
        String password = "1234";
        String email = "bob@boingo.com";
        UserData user = new UserData(username, password, email);
        userService.registerUser(user);
        UUID authToken = userService.loginUser(user);
        GameDAO gameDB = new MemoryGameDAO();
        GameService gameService = new GameService(gameDB);
        Collection<GameData> gameList = new HashSet<>();
        gameList.add(gameService.newGame(authToken));
        gameList.add(gameService.newGame(authToken));
        gameList.add(gameService.newGame(authToken));

        for (GameData game : gameService.listGames(authToken)) {
            assertTrue(gameList.contains(game));
        }
    }

    @Test
    void joinGamesTest() {
        // setup user service
        UserDAO userDB = new MemoryUserDAO();
        UserService userService = new UserService(userDB);

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
        GameService gameService = new GameService(gameDB);
        GameData game = gameService.newGame(authToken);

        // user two joins game
        JoinRequest joinRequest = new JoinRequest(BLACK, game.gameID());
        assertDoesNotThrow(() -> gameService.joinGame(joinRequest));
    }

    @Test
    void clearDatabase() {
        // setup user
        UserDAO userDB = new MemoryUserDAO();
        UserService userService = new UserService(userDB);
        String username = "bob";
        String password = "1234";
        String email = "bob@boingo.com";
        UserData user = new UserData(username, password, email);
        userService.registerUser(user);
        UUID authToken = userService.loginUser(user);

        // setup games
        GameDAO gameDB = new MemoryGameDAO();
        GameService gameService = new GameService(gameDB);
        gameService.newGame(authToken);
        gameService.newGame(authToken);
        gameService.newGame(authToken);

        // clear & test
        gameService.clearDatabase();
        Collection<GameData> list = gameService.listGames(authToken);
        assertTrue(list.isEmpty());
    }
}
