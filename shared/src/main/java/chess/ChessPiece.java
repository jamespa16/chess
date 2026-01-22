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
                    ChessPosition potential = new ChessPosition(y+j, x+i);
                    if (potential.getColumn() < 9 && potential.getColumn() > 0 && potential.getRow() < 9 && potential.getRow() > 0) {
                        ChessPiece obstacle = board.getPiece(potential);
                        if (obstacle == null || obstacle.getTeamColor() != color) {
                            newPos.add(potential);
                        }
                    }
                }
            }
            case QUEEN:
                break;
            case BISHOP:
                break;
            case KNIGHT:
                break;
            case ROOK:
                break;
            case PAWN:
                break;
        }
    }
}
