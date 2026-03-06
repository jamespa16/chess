package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.LoginRequest;
import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {
    private final String username = "bob";
    private final String password = "1234";
    private final String email = "bob@boingo.com";
    private final UserData user = new UserData(username, password, email);
    private final LoginRequest req = new LoginRequest(username, password);

    @Test
    void registerNewUserTest() {
        var db = new MemoryUserDAO();
        var authService = new AuthService(new MemoryAuthDAO());
        var userService = new UserService(db, authService);
        userService.registerUser(user);
        assertEquals(user, db.getUser(username));
    }

    @Test
    void registerExistingUser(){
        var userService = setup();
        assertThrows(UserAlreadyRegisteredError.class, () -> userService.registerUser(user));
    }

    @Test
    void loginUserTest(){
        var userService = setup();
        assertInstanceOf(String.class, userService.loginUser(req));
    }

    @Test
    void loginUserUnauthorizedTest(){
        var userService = setup();
        var req = new LoginRequest(username, "bobrocks");
        assertThrows(NotAuthorizedError.class, () -> userService.loginUser(req));
    }

    @Test
    void logoutUserTest(){
        var userService = setup();
        var authToken = userService.loginUser(req);
        assertDoesNotThrow(() -> userService.logoutUser(authToken));
    }

    @Test
    void logoutUserUnauthorizedTest(){
        var userService = setup();
        var authToken = userService.loginUser(req);
        assertDoesNotThrow(() -> userService.logoutUser(authToken));
    }

    @Test
    void clearDatabaseTest() {
        var userService = setup();
        userService.clearDatabase();
        assertDoesNotThrow(() -> userService.registerUser(user));
    }

    private UserService setup() {
        var db = new MemoryUserDAO();
        var authService = new AuthService(new MemoryAuthDAO());
        var userService = new UserService(db, authService);
        userService.registerUser(user);
        return userService;
    }
}
