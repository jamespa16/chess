package chess;

import java.util.*;

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
            case KING: // can always move in a square around it, except for squares occupied by other same-team pieces
                for (int i = -1; i < 2; i++) {
                    for (int j = -1; j < 2; j++) {
                        ChessPosition potential = new ChessPosition(y+j, x+i);
                        if (potential.getColumn() < 9 && potential.getColumn() > 0 && potential.getRow() < 9 && potential.getRow() > 0) {
                            ChessPiece obstacle = board.getPiece(potential);
                            if (obstacle == null || obstacle.getTeamColor() != color) {
                                newPos.add(potential);
                            }
                        }
                    }
                }
                break;
            case QUEEN: // can move in a diagonal / straight up to a piece.
                colRowMovementHelper(board, x, y, newPos);
                diagonalMovementHelper(board, x, y, newPos);
                // row - a == y && col - a == x
                break;
            case BISHOP: // can move in a diagonal up to a piece
                diagonalMovementHelper(board, x, y, newPos);
                break;
            case KNIGHT: // can move in an L shape, over pieces. presently hard-coded.
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
                    ChessPosition potentialMove = new ChessPosition(move[1], move[0]);
                    ChessPiece potentialObstacle = board.getPiece(potentialMove);
                    boolean isNotObstacle = potentialObstacle == null || potentialObstacle.getTeamColor() != color;
                    boolean isInBounds = move[0] < 9 && move[0] > 0 && move[1] < 9 && move[1] > 0;
                    if (isNotObstacle && isInBounds) {
                        newPos.add(potentialMove);
                    }
                }
                break;
            case ROOK: // can move horizontally or vertically up to a piece.
                colRowMovementHelper(board, x, y, newPos);
                break;
            case PAWN: // can move one square forwards, except on starting row, where it can move two. attacks to the side.
                int startingRow = 2;
                if (color == ChessGame.TeamColor.BLACK) {
                    startingRow = 7;
                }
                if (y == endOfBoard) {
                    break;
                } else {
                    ChessPosition potential = new ChessPosition(y + direction, x);
                    if (board.getPiece(potential) == null) {
                        newPos.add(potential);
                        if (y == startingRow) {
                            ChessPosition secondPotential = new ChessPosition(y + 2 * direction, x);
                            if (board.getPiece(secondPotential) == null) {
                                newPos.add(secondPotential);
                            }
                        }
                    }
                }
                ChessPosition[] attackPositions = {new ChessPosition(y+direction, x+1), new ChessPosition(y+direction, x-1)};
                for (ChessPosition attack : attackPositions) {
                    ChessPiece potentialAttack = board.getPiece(attack);
                    if (potentialAttack != null && potentialAttack.color != this.color) {
                        newPos.add(attack);
                    }
                }
                break;
        }

        Set<ChessMove> newMoves = new HashSet<>();
        for (ChessPosition pos : newPos) {
            if (pos.getRow() == endOfBoard && this.getPieceType() == PieceType.PAWN) {
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

    /**
     * Determines if a square is unoccupied (or has an opposing team piece on it), and if so, adds the square to potential moves.
     * @param board the board to consider
     * @param newPos the list of potential positions to add to.
     * @param direction a true/false reference to whether the current direction is obstructed
     * @param potential the square to consider
     * @return if the square poses an obstacle.
     */
    private boolean isUnobstructed(ChessBoard board, List<ChessPosition> newPos, boolean direction, ChessPosition potential) {
        ChessPiece obstacle = board.getPiece(potential);
        if (obstacle != null && obstacle.getTeamColor() != color) {
            newPos.add(potential);
            direction = false;
        } else if (obstacle != null) {
            direction = false;
        } else {
            newPos.add(potential);
        }
        return direction;
    }

    /**
     * Iterates through the possible positions in horizontal and vertical directions, like that of the Rook or Queen.
     * Starts at the piece, and then moves outward in a circle to check squares. Each valid square gets added to the list.
     * @param board the board to consider
     * @param x the column value of the current piece
     * @param y the row value of the current piece
     * @param newPos the list of possible moves to add to.
     */
    private void colRowMovementHelper(ChessBoard board, int x, int y, List<ChessPosition> newPos) {
        boolean up = true;
        boolean down = true;
        boolean left = true;
        boolean right = true;

        for (int i = 1; i < 9; i++) {
            if (x+i < 9 && up) {
                ChessPosition potential = new ChessPosition(y, x + i);
                up = isUnobstructed(board, newPos, up, potential);
            }

            if (x-i > 0 && down) {
                ChessPosition potential = new ChessPosition(y, x - i);
                down = isUnobstructed(board, newPos, down, potential);
            }

            if (y+i < 9 && right) {
                ChessPosition potential = new ChessPosition(y+i, x);
                right = isUnobstructed(board, newPos, right, potential);
            }

            if (y-i > 0 && left) {
                ChessPosition potential = new ChessPosition(y-i, x);
                left = isUnobstructed(board, newPos, left, potential);
            }
        }
    }

    /**
     * Iterates through the possible positions in diagonal directions, like that of the Bishop or Queen.
     * Starts at the piece, and then moves outwards in a circle to check squares. Each valid square gets added to the list.
     * @param board the board to consider
     * @param x the column value of the current piece
     * @param y the row value of the current piece
     * @param newPos the list of possible moves to add to.
     */
    private void diagonalMovementHelper(ChessBoard board, int x, int y, List<ChessPosition> newPos) {
        boolean upLeft = true;
        boolean upRight = true;
        boolean downLeft = true;
        boolean downRight = true;

        for(int i = 1; i < 9; i++) {
            if(i + x < 9 && i + y < 9 && upRight) {
                ChessPosition potential = new ChessPosition(y+i, x+i);
                upRight = isUnobstructed(board, newPos, upRight, potential);
            }

            if(i + x < 9 && y - i > 0 && downRight) {
                ChessPosition potential = new ChessPosition(y-i, x+i);
                downRight = isUnobstructed(board, newPos, downRight, potential);
            }

            if(x - i > 0 && i + y < 9 && upLeft) {
                ChessPosition potential = new ChessPosition(y+i, x-i);
                upLeft = isUnobstructed(board, newPos, upLeft, potential);
            }

            if(x - i > 0 && y - i > 0 && downLeft) {
                ChessPosition potential = new ChessPosition(y-i, x-i);
                downLeft = isUnobstructed(board, newPos, downLeft, potential);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ChessPiece that) {
            return this.getPieceType() == that.getPieceType() && this.getTeamColor() == that.getTeamColor();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getPieceType().hashCode() + getTeamColor().hashCode();
    }
}
