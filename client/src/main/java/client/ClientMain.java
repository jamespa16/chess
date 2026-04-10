package client;

import controllers.GameController;
import controllers.UserController;
import controllers.WelcomeController;
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
                new GameController("ws://127.0.0.1:8080/ws", session.authToken(), game.gameID()).gameScreen(game.color(), game.gameName());
            }
        }
    }
}


