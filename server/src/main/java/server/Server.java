package server;

import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import model.*;
import org.eclipse.jetty.server.Authentication;
import service.*;
import websocket.UserConnection;
import websocket.commands.UserGameCommand;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;
import static websocket.ConnectionType.*;

public class Server {

    private final Javalin javalin;
    private final AuthDAO authDB;
    private final AuthService authService;
    private final UserDAO userDB;
    private final UserService userService;
    private final GameDAO gameDB;
    private final GameService gameService;
    Map<String, UserConnection> sessions;

    public Server() {
        authDB = new SQLAuthDAO();
        authService = new AuthService(authDB);
        userDB = new SQLUserDAO();
        userService = new UserService(userDB, authService);
        gameDB = new SQLGameDAO();
        gameService = new GameService(gameDB, authService);
        sessions = new ConcurrentHashMap<>();

        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", this::registerUser)
                .post("/session", this::loginUser)
                .delete("/session", this::logoutUser)
                .get("/game", this::listGames)
                .post("/game", this::newGame)
                .put("/game", this::joinGame)
                .delete("/db", this::clearDatabase)
                .ws("/ws", this::gameSocket);
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
            var token = ctx.header("authorization");
            userService.logoutUser(token);
            ctx.status(200);
            ctx.result();
        });
    }

    public void listGames(Context context) { // GET /game
        handler(context, (Context ctx) -> {
            var token = ctx.header("authorization");
            var report = new GameReport(gameService.listGames(token));
            ctx.status(200);
            ctx.result(new Gson().toJson(report));
        });
    }

    public void newGame(Context context) { // POST /game
        handler(context, (Context ctx) -> {
            var token = ctx.header("authorization");
            var req = new Gson().fromJson(ctx.body(), GameRequest.class);
            var game = gameService.newGame(token, req.gameName());
            ctx.status(200);
            ctx.result("{\"gameID\":" + game + "}");
        });
    }

    public void joinGame(Context context) { // PUT /game
        handler(context, (Context ctx) -> {
            var token = ctx.header("authorization");
            var req = new Gson().fromJson(ctx.body(), JoinRequest.class);
            var user = authService.getUsername(token);
            gameService.joinGame(token, req, user);
            
            ctx.status(200);
            ctx.result();
        });
     }

    public void clearDatabase(Context context) { // DELETE /db
        handler(context, (ctx)->{
            gameService.clearDatabase();
            userService.clearDatabase();
            authService.clearDatabase();
            ctx.status(200);
            ctx.result();
        });
    }

    public void gameSocket(WsConfig socket) {
        socket.onConnect(ctx -> {
            ctx.enableAutomaticPings();
            var connect = new Gson().fromJson(ctx.queryString(), UserGameCommand.class);
            // look at all joined games. check if AUTH TOKEN is attached to any of them. if so PLAYER, else OBSERVER
            socketConnect(ctx, connect);
        });

        socket.onMessage(ctx -> {
            var message = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (message.getCommandType()) {
                case CONNECT:
                    socketConnect(ctx, message);
                    break;
                case LEAVE:
                    break;
                case MAKE_MOVE:
                    socketMove(message);
                    break;
                case RESIGN:
                    socketResign(message);
                    break;
            }
        });

        socket.onClose(ctx -> {});
    }

    private void socketConnect(WsContext ctx, UserGameCommand connect) {
        var auth = connect.getAuthToken();
        var user = userService.getUsername(auth);
        var gameID = connect.getGameID();

        var activeGames = gameService.listGames(auth);
        for (GameData game : activeGames) {
            if (game.gameID() == gameID) {
                var color = WHITE;
                if (game.blackUsername().equals(user)){
                    color = BLACK;
                }
                sessions.put(auth, new UserConnection(auth, user, gameID, PLAYER, ctx));
                notifyNewPlayer(gameID, user, color);
                return;
            }
        }
        sessions.put(auth, new UserConnection(auth, user, gameID, OBSERVER, ctx));
        notifyNewObserver(gameID, user);
    }

    private void socketLeave(UserGameCommand command) {
        var user = userService.getUser(command.getAuthToken());
        notifyClients(command.getGameID(), user + " has left");
        sessions.remove(command.getAuthToken());
    }

    private void socketMove(UserGameCommand command) {
        var user = userService.getUser(command.getAuthToken());
        var move = new Gson().fromJson(command.getMove(), ChessMove.class);
        var result = gameService.makeMove(move);
        var message = user + "moved " + move.toString();
        if (result != null) {
            message += "resulting in " + result;
        }
        notifyClients(command.getGameID(), message);
    }

    private void socketResign(UserGameCommand command) {
        var user = userService.getUser(command.getAuthToken());
        notifyClients(command.getGameID(), user + "resigned!");
        var stale = sessions.remove(auth);
        stale.connection().closeSession();
    }

    private void notifyResignation(int id, String username) {

    }

    private void notifyClients(int gameID, String message) {
        sessions.forEach((auth, session) -> {
            if (session.gameID() == gameID) {
                session.connection().send(message);
            }
        });
    }

    // All in one error handler function
    private void handler(Context ctx, Consumer<Context> endpoint) {
        try {
            endpoint.accept(ctx);
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
