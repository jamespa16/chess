package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import model.GameData;
import model.JoinRequest;
import model.UserData;
import org.jetbrains.annotations.NotNull;
import service.AuthService;
import service.GameService;
import service.UserService;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class Server {

    private final Javalin javalin;
    private final AuthDAO authDB;
    private final AuthService authService;
    private final UserDAO userDB;
    private final UserService userService;
    private final GameDAO gameDB;
    private final GameService gameService;

    public Server() {
        authDB = new MemoryAuthDAO();
        authService = new AuthService(authDB);
        userDB = new MemoryUserDAO();
        userService = new UserService(userDB, authService);
        gameDB = new MemoryGameDAO();
        gameService = new GameService(gameDB, authService);

        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", this::registerUser)
                .post("/session", this::loginUser)
                .delete("/session", this::logoutUser)
                .get("/game", this::listGames)
                .post("/game", this::newGame)
                .put("/game", this::joinGame)
                .delete("/db", this::clearDatabase);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    public void registerUser(Context ctx) {
        decoder(ctx, UserData.class, userService::registerUser);
    }

    public void loginUser(Context ctx) {
        decoder(ctx, UserData.class, userService::loginUser);
    }

    public void logoutUser(Context ctx) {
        decoder(ctx, UUID.class, userService::logoutUser);
    }

    public void listGames(Context ctx) {
        ctx.result(transcoder(ctx, UUID.class, gameService::listGames));
    }

    public void newGame(Context ctx) {
        ctx.result(transcoder(ctx, UUID.class, gameService::newGame));
    }

    public void joinGame(Context ctx) {
        UUID authToken = new Gson().fromJson(ctx.header("authToken"), UUID.class);
        JoinRequest req = new Gson().fromJson(ctx.body(), JoinRequest.class);
        UserData user = userService.getUser(authService.getUsername(authToken));
        gameService.joinGame(req, user);
    }

    public void clearDatabase(Context ctx) {
        gameService.clearDatabase();
        userService.clearDatabase();
        authService.clearDatabase();
    }

    private <T> void decoder(Context ctx, Class<T> clazz, Consumer<T> callback) {
        T obj = new Gson().fromJson(ctx.body(), clazz);
        callback.accept(obj);
    }

    private <T, R> String transcoder(Context ctx, Class<T> clazz, Function<T, R> callback) {
        T obj = new Gson().fromJson(ctx.body(), clazz);
        R result = callback.apply(obj);
        return new Gson().toJson(result);
    }
}
