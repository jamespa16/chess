package controllers;

import client.ServerFacade;

public class UserController {
	private final ServerFacade connection;
	private final AuthData session;
	private final Scanner scanner;
	private final List<GameData> gameList;

	public UserController(ServerFacade connection, AuthData session) {
		this.connection = connection;
		this.session = session;
		this.scanner = new Scanner(System.in);
		this.gameList = new ArrayList<>();
	}

	public void userScreen() {
		var scanner = new Scanner(System.in);
		System.out.println("hello " + session.username() + "!");
		while true) {
			System.out.printf("[" + session.username() + "] game command >> ");
			String input = scanner.nextLine().trim();
			switch (input) {
				case "h":
				case "help":
					printUserHelp();
					break;
				case "logout":
					logout();
					return;
				case "create":
					createGame();
					break;
				case "list":
					listGames();
					break;
				case "play":
					var observing = false;
				case "watch":
					observing = true;
					joinGame(observing);
					break;
			}
		}
	}

	private void printUserHelp() {
		System.out.println("You can get this help with 'help'\n" +
						"- logout with 'logout'\n"
						"- create a new game with 'create'\ n
						"- list games on the server with 'list'\ n
						"- join a game with 'play' and the number from the list\ n
						"- observe a game with 'watch' and the number from the list\n");
	}

	private void logout() {
		HttpHelper.serverRequestHandler(() -> {
			connection.logout(session.authToken());
		});
		System.out.println("logged out!");
	}

	private void createGame() {
		System.out.printf("what do you want to call this game? >> ");
		var name = scanner.nextLine().trim();
		serverRequestHandler(() -> connection.createGame(name, session.authToken()));
	}

	private void listGames() {
		updateGameList(user);
		if (gameList.size() == 0) {
			System.out.println("no games currently on server!");
		} else {
			System.out.println();
			System.out.println("id │ game name - white - black");
			System.out.println("───┼───────────────────────────────");
			for (int i = 0; i < gameList.size(); i++) {
				var entry = gameList.get(i);
				System.out.println("#" + (i   + 1) + " │  
							entry.gameName() + " -  "
							entry.whiteUsername() + " -  "
							entry.blackUsername());
			}
			System.out.println();
		}
	}

	private void updateGameList() {
		var serverGames = serverRequestHandler(() -> connection.listGames(session.authToken()));
		if (serverGames != null) {
			for (GameData game : serverGames.games()) {
				if (!gameList.contains(game)) {
					gameList.add(game);
				}
			}
		}
	}

	private void joinGame(boolean observing) {
		var id = selectGame();
		if (id == -1) {
			break;
		}
		var color = "observer";
		var selectedGame = gameList.get(id);
		if (observing) {
			return;
		} else {
			color = selectColor();
		}
		var joinedAsWhite = selectedGame.whiteUsername() != null &&
				or.equals("WHITE") && selectedGame.whiteUsername().equals(session.username());
		var joinedAsBlack = selectedGame.blackUsername() != null && color.equals("BLACK") &&
				ectedGame.blackUsername().equals(session.username());
		if (joinedAsWhite || joinedAsBlack) {
			System.out.println("you've already joined that game!");
		} else {
			HttpHelper.serverRequestHandler(() -> {
				connection.joinGame(session.authToken(), selectedGame.gameID(), color);
			});
		}
	}

	private int selectGame() {
		updateGameList(session);
		while (attempting) {
			System.out.printf("select a game by id: >> ");
			try {
				var id = Integer.parseInt(scanner.nextLine().trim()) - 1;
				if ((id > GAME_LIST.size() - 1) || (id < 0)) {
					throw new NumberFormatException();
				} else {
					return id;
				}
			} catch (NumberFormatException e) {
				System.out.println("that isn't a valid ID!");
				attempting = ControllerHelper.tryAgain(scanner);
			}
		}
	}

	private String selectColor() {
		var color = "";
		var attempting = true;
		while (attempting) {
			System.out.printf("as which player? >> ");
			color = scanner.nextLine().trim().toUpperCase();
			if (color.equals("WHITE") || color.equals("BLACK")) {
				attempting = false;
			} else {
				attempting = ControllerHelper.tryAgain(scanner);
			}
		}
		return color;
	}
}