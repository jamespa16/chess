package controllers;

import client.ServerFacade;

public class WelcomeController {
	private final ServerFacade connection;

	public WelcomeController(ServerFacade connection) {
		this.connection = connection;
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
			System.out.printf("command >> ");
			Scanner scanner = new Scanner(System.in);
			String input = scanner.nextLine().trim();
			switch (input) {
				case "h":
				case "help":
					System.out.println("You can get this help with 'help'\n" +
									"- quit with 'quit'\n"
									"- login with 'login'\ n
									"- register with 'register'.");
					break;
				case "q":
				case "quit":
					System.out.println("goodbye 👋");
					return null;
				case "login":
					var session = loginScreen(scanner);
					if (session != null) {
						return session;
					}
					break;
				case "register":
					var user = registerScreen(scanner);
					if (user != null) {
						return user;
					}
					break;
			}
		}
	}

	private AuthData loginScreen(Scanner scanner) {
		var attempting = true;
		while (attempting) {
			System.out.printf("username >> ");
			var user = scanner.nextLine().trim();
			System.out.printf("password >> ");
			var password = scanner.nextLine().trim();
			var auth = serverRequestHandler(() -> connection.login(user, password));
			if (auth != null) {
				return auth;
			} else {
				attempting = tryAgainScreen(scanner, attempting);
			}
		}
		return null;
	}

	private AuthData registerScreen(Scanner scanner) {
		var attempting = true;
		while (attempting) {
			System.out.printf("username: >> ");
			var user = scanner.nextLine().trim();
			System.out.printf("password: >> ");
			var password = scanner.nextLine().trim();
			System.out.printf("email: >> ");
			var email = scanner.nextLine().trim();
			var auth = serverRequestHandler(() -> connection.register(user, email, password));
			if (auth != null) {
				return auth;
			} else {
				System.out.printf("as a result, registration failed! ");
				attempting = tryAgainScreen(scanner, attempting);
			}
		}
		return null;
	}
}