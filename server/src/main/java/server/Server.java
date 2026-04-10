package server;

import chess.InvalidMoveException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import io.javalin.websocket.WsContext;
import model.*;
import service.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;
import static server.ConnectionType.*;
import static websocket.messages.ServerMessage.ServerMessageType.*;

public class Server {

    private final Javalin javalin;
    private final AuthDAO authDB;
    private final AuthService authService;
    private final UserDAO userDB;
    private final UserService userService;
    private final GameDAO gameDB;
    private final GameService gameService;
    Collection<WsContext> connections;
    Map<String, UserConnection> sessions;


    public Server() {
        authDB = new SQLAuthDAO();
        authService = new AuthService(authDB);
        userDB = new SQLUserDAO();
        userService = new UserService(userDB, authService);
        gameDB = new SQLGameDAO();
        gameService = new GameService(gameDB, authService);
        sessions = new ConcurrentHashMap<>();
        connections = new ArrayList<>();

        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", this::registerUser)
                .post("/session", this::loginUser)
                .delete("/session", this::logoutUser)
                .get("/game", this::listGames)
                .post("/game", this::newGame)
                .put("/game", this::joinGame)
                .delete("/db", this::clearDatabase)
                .ws("/ws", (socket) -> {
                    socket.onConnect(ctx -> {
                        ctx.enableAutomaticPings();
                        System.out.println("CONNECTION MADE " + ctx.sessionId());
                        connections.add(ctx);
                    });

                    socket.onMessage(ctx -> {
                        System.out.println("MESSAGE RECEIVED " + ctx.sessionId());
                        var message = new Gson().fromJson(ctx.message(), UserGameCommand.class);
                        switch (message.getCommandType()) {
                            case CONNECT:
                                socketConnect(ctx, message);
                                break;
                            case LEAVE:
                                socketLeave(message);
                                break;
                            case MAKE_MOVE:
                                socketMove(ctx, message);
                                break;
                            case RESIGN:
                                socketResign(ctx, message);
                                break;
                        }
                    });

                    socket.onClose(ctx -> {
                        System.out.println("SESSION CLOSED " + ctx.sessionId());
                        connections.remove(ctx);
                    });
                });
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

    private void socketConnect(WsContext ctx, UserGameCommand connect) {
        try {
            var auth = connect.getAuthToken();
            var user = authService.getUsername(auth);
            var gameID = connect.getGameID();

            var activeGames = gameService.listGames(auth);
            var validGame = false;
            for (GameData game : activeGames) {

                if (game.gameID() == gameID) {
                    validGame = true;
                    var color = WHITE;
                    var message = "";
                    if (Objects.equals(game.whiteUsername(), user)) {
                        message = "white";
                    } else if (game.blackUsername().equals(user)) {
                        color = BLACK;
                        message = "black";
                    } else {
                        sessions.put(auth, new UserConnection(auth, user, gameID, OBSERVER, ctx));
                        ctx.send(new Gson().toJson(new ServerMessage(LOAD_GAME, null, game.game())));
                        notifyClients(gameID, new ServerMessage(NOTIFICATION, user + "is now observing"), auth);
                        return;
                    }

                    System.out.println("notifying others...");
                    notifyClients(gameID, new ServerMessage(NOTIFICATION, message + " is now " + user), null);
                    System.out.println("adding client to game...");
                    sessions.put(auth, new UserConnection(auth, user, gameID, PLAYER, ctx));
                    System.out.println("loading game on client...");
                    var load = new ServerMessage(LOAD_GAME, null, game.game());
                    ctx.send(new Gson().toJson(load));
                    return;
                }
            }
            if(!validGame){
                sendError(ctx, "Game ID is invalid");
            }
        } catch (Exception e) {
            sendError(ctx, "Connection Error: " + e.getMessage());
        }
    }


    private void socketLeave(UserGameCommand command) {
        var user = authService.getUsername(command.getAuthToken());
        gameService.resign(command.getAuthToken(), command.getGameID());
        sessions.remove(command.getAuthToken());
        notifyClients(command.getGameID(), new ServerMessage(NOTIFICATION, user + " has left"), null);

    }

    private void socketMove(WsContext ctx, UserGameCommand command) {
        if (!sessions.containsKey(command.getAuthToken())) {
            sendError(ctx, "you have no games to move");
            return;
        }

        try {
            System.out.println("getting context ...");
            var auth = command.getAuthToken();
            var user = authService.getUsername(auth);
            var gameID = command.getGameID();
            var gameData = gameService.getData(auth, gameID);
            System.out.println(gameData);
            if (!(!Objects.equals(user, gameData.whiteUsername()) || !Objects.equals(user, gameData.blackUsername()))) throw new NotAuthorizedError();
            System.out.println("computing move");
            var result = gameService.makeMove(auth, gameID, command.getMove());

            System.out.println("broadcasting move ...");
            var content = new ServerMessage(LOAD_GAME, null, gameService.getGame(auth, gameID));
            notifyClients(gameID, content, null);

            if (result != null && result.contains("checkmate")) {
                notifyClients(gameID, new ServerMessage(NOTIFICATION, user + " checkmate!"), null);
            } else if (result != null && result.contains("check")) {
                notifyClients(gameID, new ServerMessage(NOTIFICATION, user + " check!"), null);
            } else if (result != null && result.contains("stalemate")) {
                notifyClients(gameID, new ServerMessage(NOTIFICATION, user + " stalemate!"), null);
            }

            notifyClients(gameID, new ServerMessage(NOTIFICATION, user + " moved"), auth);
        } catch (NotAuthorizedError e) {
            sendError(ctx, "you are not part of this game!");
        } catch (InvalidMoveException e) {
            sendError(ctx, "that move is invalid");
        }
    }

    private void socketResign(WsContext ctx, UserGameCommand command) {
        var auth = command.getAuthToken();
        var user = authService.getUsername(auth);
        System.out.println("resigning " + user + " @ " + ctx.sessionId());
        var data = gameService.getData(auth, command.getGameID());
        if (sessions.get(auth) == null) {
            sendError(ctx, "you have no games");
        } else if (sessions.get(auth).type() == OBSERVER) {
            sendError(ctx, "you were just watching");
        } else if (data.whiteUsername() == null || data.blackUsername() == null) {
            sendError(ctx, "that would orphan the game!");
        } else {
            var stale = sessions.get(auth);
            gameService.resign(auth, stale.gameID());
            notifyClients(command.getGameID(), new ServerMessage(NOTIFICATION, user + " resigned! "), null);
            List<String> staleTokens = new ArrayList<>();
            sessions.forEach((token, UserConnection) -> {
                if (UserConnection.gameID() == stale.gameID()) {
                    staleTokens.add(token);
                }
            });
            for (String token : staleTokens) {
                sessions.remove(token);
            }
            stale.connection().closeSession();
        }
    }

    private void notifyClients(int gameID, ServerMessage message, String except) {
        sessions.forEach((auth, session) -> {
            var exclude = false;
            if (except != null) exclude = session.auth().equals(except) ;
            if (!exclude && session.gameID() == gameID && connections.contains(session.connection())) {
                System.out.println("sending to " + session.username() + " @ " + session.connection().sessionId());
                session.connection().send(new Gson().toJson(message));
            }
        });
    }

    private void sendError(WsContext ctx, String msg) {
        ctx.send(new Gson().toJson(new ServerMessage(ERROR, msg)));
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
