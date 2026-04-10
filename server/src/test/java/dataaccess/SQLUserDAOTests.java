package dataaccess;

import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTests {
    private final String username = "bob";
    private final String email = "bob@boingo.com";
    private final String password = "1234";
    private final UserData user = new UserData(username, password, email);

    @Test
    void createTableTest() {
        assertDoesNotThrow(SQLUserDAO::new);
    }

    @Test
    void createUser() {
        var db = new SQLUserDAO();
        db.clear();
        assertDoesNotThrow(() -> db.createUser(user));
    }

    @Test
    void getUser() {
        var db = new SQLUserDAO();
        db.clear();
        db.createUser(user);
        assertEquals(user, db.getUser(username));
    }

    @Test
    void clear() {
        var db = new SQLUserDAO();
        assertDoesNotThrow(db::clear);
    }

    @Test
    void createTwoOfUser() {
        var db = new SQLUserDAO();
        db.clear();
        db.createUser(user);
        assertThrows(DataAccessException.class, () -> db.createUser(user));
    }

    @Test
    void getFakeUser() {
        var db = new SQLUserDAO();
        db.clear();
        db.createUser(user);
        assertEquals(user, db.getUser(username));
    }
}
