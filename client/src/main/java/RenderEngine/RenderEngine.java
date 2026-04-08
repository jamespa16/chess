package RenderEngine;

import chess.ChessGame;
import chess.ChessGame.TeamColor;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import model.Notification;

import java.util.ArrayList;

public class RenderEngine {
    private GameData game = null;
    private final ArrayList<Notification> notifications = new ArrayList<>();
    private ChessPosition highlightedPiece = null;
    private int height = 28;
    private int width = height * 16 / 9;
    private int spacing = 10;
    private int notificationWidth = width - 44 - spacing; //chess board is 44 char wide
    private int notificationX = 44 + spacing;

    public RenderEngine() {}

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

        System.out.printf("[ "+game.gameName()+" ] game control >>");
    }

    public void updateGame(GameData game) {
        this.game = game;
    }

    public void updateNotifications(Notification[] notifications) {
        for (Notification notification : notifications) {
            if (!this.notifications.contains(notification)) this.notifications.add(notification);
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

        var board = game.game().getBoard();
        result[0] = "╭── " + colorFrame + " ──╮";
        for (int y = 0; y < 9; y++) {
            for (int j = 0; j < 3; j++) {
                for (int x = 0; x < 9; x++) {
                    var line = "";
                    var clear = "\u001b[49m";
                    var squareColor = "\u001b[47m"; // white

                    var squareDirection = 0;
                    var row = (9-y);
                    var col = x;
                    if (color == TeamColor.BLACK) {
                        row = y;
                        col = (9-x);
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
                    result[y+j] = lineColor + line + clear;
                }
            }
        }
    }

    private String renderPiece(ChessPiece piece) {
        var cell = "";
        var pieceCode = 0;
        var pieceColor = "\u001b[29m";
        if (piece != null) {
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
            if (notificationIndex > -1) {
                line += " [" +
                        notifications.get(notificationIndex).username() +
                        "] " + notifications.get(notificationIndex).message();
            }
            for (int j = 0; j < width - line.length() - 1; j++) {
                line += " ";
            }
            renderScanline(x, i+2, line + "│", buffer);
        }
    }

    private void renderScanline(int yPos, String scanline, String[][] buffer) {
        renderScanline(0, yPos, scanline, buffer);
    }

    private void renderScanline(int xPos, int yPos, String scanline, String[][] buffer) {
        char[] decomposition = scanline.toCharArray();

        for (int i = 0; i < decomposition.length; i++) {
            buffer[yPos][i+xPos] = String.valueOf(decomposition[i]);
        }
    }
}

    /* get recked llol
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
     */