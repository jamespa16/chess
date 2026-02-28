package service;

import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import model.UserData;
import org.junit.jupiter.api.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {
    @Test
    void registerNewUserTest() {
        UserDAO db = new MemoryUserDAO();
        UserService userService = new UserService(db);
        String username = "bob";
        String password = "1234";
        String email = "bob@boingo.com";
        UserData user = new UserData(username, password, email);
        userService.registerUser(user);
        assertEquals(user, db.getUser(username));
    }

    @Test
    void registerExistingUser(){
        UserDAO db = new MemoryUserDAO();
        UserService userService = new UserService(db);
        String username = "bob";
        String password = "1234";
        String email = "bob@boingo.com";
        UserData user = new UserData(username, password, email);
        userService.registerUser(user);
        assertThrows(UserAlreadyRegisteredError.class, () -> userService.registerUser(user));
    }

    @Test
    void loginUserTest(){
        UserDAO db = new MemoryUserDAO();
        UserService userService = new UserService(db);
        String username = "bob";
        String password = "1234";
        String email = "bob@boingo.com";
        UserData user = new UserData(username, password, email);
        userService.registerUser(user);
        assertInstanceOf(UUID.class, userService.loginUser(user));
    }

    @Test
    void loginUserUnauthorizedTest(){
        UserDAO db = new MemoryUserDAO();
        UserService userService = new UserService(db);
        String username = "bob";
        String password = "1234";
        String email = "bob@boingo.com";
        UserData user = new UserData(username, password, email);
        userService.registerUser(user);
        assertThrows(NotAuthorizedError.class, () -> userService.loginUser(new UserData(username, "bob", email)));
    }

    @Test
    void logoutUserTest(){
        UserDAO db = new MemoryUserDAO();
        UserService userService = new UserService(db);
        String username = "bob";
        String password = "1234";
        String email = "bob@boingo.com";
        UserData user = new UserData(username, password, email);
        userService.registerUser(user);
        UUID authToken = userService.loginUser(user);
        assertDoesNotThrow(() -> userService.logoutUser(authToken));
    }

    @Test
    void logoutUserUnauthorizedTest(){
        UserDAO db = new MemoryUserDAO();
        UserService userService = new UserService(db);
        String username = "bob";
        String password = "1234";
        String email = "bob@boingo.com";
        UserData user = new UserData(username, password, email);
        userService.registerUser(user);
        UUID authToken = userService.loginUser(user);
        assertThrows(NotAuthorizedError.class, () -> userService.logoutUser(authToken));
    }

    @Test
    void clearDatabase() {
        UserDAO db = new MemoryUserDAO();
        UserService userService = new UserService(db);
        String username = "bob";
        String password = "1234";
        String email = "bob@boingo.com";
        UserData user = new UserData(username, password, email);
        userService.registerUser(user);
        userService.clearDatabase();
        assertDoesNotThrow(() -> userService.registerUser(user));
    }
}
