package dataaccess;

import model.UserData;
import org.junit.jupiter.api.Test;
import service.NotAuthorizedError;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


public class SQLAuthDAOTests {
    private final String username = "bob";
    private final String email = "bob@boingo.com";
    private final String password = "1234";
    private final UserData user = new UserData(username, password, email);

    @Test
    void createTableTest() {
        assertDoesNotThrow(SQLAuthDAO::new);
    }

    @Test
    void createAuthTest() {
        var db = new SQLAuthDAO();
        db.clear();
        assertDoesNotThrow(() -> db.createAuth(user));
    }

    @Test
    void getAuthTest() {
        var db = new SQLAuthDAO();
        db.clear();
        var auth = db.createAuth(user);
        assertEquals(auth, db.getAuth(user));
    }

    @Test
    void deleteAuthTest() {
        var db = new SQLAuthDAO();
        db.clear();
        var auth = db.createAuth(user);
        assertDoesNotThrow(() -> db.deleteAuth(auth.authToken()));
    }

    @Test
    void clearTest() {
        var db = new SQLAuthDAO();
        assertDoesNotThrow(db::clear);
    }

    @Test
    void verifyTest() {
        var db = new SQLAuthDAO();
        db.clear();
        var auth = db.createAuth(user);
        assertTrue(db.verify(auth.authToken()));
    }

    @Test
    void getUsernameTest() {
        var db = new SQLAuthDAO();
        db.clear();
        var auth = db.createAuth(user);
        assertEquals(db.getUsername(auth.authToken()), username);
    }

    @Test
    void createTwoAuthTest() {
        var db = new SQLAuthDAO();
        db.clear();
        db.createAuth(user);
        assertDoesNotThrow(() -> db.createAuth(user));
    }

    @Test
    void getFakeAuthTest() {
        var db = new SQLAuthDAO();
        db.clear();
        assertThrows(NotAuthorizedError.class, () -> db.getAuth(user));
    }

    @Test
    void deleteFakeAuthTest() {
        var db = new SQLAuthDAO();
        db.clear();
        assertThrows(NotAuthorizedError.class, () -> db.deleteAuth(UUID.randomUUID().toString()));
    }

    @Test
    void verifyNoAuthTest() {
        var db = new SQLAuthDAO();
        db.clear();
        assertThrows(NotAuthorizedError.class, () -> db.verify(UUID.randomUUID().toString()));
    }

    @Test
    void getFakeUsernameTest() {
        var db = new SQLAuthDAO();
        db.clear();
        assertThrows(NotAuthorizedError.class, () -> db.getUsername(UUID.randomUUID().toString()));
    }
}
