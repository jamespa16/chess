package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import chess.ChessPiece.PieceType;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor currentTurn;
    private boolean whiteCanCastle = false;
    private boolean blackCanCastle = false;

    public ChessGame() {
        board = new ChessBoard();
        currentTurn = TeamColor.WHITE;
        whiteCanCastle = true;
        blackCanCastle = true;

        board.resetBoard();
    }

    public ChessGame(ChessGame other) {
        this.board = new ChessBoard(other.board);
        this.currentTurn = other.getTeamTurn();
        this.whiteCanCastle = other.whiteCanCastle;
        this.blackCanCastle = other.blackCanCastle;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessGame chessGame)) {
            return false;
        }
        return whiteCanCastle == chessGame.whiteCanCastle && blackCanCastle == chessGame.blackCanCastle && Objects.equals(getBoard(), chessGame.getBoard()) && currentTurn == chessGame.currentTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBoard(), currentTurn, whiteCanCastle, blackCanCastle);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> logicalMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new HashSet<>();
        for(ChessMove move : logicalMoves) {
            if (simulateMove(move)){
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    private boolean simulateMove(ChessMove move) {
        ChessGame future = new ChessGame(this);
        try {
            future.makeMove(move);
            return true;
        } catch (InvalidMoveException e) {
            return false;
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);

        if (piece == null) {
            throw new InvalidMoveException("piece to move does not exist");
        }

        if (currentTurn != piece.getTeamColor()) {
            throw new InvalidMoveException("move is out of turn");
        }

        Collection<ChessMove> potentialMoves = piece.pieceMoves(board, start);
        if (!potentialMoves.contains(move)) {
            throw new InvalidMoveException("move is invalid");
        }

        board.movePiece(start, end);
        if (move.getPromotionPiece() != null) {
            board.addPiece(end, new ChessPiece(currentTurn, move.getPromotionPiece()));
        }

        if (isInCheck(currentTurn)) {
            throw new InvalidMoveException("move results in check");
        }

        if(currentTurn == TeamColor.WHITE) {
            currentTurn = TeamColor.BLACK;
        } else {
            currentTurn = TeamColor.WHITE;
        }
    }

    private boolean[] checkSquare(TeamColor teamColor, ChessBoard board, int y, int x, boolean diagonals) {
        boolean[] results = {false, false};
        ChessPiece potentialAttacker = board.getPiece(new ChessPosition(x, y));
        if (potentialAttacker != null) {
            if (potentialAttacker.getTeamColor() != teamColor) {
                PieceType attackerType = potentialAttacker.getPieceType();
                if (attackerType == PieceType.QUEEN) {
                    results[0] = true;
                } else if (diagonals && attackerType == PieceType.BISHOP) {
                    results[0] = true;
                } else {
                    if (attackerType == PieceType.ROOK) {
                        results[0] = true;
                    }
                }
            } else {
                results[1] = true;
            }
        }
        return results;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        /* PSEUDO CODE
        idea: check each movement type from the king's square for attackers
        1. check the two diagonals for opposing pawns / beyond for bishops & queens
        2. check the rows for rooks & queens
        3. check the Ls for knights

        this can basically reuse the movement logic, but modify it to return TRUE if an opposing piece is found
         */
        boolean check = false;

        ChessPosition kingLocation = getKingPosition(teamColor);
        if (kingLocation == null) {
            return true; // this means the simulator attacked the other king successfully
        }

        int king_x = kingLocation.getColumn();
        int king_y = kingLocation.getRow();
        
        int direction = -1;
        if (teamColor == ChessGame.TeamColor.BLACK) {
            direction = 1;
        }

        // check for pawns
        ChessPosition rightPawnAttack = new ChessPosition(king_x + direction, king_y + 1);
        ChessPosition leftPawnAttack = new ChessPosition(king_x + direction, king_y - 1);
        ChessPiece potentialRightPawn = board.getPiece(rightPawnAttack);
        ChessPiece potentialLeftPawn = board.getPiece(leftPawnAttack);
        if (potentialRightPawn != null && potentialRightPawn.getTeamColor() != teamColor && potentialRightPawn.getPieceType() == ChessPiece.PieceType.PAWN){
            check = true;
        }
        if (!check && potentialLeftPawn != null && potentialLeftPawn.getTeamColor() != teamColor && potentialLeftPawn.getPieceType() == ChessPiece.PieceType.PAWN){
            check = true;
        }

        // set up booleans for checking pieces
        boolean upLeftBlocked = false;
        boolean upRightBlocked = false;
        boolean downLeftBlocked = false;
        boolean downRightBlocked = false;

        boolean upBlocked = false;
        boolean downBlocked = false;
        boolean leftBlocked = false;
        boolean rightBlocked = false;

        for (int i = 0; i < 9 && !check; i++) {
            // check for bishops & queens on the diagonal
            if (i + king_x < 9 && i + king_y < 9 && !upRightBlocked) {
                boolean[] results = checkSquare(teamColor, getBoard(), i + king_x, i+king_y, true);
                check = results[0];
                upRightBlocked = results[1];
            }

            if (i + king_x < 9 && king_y - i > 0 && !check && !upLeftBlocked) {
                boolean[] results = checkSquare(teamColor, getBoard(), i + king_x, king_y - i, true);
                check = results[0];
                upLeftBlocked = results[1];
            }

            if (king_x - i > 0 && i + king_y < 9 && !check && !downRightBlocked) {
                boolean[] results = checkSquare(teamColor, getBoard(), king_x - i, i + king_y, true);
                check = results[0];
                downRightBlocked = results[1];
            }

            if (king_x - i > 0 && king_y - i > 0 && !check && !downLeftBlocked) {
                boolean[] results = checkSquare(teamColor, getBoard(), king_x - i, king_y - i, true);
                check = results[0];
                downLeftBlocked = results[1];
            }

            // check for straight-line attacks
            if (i + king_x < 9 && !check && !upBlocked) {
                boolean[] results = checkSquare(teamColor, getBoard(), i + king_x, king_y, false);
                check = results[0];
                upBlocked = results[1];
            }

            if (king_x - i > 0 && !check && !downBlocked) {
                boolean[] results = checkSquare(teamColor, getBoard(), king_x - i, king_y, false);
                check = results[0];
                downBlocked = results[1];
            }

            if (i + king_y < 9 && !check && !rightBlocked) {
                boolean[] results = checkSquare(teamColor, getBoard(), king_x, king_y + i, false);
                check = results[0];
                rightBlocked = results[1];
            }

            if (king_y - i > 0 && !check && !leftBlocked) {
                boolean[] results = checkSquare(teamColor, getBoard(), king_x, king_y - i, false);
                check = results[0];
                leftBlocked = results[1];
            }
        }
        
        // check for knights
        int[][] potentialKnights = {
                        {king_x + 2, king_y + 1},
                        {king_x + 2, king_y - 1},
                        {king_x - 2, king_y + 1},
                        {king_x - 2, king_y - 1},
                        {king_x + 1, king_y + 2},
                        {king_x + 1, king_y - 2},
                        {king_x - 1, king_y + 2},
                        {king_x - 1, king_y - 2}
                };
        
        for (int[] knight : potentialKnights) {
            ChessPiece potential = board.getPiece(new ChessPosition(knight[1], knight[0]));
            if (potential != null && potential.getTeamColor() != teamColor && potential.getPieceType() == ChessPiece.PieceType.KNIGHT){
                check = true;
            }
        }

        return check;
    }

    private ChessPosition getKingPosition(TeamColor teamColor) {
        ChessPosition kingPosition = null;
        boolean kingFound = false;
        for(int i = 1; i < 9 && !kingFound; i++) {
            for(int j = 1; j < 9 && !kingFound; j++) {
                ChessPosition potentialPosition = new ChessPosition(i, j);
                ChessPiece potentialPiece = board.getPiece(potentialPosition);
                boolean isCorrectKing = potentialPiece != null &&
                                        potentialPiece.getTeamColor() == teamColor &&
                                        potentialPiece.getPieceType() == ChessPiece.PieceType.KING;
                if (isCorrectKing) {
                    kingPosition = potentialPosition;
                    kingFound = true;
                }
            }
        }

        return kingPosition;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        /* PSEUDO CODE
        idea: reuse the isInCheck on each square around the king
         */
        Collection<ChessPosition> team = getTeammates(teamColor);
        Collection<ChessMove> potentialMoves = new HashSet<>();
        for(ChessPosition piece : team) {
            potentialMoves.addAll(validMoves(piece));
        }
        return potentialMoves.isEmpty() && isInCheck(teamColor);
    }

    private Collection<ChessPosition> getTeammates(TeamColor teamColor) {
        Collection<ChessPosition> teammates = new HashSet<>();
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition potential = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(potential);
                if (piece != null && piece.getTeamColor() ==  teamColor) {
                    teammates.add(potential);
                }
            }
        }
        return teammates;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        /* PSEUDO CODE
        idea: if isInCheck returns true on every square around the king, but not their own, then they are in stalemate
         */
        ChessPosition king = getKingPosition(teamColor);
        Collection<ChessMove> validMoves = validMoves(king);
        return validMoves.isEmpty() && !isInCheck(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
