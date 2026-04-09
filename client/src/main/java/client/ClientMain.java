package client;

import jakarta.websocket.ContainerProvider;
import model.AuthData;
import controllers.WelcomeController;
import controllers.UserController;
import controllers.GameController;
import network.GameSocket;
import network.ServerFacade;

import java.net.URI;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        var server = new ServerFacade("http://127.0.0.1:8080");
        var welcome = new WelcomeController(server);
        var session = welcome.welcomeScreen();
        while (session != null) {
            var game = new UserController(server, session).userScreen();
            if (game == null) session = welcome.welcomeScreen();
            var gameSocket = new GameSocket("ws://127.0.0.1:8080/ws");
            new GameController(gameSocket).gameScreen(game.color(), game.gameName());
        }
    }
}


