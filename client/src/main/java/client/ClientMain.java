package client;

import model.AuthData;
import controllers.WelcomeController;
import controllers.UserController;
import controllers.GameController;
import network.ServerFacade;

public class ClientMain {
    public static void main(String[] args) {
        var server = new ServerFacade("http://127.0.0.1:8080");
        var welcome = new WelcomeController(server);
        var session = welcome.welcomeScreen();
        while (session != null) {
            var game = new UserController(server, session).userScreen();
            if (game == -1) session = welcome.welcomeScreen();
            new GameController(server, session, game).gameScreen();
        }
    }
}


