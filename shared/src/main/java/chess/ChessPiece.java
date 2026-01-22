package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor color;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        color = pieceColor;
        this.type = type;

    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        int x = myPosition.getColumn();
        int y = myPosition.getRow();
        List<ChessPosition> newPos = new ArrayList<>();

        int endOfBoard = 8;
        int direction = 1;
        if (color == ChessGame.TeamColor.BLACK) {
            endOfBoard = 1;
            direction = -1;
        }

        switch (type) {
            case KING: // can always move in a square around it
                for (int i = -1; i < 2; i++) {
                    for (int j = -1; j < 2; j++) {
                        if (i != y || j != x) {
                            newPos.add(new ChessPosition(y+i, x+j));
                        }
                    }
                }
            case QUEEN: // can move in a diagonal / straight up to a piece.
                rowMovementHelper(board, x, y, newPos);
                colMovementHelper(board, x, y, newPos);
                diagonalMovementHelper(board, x, y, newPos);
                // row - a == y && col - a == x
            case BISHOP:
                diagonalMovementHelper(board, x, y, newPos);
            case KNIGHT:
                int[][] potentialMoves = {
                        {x + 2, y + 1},
                        {x + 2, y - 1},
                        {x - 2, y + 1},
                        {x - 2, y - 1},
                        {x + 1, y + 2},
                        {x + 1, y - 2},
                        {x - 1, y + 2},
                        {x - 1, y - 2}
                };

                for (int[] move : potentialMoves) {
                    if (move[0] > 0 && move[0] < 9) {
                        newPos.add(new ChessPosition(move[1], move[0]));
                    }
                }
            case ROOK:
                rowMovementHelper(board, x, y, newPos);
                colMovementHelper(board, x, y, newPos);
            case PAWN:
                int startingRow = 2;
                if (color == ChessGame.TeamColor.BLACK) {
                    startingRow = 7;
                }
                if (y == endOfBoard) {
                    break;
                } else {
                    newPos.add(new ChessPosition(y + direction, x));
                    if (y == startingRow) {
                        newPos.add(new ChessPosition(y + (2 * direction), x));
                    }
                }

                ChessPosition[] attackPositions = {new ChessPosition(y+direction, x+1), new ChessPosition(y+direction, x-1)};
                for (ChessPosition attack : attackPositions) {
                    ChessPiece potentialAttack = board.getPiece(attack);
                    if (potentialAttack != null && potentialAttack.color != this.color) {
                        newPos.add(attack);
                    }
                }
        }

        List<ChessMove> newMoves = new ArrayList<>();
        for (ChessPosition pos : newPos) {
            if (newPos.get(1).getRow() == endOfBoard) {
                newMoves.add(new ChessMove(myPosition, pos, PieceType.QUEEN));
                newMoves.add(new ChessMove(myPosition, pos, PieceType.BISHOP));
                newMoves.add(new ChessMove(myPosition, pos, PieceType.KNIGHT));
                newMoves.add(new ChessMove(myPosition, pos, PieceType.ROOK));
            } else {
                newMoves.add(new ChessMove(myPosition, pos, null));
            }

        }

        return newMoves;
    }

    private void colMovementHelper(ChessBoard board, int x, int y, List<ChessPosition> newPos) {
        colDownMovementHelper(board, x, y, newPos);
        colUpMovementHelper(board, x, y, newPos);
    }

    private void colUpMovementHelper(ChessBoard board, int x, int y, List<ChessPosition> newPos) {
        for (int i = 1; y + i > 9; i++) {
            var potential = new ChessPosition(y + i, x);
            if (board.getPiece(potential) != null) {
                break;
            } else {
                newPos.add(potential);
            }
        }
    }

    private void colDownMovementHelper(ChessBoard board, int x, int y, List<ChessPosition> newPos) {
        for (int i = 1; y - i > 0; i++) {
            var potential = new ChessPosition(y - i, x);
            if (board.getPiece(potential) != null) {
                break;
            } else {
                newPos.add(potential);
            }
        }
    }

    private void rowLeftMovementHelper(ChessBoard board, int x, int y, List<ChessPosition> newPos) {
        for (int i = 1; x - i > 0; i++) {
            var potential = new ChessPosition(y, x - i);
            if (board.getPiece(potential) != null) {
                break;
            } else {
                newPos.add(potential);
            }
        }
    }

    private void rowRightMovementHelper(ChessBoard board, int x, int y, List<ChessPosition> newPos) {
        for (int i = 1; x + i > 9; i++) {
            var potential = new ChessPosition(y, x + i);
            if (board.getPiece(potential) != null) {
                break;
            } else {
                newPos.add(potential);
            }
        }
    }

    private void rowMovementHelper(ChessBoard board, int x, int y, List<ChessPosition> newPos) {
        rowLeftMovementHelper(board, x, y, newPos);
        rowRightMovementHelper(board, x, y, newPos);
    }

    private void diagonalMovementHelper(ChessBoard board, int x, int y, List<ChessPosition> newPos) {
        boolean upLeft = true;
        boolean upRight = true;
        boolean downLeft = true;
        boolean downRight = true;
        for(int i = 0; i < 9; i++) {
            if(i + x < 9 && i + y < 9 && upRight) {
                ChessPosition potential = new ChessPosition(y+i, x+i);
                if (board.getPiece(potential) == null) {
                    newPos.add(potential);
                } else {
                    upRight = false;
                }
            }

            if(i + x < 9 && y - i > 0 && downRight) {
                ChessPosition potential = new ChessPosition(y-i, x+i);
                if (board.getPiece(potential) == null) {
                    newPos.add(potential);
                } else {
                    downRight = false;
                }
            }

            if(x - i > 0 && i + y < 9 && upLeft) {
                ChessPosition potential = new ChessPosition(y+i, x-i);
                if (board.getPiece(potential) == null) {
                    newPos.add(potential);
                } else {
                    upLeft = false;
                }
            }

            if(x - i > 0 && y - i > 0 && downLeft) {
                ChessPosition potential = new ChessPosition(y-i, x-i);
                if (board.getPiece(potential) == null) {
                    newPos.add(potential);
                } else {
                    downLeft = false;
                }
            }
        }
    }
}
