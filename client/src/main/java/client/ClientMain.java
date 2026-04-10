package client;

import controllers.GameController;
import controllers.UserController;
import controllers.WelcomeController;
import network.GameSocket;
import network.ServerFacade;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        var server = new ServerFacade("http://127.0.0.1:8080");
        var welcome = new WelcomeController(server);
        var session = welcome.welcomeScreen();
        while (session != null) {
            var game = new UserController(server, session).userScreen();
            if (game == null) {
                session = welcome.welcomeScreen();
            } else {
                var gameSocket = new GameSocket("ws://127.0.0.1:8080/ws", session.authToken(), game.gameID());
                new GameController(gameSocket, "ws://127.0.0.1:8080/ws").gameScreen(game.color(), game.gameName());
            }
        }
    }
}


