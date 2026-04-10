package service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import model.UserData;

public class AuthServiceTests {
    private final String username = "bob";
    private final String password = "1234";
    private final String email = "bob@boingo.com";
    private UserData user = new UserData(username, password, email);

    @Test
    void createAuthTest() {
        var authService = setup();
        assertInstanceOf(String.class, authService.createAuth(user));
    }

    @Test
    void getAuthTest() {
        var authService = setup();
        var token = authService.createAuth(user);
        assertEquals(token, authService.getAuth(user));
    }

    @Test
    void deleteAuthTest() {
        var authService = setup();
        var token = authService.createAuth(user);
        assertDoesNotThrow(() -> authService.deleteAuth(token));
    }

    @Test
    void clearDatabaseTest() {
        var authService = setup();
        assertDoesNotThrow(() -> authService.clearDatabase());
    }

    @Test 
    void verifyTest() {
        var authService = setup();
        var token = authService.createAuth(user);
        assertTrue(authService.verify(token));
    }

    @Test
    void getUsernameTest() {
        var authService = setup();
        var token = authService.createAuth(user);
        assertEquals(username, authService.getUsername(token));
    }

    @Test
    void createAuthBadUserTest() {
        var authService = setup();
        assertThrows(DataAccessException.class, () -> authService.createAuth(null));
    }

    @Test
    void getAuthTestUserNotFoundTest() {
        var authService = setup();
        assertThrows(DataAccessException.class, () -> authService.getAuth(user));
    }

    @Test
    void deleteAuthTestAuthNotFoundTest() {
        var authService = setup();
        assertThrows(NotAuthorizedError.class, () -> authService.deleteAuth(UUID.randomUUID().toString()));
    }

    @Test 
    void verifyFailsTest() {
        var authService = setup();
        assertFalse(authService.verify(UUID.randomUUID().toString()));
    }

    @Test
    void getUsernameNotFoundTest() {
        var authService = setup();
        assertThrows(NotAuthorizedError.class, () -> authService.getUsername(UUID.randomUUID().toString()));
    }

    private AuthService setup() {
        return new AuthService(new MemoryAuthDAO());
    }
}
