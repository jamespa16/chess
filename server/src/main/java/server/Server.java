package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import model.*;
import service.*;

import java.util.UUID;
import java.util.function.Consumer;

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

    public void registerUser(Context context) { // POST /user
        handler(context, (Context ctx) -> {
            var user = new Gson().fromJson(ctx.body(), UserData.class);
            userService.registerUser(user);
            var token = userService.loginUser(new LoginRequest(user.username(), user.password()));
            var result = new AuthData(token, user.username());

            ctx.status(200);
            ctx.result(new Gson().toJson(result));
        });
    }

    public void loginUser(Context context) { // POST /session
        handler(context, (Context ctx) -> {
            var req = new Gson().fromJson(ctx.body(), LoginRequest.class);
            var token = userService.loginUser(req);
            var result = new AuthData(token, req.username());

            ctx.status(200);
            ctx.result(new Gson().toJson(result));
        });
    }

    public void logoutUser(Context context) { // DELETE /session
        handler(context, (Context ctx) -> {
            var token = new Gson().fromJson(ctx.header("authorization"), UUID.class);
            userService.logoutUser(token);
            ctx.status(200);
            ctx.result();
        });
    }

    public void listGames(Context context) {
        handler(context, (Context ctx) -> {
            var token = new Gson().fromJson(ctx.header("authorization"), UUID.class);
            var list = gameService.listGames(token);
            ctx.status(200);
            ctx.result(new Gson().toJson(list));
        });
    }

    public void newGame(Context context) {
        handler(context, (Context ctx) -> {
            var token = new Gson().fromJson(ctx.header("authorization"), UUID.class);
            var game = gameService.newGame(token);
            ctx.status(200);
            ctx.result("{\"gameID\":" + game + "}");
        });
    }

    public void joinGame(Context context) {
        handler(context, (Context ctx) -> {
            var token = new Gson().fromJson(ctx.header("authorization"), UUID.class);
            var req = new Gson().fromJson(ctx.body(), JoinRequest.class);
            var user = authService.getUsername(token);
            gameService.joinGame(req, user);

            ctx.status(200);
            ctx.result();
        });
    }

    public void clearDatabase(Context ctx) {
        gameService.clearDatabase();
        userService.clearDatabase();
        authService.clearDatabase();
        ctx.status(200);
        ctx.result();
    }

    public void handler(Context ctx, Consumer<Context> callback) {
        try {
            callback.accept(ctx);
        } catch (JsonSyntaxException e) {
            ctx.status(400);
            ctx.result("{\"message\":\"Error: bad request\"}");
        } catch (NotAuthorizedError e) {
            ctx.status(401);
            ctx.result("{\"message\":\"Error: unauthorized\"}");
        } catch (UserAlreadyRegisteredError e) {
            ctx.status(403);
            ctx.result("{\"message\":\"Error: already taken\"}");
        } catch (Exception e) {
            ctx.status(500);
            ctx.result("{\"message\":\"Error: " + e.getMessage() + "\"}");
        }
    }
}
