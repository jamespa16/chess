package client;

import chess.ChessGame;
import network.ServerFacade;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static final String username = "bob";
    private static final String password = "1234";
    private static final String email = "bob@boing.blob";
    private static Server server;
    private static String url = "http://localhost";

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        url += ":" + port;
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void registerTest() {
        var facade = new ServerFacade(url);

        assertDoesNotThrow(() -> {
            facade.deleteDB();
            facade.register(username, email, password);
        });
    }

    @Test
    public void loginTest() {
        var facade = new ServerFacade(url);
        assertDoesNotThrow(() -> {
            facade.deleteDB();
            var session = facade.register(username, email, password);
            facade.logout(session.authToken());
            facade.login(username, password);
        });
    }

    @Test
    public void logoutTest() {
        var facade = new ServerFacade(url);
        assertDoesNotThrow(() -> {
            facade.deleteDB();
            var session = facade.register(username, email, password);
            facade.logout(session.authToken());
        });
    }

    @Test
    public void createGameTest() {
        var facade = new ServerFacade(url);
        assertDoesNotThrow(() -> {
            facade.deleteDB();
            var session = facade.register(username, email, password);
            facade.createGame("test", session.authToken());
        });
    }

    @Test
    public void listGamesTest() {
        var facade = new ServerFacade(url);
        assertDoesNotThrow(() -> {
            facade.deleteDB();
            var session = facade.register(username, email, password);
            facade.createGame("test1", session.authToken());
            facade.createGame("test2", session.authToken());
            facade.createGame("test3", session.authToken());

            assertEquals(3, facade.listGames(session.authToken()).games().size());
        });
    }

    @Test
    public void joinGameTest() {
        var facade = new ServerFacade(url);
        assertDoesNotThrow(() -> {
            facade.deleteDB();
            var session = facade.register(username, email, password);
            var id = facade.createGame("test1", session.authToken());
            facade.joinGame(session.authToken(), id, ChessGame.TeamColor.WHITE);
        });
    }

    @Test
    public void deleteDBTest() {
        var facade = new ServerFacade(url);
        assertDoesNotThrow(() -> facade.deleteDB());
    }

    @Test
    public void registerDoubleTest() {
        var facade = new ServerFacade(url);
        assertThrows(Exception.class, () -> {
            facade.deleteDB();
            facade.register(username, email, password);
            facade.register(username, email, password);
        });
    }

    @Test
    public void loginNotRegisteredTest() {
        var facade = new ServerFacade(url);
        assertThrows(Exception.class, () -> {
            facade.deleteDB();
            facade.login(username, password);
        });
    }

    @Test
    public void logoutNotInSessionTest() {
        var facade = new ServerFacade(url);
        assertThrows(Exception.class, () -> {
            facade.deleteDB();
            facade.logout("beans");
        });
    }

    @Test
    public void createGameNoAuthTest() {
        var facade = new ServerFacade(url);
        assertThrows(Exception.class, () -> {
            facade.deleteDB();
            facade.createGame("game", "beans");
        });
    }

    @Test
    public void listGamesNoAuthTest() {
        var facade = new ServerFacade(url);
        assertThrows(Exception.class, () -> {
            facade.deleteDB();
            facade.listGames("beans");
        });
    }

    @Test
    public void joinGameDoesNotExist() {
        var facade = new ServerFacade(url);
        assertThrows(Exception.class, () -> {
            facade.deleteDB();
            var session = facade.register(username, email, password);
            facade.joinGame(session.authToken(), 0, ChessGame.TeamColor.BLACK);
        });
    }
}