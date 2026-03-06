package dataaccess;

import model.GameData;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import chess.ChessGame;

public class SQLGameDAOTests {
    @Test
    void createTableTest() {
        assertDoesNotThrow(SQLGameDAO::new);
    }

    @Test
    void createGameTest() {
        var db = new SQLGameDAO();
        db.clear();
        assertDoesNotThrow(() -> db.createGame("game"));
    }

    @Test
    void getGameTest() {
        var db = new SQLGameDAO();
        db.clear();
        var id = db.createGame("game");
        assertEquals(id, db.getGame(id).gameID());
    }

    @Test
    void listGamesTest() {
        var db = new SQLGameDAO();
        db.clear();
        Collection<Integer> list = new HashSet<>();
        list.add(db.createGame("game1"));
        list.add(db.createGame("game2"));
        list.add(db.createGame("game3"));

        for (Integer game : list) {
            assertTrue(
                db.listGames()
                .stream()
                .anyMatch((data) -> data.gameID() == game.intValue())
            );
        }
    }

    @Test 
    void updateGame() {
        var db = new SQLGameDAO();
        db.clear();
        var game = db.createGame("game");
        var newGame = new GameData(game, "bob", "boing", "gameBOB", new ChessGame());
        assertDoesNotThrow(() -> db.updateGame(newGame));
    }

    @Test
    void clearTest() {
        var db = new SQLGameDAO();
        db.createGame("game");
        assertDoesNotThrow(db::clear);
    }

    @Test
    void getFakeGameTest() {
        var db = new SQLGameDAO();
        db.clear();
        assertThrows(DataAccessException.class, () -> db.getGame(1));
    }

    @Test
    void listNoGamesTest() {
        var db = new SQLGameDAO();
        db.clear();
        Collection<Integer> list = new HashSet<>();

        for (Integer game : list) {
            assertTrue(
                db.listGames()
                .stream()
                .anyMatch((data) -> data.gameID() == game.intValue())
            );
        }
    }

    @Test 
    void updateFakeGame() {
        var db = new SQLGameDAO();
        db.clear();
        var newGame = new GameData(1, "bob", "boing", "gameBOB", new ChessGame());
        assertDoesNotThrow(() -> db.updateGame(newGame));
    }
}
