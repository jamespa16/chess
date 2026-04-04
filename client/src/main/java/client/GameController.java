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
	private AuthData session;
	private ChessGame game;
	private String gameName;
	private Scanner scanner;
	private String color; 
	private Collection<Notification> notifications = new ArrayList<>();

	public GameController(AuthData session, GameData game, Scanner scanner, String color) {
		this.session = session;
		this.game = game.game();
		this.gameName = game.gameName();
		this.scanner = scanner;
		this.color = color;
	}



	public boolean gameScreen() {
        var perspective = TeamColor.WHITE;
        if (color.equals("BLACK")) {
            perspective = TeamColor.BLACK;
        }
        String selectedPiece = null;

        var engine = new RenderEngine();
		System.out.print("\u001b[H\u001b[2J");
		engine.addTopFrame();
		engine.renderBoard(game, perspective, notifications.toArray(new Notification[notifications.size()]), selectedPiece);
		engine.addBottomFrame();
		engine.render();
		System.out.printf("[" + gameName + "]" + " control >> ");
		var command = scanner.nextLine().trim();
		switch (command) {
			case "help":
				System.out.printf("available commands:\n"+
				" - help: brings up this help\n"+
				" - quit: exits the game screen\n"+
				" - redraw: refreshes board\n"+
				" - move: allows you to input a move\n"+
				" - resign: resigns from the game\n"+
				" - highlight: shows legal moves for the piece on that square");
				break;
			case "quit":
				return false;
			case "redraw":
				break;
			case "move":
				System.out.printf("which piece? >> ");
				var piece = parsePos(scanner.nextLine().trim());
				System.out.printf("to where? >> ");
				var destination = parsePos(scanner.nextLine().trim());
				break;
			case "resign":
				//resign(user);
				break;
			case "highlight":
				System.out.printf("which piece? >> ");
				selectedPiece = scanner.nextLine().trim();
				break;
		}

		return true;
    }

	private static ChessPosition parsePos(String input) {
		int row = input.codePointAt(0) - 'a' + 1;
		int col = input.codePointAt(1) - '0' + 1;
		return new ChessPosition(row, col);
	}
}

