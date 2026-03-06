package dataaccess;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import model.UserData;

public class SQLUserDAOTests {
    private String username = "bob";
    private String email = "bob@boingo.com";
    private String password = "1234";
    private UserData user = new UserData(username, password, email);

    @Test
    void createTableTest(){
        assertDoesNotThrow(() -> new SQLUserDAO());
    }

    @Test
    void createUser() {
        var db = new SQLUserDAO();
        assertDoesNotThrow(() -> db.createUser(user));
    }

    @Test
    void getUser() {
        var db = new SQLUserDAO();
        db.createUser(user);
        assertEquals(user, db.getUser(username));
    }

    @Test
    void clear() {
        var db = new SQLUserDAO();
        db.createUser(user);
        assertDoesNotThrow(() -> db.clear());
    }
}
