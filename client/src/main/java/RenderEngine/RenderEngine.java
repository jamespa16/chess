package RenderEngine;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.Notification;

public class RenderEngine {
    private final int WIDTH = 100;
    private String buffer = new String();

    public RenderEngine() {

    }

    public void render() {
        System.out.printf(buffer);
        buffer = "";
    }

    public void addScanline(String line) {
        buffer += "│" + line;
        for (int i = line.length(); i < WIDTH - 2; i++){
            buffer += " ";
        }
        buffer += "│\n";
    }


    public void addTopFrame() {
        buffer += "╭── CHESS ";
        for (int i = 0; i < WIDTH - 11; i++) {
            buffer += "─";
        }
        buffer += "╮\n";
    }

    public void addBottomFrame() {
        buffer += "╰";
        for (int i = 0; i < WIDTH - 2; i++) {
            buffer += "─";
        }
        buffer += "╯\n";
    }

    public void renderBoard(ChessGame game, ChessGame.TeamColor perspective, Notification[] notifications, String selectedPiece) {
        ChessPosition selectedPosition = parsePosition(selectedPiece);
        var whiteFrame = "A ── B ── C ── D ── E ── F ── G ── H";
        var blackFrame = "H ── G ── F ── E ── D ── C ── B ── A";

        var colorFrame = whiteFrame;
        if (perspective == ChessGame.TeamColor.BLACK) {
            colorFrame = blackFrame;
        }

        var board = game.getBoard();
        var topLine = " ╭── " + colorFrame + " ──╮    ╭── NOTIFICATIONS ";
        for (int i = topLine.length(); i < WIDTH - 3; i++) {
            topLine += "─";
        }
        topLine += "╮";
        addScanline(topLine);
        for (int y = 1; y < 9; y++) {
            for (int j = 0; j < 3; j++) {
                var line = "";
                for (int x = 0; x < 10; x++) {
                    if (perspective == ChessGame.TeamColor.WHITE) {
                        var selected = false;
                        var pos = new ChessPosition((9-y), x);
                        if (pos == selectedPosition) {
                            selected = true;
                        }
                        var piece = board.getPiece(pos);
                        line += renderBoardSquare(x, (9-y), j, piece, perspective, selected);
                    } else {
                        var selected = false;
                        var pos = new ChessPosition(y, (9 - x));
                        if (pos == selectedPosition) {
                            selected = true;
                        }
                        var piece = board.getPiece(pos);
                        line += renderBoardSquare(x, y, j, piece, perspective, selected);
                    }
                }
                line += "    ";
                var lengthRemaining = WIDTH - 33;
                line += renderNotifications(y, j, notifications, lengthRemaining);
                addScanline(line);
            }
        }
        var bottomLine = " ╰── " + colorFrame + " ──╯    ╰";
        for (int i = bottomLine.length(); i < WIDTH - 3; i++) {
            bottomLine += "─";
        }
        bottomLine += "╯";
        addScanline(bottomLine);
    }

    private String renderBoardSquare(int x, int y, int j, ChessPiece piece, ChessGame.TeamColor perspective, boolean selected) {
        var line = "";
        var clear = "\u001b[49m";
        var color = "\u001b[47m"; // white

        var squareDirection = 0;
        if (perspective == ChessGame.TeamColor.BLACK) {
            squareDirection = 1;
        }

        if ((x+y) % 2 == squareDirection) {
            color = "\u001b[100m"; // black
        }

        var lineColor = clear;
        for (int i = 0; i < 5; i++) {
            if (x == 0) {
                if (i == 0 && j != 1) {
                    line += " │ ";
                } else if (i == 0) {
                    line += " " + y + " ";
                }
            } else if (x == 9) {
                if (i == 2 && j != 1) {
                    line += " │";
                } else if (i == 2) {
                    line += " " + y;
                }
            } else {
                lineColor = color;
                line += renderPiece(i, j, piece);
            }
        }
        return lineColor + line + clear;
    }


    private String renderPiece(int i, int j, ChessPiece piece) {
        var cell = "";
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
            if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                pieceCode += 6;
                pieceColor = "\u001b[30m";
            }
        }
        cell += pieceColor;
        switch (pieceCode) {
            case 0:
                cell += " ";
                break;
            case 1:
                cell += "♙";
                break;
            case 2:
                cell += "♖";
                break;
            case 3:
                cell += "♗";
                break;
            case 4:
                cell += "♘";
                break;
            case 5:
                cell += "♕";
                break;
            case 6:
                cell += "♔";
                break;
            case 7:
                cell += "♟";
                break;
            case 8:
                cell += "♜";
                break;
            case 9:
                cell += "♝";
                break;
            case 10:
                cell += "♞";
                break;
            case 11:
                cell += "♛";
                break;
            case 12:
                cell += "♚";
                break;
        }
        cell += "\u001b[39m";
        return cell;
    }

    private String renderNotifications(int y, int j, Notification[] notifications, int lengthRemaining) {
        var line = "│";
        var notificationIndex = notifications.length - y;
        if (notificationIndex > -1 && j == 1) {
            line += " [" + notifications[notificationIndex].username() + "] " + notifications[notificationIndex].message();
        }
        for (int i = 0; i < lengthRemaining - line.length(); i++) {
            line += " ";
        }
        return line + "│";
    }

    private ChessPosition parsePosition(String pos) {
        return new ChessPosition(1,1);
    }
}
