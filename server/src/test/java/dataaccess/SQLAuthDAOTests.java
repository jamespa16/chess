package dataaccess;

import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class SQLAuthDAOTests {
    private String username = "bob";
    private String email = "bob@boingo.com";
    private String password = "1234";
    private UserData user = new UserData(username, email, password);
    @Test
    void createTableTest() {
        assertDoesNotThrow(() -> new SQLAuthDAO());
    }

    @Test
    void createAuthTest() {
        var db = new SQLAuthDAO();
        assertDoesNotThrow(() -> db.createAuth(user));
    }

    @Test
    void getAuthTest() {
        var db = new SQLAuthDAO();
        var auth = db.createAuth(user);
        assertEquals(auth, db.getAuth(user));
    }

    @Test
    void deleteAuthTest() {
        var db = new SQLAuthDAO();
        var auth = db.createAuth(user);
        assertDoesNotThrow(() -> db.deleteAuth(auth.authToken()));
    }

    @Test
    void clearTest() {
        var db = new SQLAuthDAO();
        assertDoesNotThrow(() -> db.clear());
    }

    @Test 
    void verifyTest() {
        var db = new SQLAuthDAO();
        var auth = db.createAuth(user);
        assertTrue(db.verify(auth.authToken()));
    }

    @Test
    void getUsernameTest() {
        var db = new SQLAuthDAO();
        var auth = db.createAuth(user);
        assertEquals(db.getUsername(auth.authToken()), username);
    }
}
