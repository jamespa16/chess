package client;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessGame.TeamColor;

public class Renderer {
        public static void render(ChessGame game, TeamColor perspective) {
            var whiteFrame = "A ── B ── C ── D ── E ── F ── G ── H";
            var blackFrame = "H ── G ── F ── E ── D ── C ── B ── A";

            var colorFrame = whiteFrame;
            if (perspective == TeamColor.BLACK) {
                colorFrame = blackFrame;
            }

            var board = game.getBoard();
            for (int y = 0; y < 10; y++) {
                if (y == 0) {
                    System.out.println("╭── " + colorFrame + " ──╮");
                } else if (y == 9) {
                    System.out.println("╰── " + colorFrame + " ──╯");
                } else {
                    for (int j = 0; j < 3; j++) {
                        for (int x = 0; x < 10; x++) {
                            if (perspective == TeamColor.WHITE) {
                                renderLine(x, (9-y), j, board.getPiece(new ChessPosition((9-y), x)));
                            } else {
                                renderLine(x, y, j, board.getPiece(new ChessPosition(y, x)));
                            }
                            
                        }
                    }
                }
            }
        }


    private static void renderLine(int x, int y, int j, ChessPiece piece) {
        var line = "";
        var clear = "\u001b[49m";
        var color = "\u001b[47m"; // white
        if ((x+y) % 2 == 1) {
            color = "\u001b[100m"; // black
        }

        var lineColor = clear;
        for (int i = 0; i < 5; i++) {
                if (x == 0) { 
                    if (i == 0 && j != 1) {
                        line += "│ ";
                    } else if (i == 0) {
                        line += y + " ";
                    }
                } else if (x == 9) {
                    if (i == 2 && j != 1) {
                        line += " │\n";
                    } else if (i == 2) {
                        line += " " + y + "\n";
                    }
                } else {
                    lineColor = color;
                var pieceCode = 0;
                var pieceColor = "\u001b[29m";
                if (i == 2 && j == 1 && piece != null) {
                    switch(piece.getPieceType()) {
                        case PAWN:
                            pieceCode = 1;
                            break;
                        case ROOK:
                            pieceCode = 2;
                            break;
                        case BISHOP:
                            pieceCode = 3;
                            break;
                        case KNIGHT:
                            pieceCode = 4;
                            break;
                        case QUEEN:
                            pieceCode = 5;
                            break;
                        case KING:
                            pieceCode = 6;
                            break;
                    }
                    if (piece.getTeamColor() == TeamColor.BLACK) {
                        pieceCode += 6;
                        pieceColor = "\u001b[30m";
                    }
                }
                
                line += pieceColor;
                switch (pieceCode) {
                    case 0:
                        line += " ";
                        break;
                    case 1:
                        line += "♙";
                        break;
                    case 2:
                        line += "♖";
                        break;
                    case 3:
                        line += "♗";
                        break;
                    case 4:
                        line += "♘";
                        break;
                    case 5:
                        line += "♕";
                        break;
                    case 6:
                        line += "♔";
                        break;
                    case 7:
                        line += "♟";
                        break;
                    case 8:
                        line += "♜";
                        break;
                    case 9:
                        line += "♝";
                        break;
                    case 10:
                        line += "♞";
                        break;
                    case 11:
                        line += "♛";
                        break;
                    case 12:
                        line += "♚";
                        break;
                }
                line += "\u001b[39m";
                
            }
            }
        System.out.printf(lineColor);
        System.out.printf(line);
        System.out.printf(clear);
    }
}
