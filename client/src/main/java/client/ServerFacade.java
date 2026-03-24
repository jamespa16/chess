package client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.util.Collection;

import com.google.gson.Gson;

import model.AuthData;
import model.GameData;
import model.LoginRequest;

public class ServerFacade {
    private String url;
    private static final HttpClient client = HttpClient.newHttpClient();

    public ServerFacade(String url){
        this.url = url;
    }

    public AuthData login(String user, String password) {
        try {
            var body = new Gson().toJson(new LoginRequest(user, password));
            var request = requestHandler("/session", null).POST(BodyPublishers.ofString(body)).build();
            return httpHandler(request, AuthData.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public AuthData register(String user, String password) {
        try {
            var body = new Gson().toJson(new LoginRequest(user, password));
            var request = requestHandler("/user", null).POST(BodyPublishers.ofString(body)).build();
            return httpHandler(request, AuthData.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void logout(String authToken) {
        try {
            var request = requestHandler("/session", authToken).DELETE().build();
            httpHandler(request, null);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void createGame(String name, String authToken){
        try {
            var request = requestHandler("/game", authToken).POST(BodyPublishers.ofString(name)).build();
            httpHandler(request, null);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Collection<GameData> listGames(String authToken) {
        try {
            var request = requestHandler("/game", authToken).GET().build();
            return httpHandler(request, null);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public GameData joinGame(String authToken, String id) {
        try {
            var request = requestHandler("/game", authToken).PUT(BodyPublishers.ofString(id)).build();
            return httpHandler(request, null);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private <T> T httpHandler(HttpRequest request, Class<T> responseType) throws Exception {
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return new Gson().fromJson(response.body(), responseType);
        }

        throw new Exception("Error " + response.statusCode() + ": " + response.body());
    }

    private Builder requestHandler(String endpoint, String authToken) throws Exception {
        return HttpRequest.newBuilder()
        .uri(new URI(url + endpoint))
        .timeout(java.time.Duration.ofMillis(5000))
        .header("authToken", authToken);
    }
}