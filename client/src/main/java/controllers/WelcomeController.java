package controllers;

import model.AuthData;
import network.HttpHelper;
import network.ServerFacade;

import java.util.Scanner;


public class WelcomeController {
    private final ServerFacade connection;
    private final Scanner scanner;

    public WelcomeController(ServerFacade connection) {
        this.connection = connection;
        this.scanner = new Scanner(System.in);
        System.out.println("  ╭─────╮ ╭─╮ ╭─╮ ╭─────╮ ╭─────╮ ╭─────╮  ");
        System.out.println("  │ ╭───╯ │ │ │ │ │ ╭───╯ │ ╭───╯ │ ╭───╯  ");
        System.out.println("  │ │     │ ╰─╯ │ │ │     │ ╰───╮ │ ╰───╮  ");
        System.out.println("  │ │     │ ╭─╮ │ │ ╰─╮   ╰───╮ │ ╰───╮ │  ");
        System.out.println("  │ ╰───╮ │ │ │ │ │   ╰─╮ ╭───╯ │ ╭───╯ │  ");
        System.out.println("  ╰─────╯ ╰─╯ ╰─╯ ╰─────╯ ╰─────╯ ╰─────╯  ");
        System.out.println("log in with 'login', or 'register' to play!");
    }

    public AuthData welcomeScreen() {
        while (true) {
            System.out.print("command >> ");
            String input = scanner.nextLine().trim();
            switch (input) {
                case "h":
                case "help":
                    System.out.println("You can get this help with 'help'\n" +
                            "- quit with 'quit'\n" +
                            "- login with 'login'\n" +
                            "- register with 'register'.");
                    break;
                case "q":
                case "quit":
                    System.out.println("goodbye 👋");
                    return null;
                case "login":
                    var session = loginScreen();
                    if (session != null) {
                        return session;
                    }
                    break;
                case "register":
                    var user = registerScreen();
                    if (user != null) {
                        return user;
                    }
                    break;
            }
        }
    }

    private AuthData loginScreen() {
        var attempting = true;
        while (attempting) {
            System.out.print("username >> ");
            var user = scanner.nextLine().trim();
            System.out.print("password >> ");
            var password = scanner.nextLine().trim();
            var auth = HttpHelper.serverRequestHandler(() -> connection.login(user, password));
            if (auth != null) {
                return auth;
            } else {
                attempting = ControllerHelper.tryAgain(scanner);
            }
        }
        return null;
    }

    private AuthData registerScreen() {
        var attempting = true;
        while (attempting) {
            System.out.print("username: >> ");
            var user = scanner.nextLine().trim();
            System.out.print("password: >> ");
            var password = scanner.nextLine().trim();
            System.out.print("email: >> ");
            var email = scanner.nextLine().trim();
            var auth = HttpHelper.serverRequestHandler(() -> connection.register(user, email, password));
            if (auth != null) {
                return auth;
            } else {
                System.out.print("as a result, registration failed! ");
                attempting = ControllerHelper.tryAgain(scanner);
            }
        }
        return null;
    }
}