package network;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.function.Function;

import static chess.ChessGame.TeamColor.BLACK;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String url;

    public ServerFacade(String url) {
        this.url = url;
    }

    public AuthData login(String user, String password) throws Exception {
        var body = new Gson().toJson(new LoginRequest(user, password));
        var endpoint = "/session";
        var request = HttpRequest.newBuilder()
                .uri(new URI(url + endpoint))
                .timeout(java.time.Duration.ofMillis(5000))
                .POST(BodyPublishers.ofString(body)).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return new Gson().fromJson(response.body(), AuthData.class);
        }

        throw new Exception("Error " + response.statusCode() + ": " + response.body());
    }

    public AuthData register(String user, String email, String password) throws Exception {
        var body = new Gson().toJson(new UserData(user, password, email));
        var endpoint = "/user";
        var request = HttpRequest.newBuilder()
                .uri(new URI(url + endpoint))
                .timeout(java.time.Duration.ofMillis(5000))
                .POST(BodyPublishers.ofString(body)).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return new Gson().fromJson(response.body(), AuthData.class);
        }

        throw new Exception(response.statusCode() + ": " + response.body());
    }

    public void logout(String authToken) throws Exception {
        var endpoint = "/session";
        var request = HttpRequest.newBuilder()
                .uri(new URI(url + endpoint))
                .timeout(java.time.Duration.ofMillis(5000))
                .DELETE()
                .header("authorization", authToken)
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception(response.statusCode() + ": " + response.body());
        }
    }

    public int createGame(String name, String authToken) throws Exception {
        var body = new Gson().toJson(new GameRequest(name));
        var endpoint = "/game";
        var request = HttpRequest.newBuilder()
                .uri(new URI(url + endpoint))
                .timeout(java.time.Duration.ofMillis(5000))
                .POST(BodyPublishers.ofString(body))
                .header("authorization", authToken)
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Integer> result = new Gson().fromJson(response.body(), new TypeToken<Map<String, Integer>>() {
            }.getType());
            return result.get("gameID");
        }

        throw new Exception(response.statusCode() + ": " + response.body());
    }

    public GameReport listGames(String authToken) throws Exception {
        var response = httpRequestHelper("/game", (req) -> {
            return req.header("authorization", authToken).GET();
        });

        return new Gson().fromJson(response, GameReport.class);
    }

    private String httpRequestHelper(String endpoint, Function<Builder, Builder> details) throws Exception {
        var request = details.apply(HttpRequest.newBuilder()
                        .uri(new URI(url + endpoint))
                        .timeout(java.time.Duration.ofMillis(5000)))
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception(response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    public void joinGame(String authToken, int id, ChessGame.TeamColor color) throws Exception {
        var colorStr = "WHITE";
        if (color == BLACK) {
            colorStr = "BLACK";

        }
        var body = new Gson().toJson(new JoinRequest(colorStr, id));
        httpRequestHelper("/game", (req) -> {
            return req.PUT(BodyPublishers.ofString(body))
                    .header("authorization", authToken);
        });
    }

    public void deleteDB() throws Exception {
        httpRequestHelper("/db", (req) -> req.DELETE());
    }
}