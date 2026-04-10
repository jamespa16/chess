package renderengine;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessGame.TeamColor;
import chess.ChessPiece;
import chess.ChessPosition;
import model.Notification;

import java.util.ArrayList;
import java.util.Collection;

public class RenderEngine {
    private final ArrayList<Notification> notifications = new ArrayList<>();
    private final int height = 28;
    private final int width = height * 16 / 9;
    private final int notificationWidth; //chess board is 44 char wide
    private final int spacing = 10;
    private final int notificationX = 44 + spacing;
    private ChessGame game = null;
    private ChessPosition highlightedPiece = null;

    public RenderEngine() {
        notificationWidth = width - 44 - spacing;
    }

    public void render(TeamColor color) {
        String[][] buffer = new String[height][width];

        renderFrame(buffer);
        renderBoard(color, buffer);
        renderNotifications(notificationWidth, notificationX, buffer);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(buffer[i][j]);
            }
        }
    }

    public void updateGame(ChessGame game) {
        this.game = game;
    }

    public void updateNotifications(Collection<Notification> notifications) {
        for (Notification notification : notifications) {
            if (!this.notifications.contains(notification)) {
                this.notifications.add(notification);
            }
        }
    }

    public void highlightPiece(ChessPosition position) {
        this.highlightedPiece = position;
    }

    private void renderFrame(String[][] buffer) {
        var top = "╭── CHESS ";
        for (int i = 0; i < width - top.length(); i++) {
            top += "─";
        }
        top += "╮";

        var bottom = "╰";
        for (int i = 0; i < width - 2; i++) {
            bottom += "─";
        }
        bottom += "╯";

        renderScanline(0, top, buffer);
        renderScanline(height - 1, bottom, buffer);

        for (int i = 1; i < height - 1; i++) {
            buffer[i][0] = "│";
            buffer[i][width - 1] = "│";
        }
    }

    private void renderBoard(TeamColor color, String[][] buffer) {
        String[] result = new String[26];
        var whiteFrame = "A ── B ── C ── D ── E ── F ── G ── H";
        var blackFrame = "H ── G ── F ── E ── D ── C ── B ── A";
        var colorFrame = whiteFrame;
        if (color == TeamColor.BLACK) {
            colorFrame = blackFrame;
        }

        var board = game.getBoard();
        result[0] = "╭── " + colorFrame + " ──╮";
        for (int y = 0; y < 9; y++) {
            for (int j = 0; j < 3; j++) {
                for (int x = 0; x < 9; x++) {
                    renderBoardCell(color, y, x, j, board, result);
                }
            }
        }
    }

    private void renderBoardCell(TeamColor color, int y, int x, int j, ChessBoard board, String[] result) {
        var line = "";
        var clear = "\u001b[49m";
        var squareColor = "\u001b[47m"; // white

        var squareDirection = 0;
        var row = (9 - y);
        var col = x;
        if (color == TeamColor.BLACK) {
            row = y;
            col = (9 - x);
            squareDirection = 1;
        }

        if ((x + y) % 2 == squareDirection) {
            squareColor = "\u001b[100m"; // black
        }

        var lineColor = clear;
        for (int i = 0; i < 5; i++) {
            if (col == 0) {
                if (i == 0 && j != 1) {
                    line += "│ ";
                } else if (i == 0) {
                    line += row + " ";
                }
            } else if (col == 9) {
                if (i == 2 && j != 1) {
                    line += " │\n";
                } else if (i == 2) {
                    line += " " + row + "\n";
                }
            } else {
                lineColor = squareColor;
                line += renderPiece(board.getPiece(new ChessPosition(row, col)));
            }
        }
        result[y + j] = lineColor + line + clear;
    }

    private String renderPiece(ChessPiece piece) {
        var cell = "";
        var pieceCode = 0;
        var pieceColor = "\u001b[29m";
        if (piece != null) {
            switch (piece.getPieceType()) {
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

    private void renderNotifications(int width, int x, String[][] buffer) {
        var top = "╭── NOTIFICATIONS ";
        for (int i = top.length(); i < width - 3; i++) {
            top += "─";
        }
        top += "──╮";
        renderScanline(0, top, buffer);

        for (int i = 0; i < height - 2; i++) {
            var line = "│";
            var notificationIndex = notifications.size() - i;
            if (notificationIndex > 0) {
                line += " [" +
                        notifications.get(notificationIndex).username() +
                        "] " + notifications.get(notificationIndex).message();
            }
            for (int j = 0; j < width - line.length() - 1; j++) {
                line += " ";
            }
            renderScanline(x, i + 2, line + "│", buffer);
        }
    }

    private void renderScanline(int yPos, String scanline, String[][] buffer) {
        renderScanline(0, yPos, scanline, buffer);
    }

    private void renderScanline(int xPos, int yPos, String scanline, String[][] buffer) {
        char[] decomposition = scanline.toCharArray();

        for (int i = 0; i < decomposition.length; i++) {
            buffer[yPos][i + xPos] = String.valueOf(decomposition[i]);
        }
    }
}