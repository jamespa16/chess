package client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import RenderEngine.RenderEngine;
import chess.ChessGame;
import chess.ChessGame.TeamColor;
import chess.ChessMove;
import chess.ChessPosition;
import model.AuthData;
import model.GameData;
import model.Notification;

public class GameController {
	private Scanner scanner;
	private GameSocket connection;

	public GameController(ServerFacade connection, AuthData session, InitGameRequest game) {
		this.connection = connection.getSocket(new UserGameCommand(CONNECT, session.authToken(), game.gameID()));
	}

	public void gameScreen() {
		var perspective = TeamColor.WHITE;
		if (connection.color().equals("BLACK"))
			perspective = TeamColor.BLACK;

		var engine = new RenderEngine();

		while (true) {
			engine.updateGame(connection.getGame());
			engine.updateNotifications(connection.getNotifications());
			engine.render();

			var command = scanner.nextLine().trim();
			switch (command) {
				case "help":
					System.out.printf("available commands:\n" +
							" - help: brings up this help\n" +
							" - quit: exits the game screen\n" +
							" - redraw: refreshes board\n" +
							" - move: allows you to input a move\n" +
							" - resign: resigns from the game\n" +
							" - highlight: shows legal moves for the piece on that square");
					break;
				case "quit":
					return;
				case "redraw":
					break;
				case "move":
					var start = getPiece("which piece?");
					var destination = getPiece("to where?");
					connection.makeMove(new ChessMove(piece, destination));
					break;
				case "resign":
					// resign(user);
					break;
				case "highlight":
					var selection = getPiece("which piece?");
					engine.highlightPiece(selection);
					break;
			}
		}
	}

	private ChessPosition getPiece(String message) {
		System.out.printf(message + " >> ");
		return parsePos(scanner.nextLine().trim());
	}

	private static ChessPosition parsePos(String input) {
		int row = input.codePointAt(0) - 'a' + 1;
		int col = input.codePointAt(1) - '0' + 1;
		return new ChessPosition(row, col);
	}
}

