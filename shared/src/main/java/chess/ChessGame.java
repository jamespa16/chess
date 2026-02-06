package chess;

import java.util.Collection;
import java.util.HashSet;

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
    private boolean whiteCanCastle;
    private boolean blackCanCastle;

    public ChessGame() {
        board = new ChessBoard();
        currentTurn = TeamColor.WHITE;
        whiteCanCastle = true;
        blackCanCastle = true;

        board.resetBoard();
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

    private boolean simulateMove (ChessMove move) {
        ChessGame future = new ChessGame();
        future.board = this.board;
        try {
            future.makeMove(move);
        } catch (InvalidMoveException e) {
            return false;
        }
        return future.isInCheck(currentTurn);
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
        int start_x = start.getColumn();
        int start_y = start.getRow();
        int end_x = end.getColumn();
        int end_y = end.getRow();

        boolean boundsCheck = start_x > 0 &&
                              start_x < 9 &&
                              start_y > 0 &&
                              start_y < 9 &&
                              end_x > 0 &&
                              end_x < 9 &&
                              end_y > 0 &&
                              end_y < 9;
        
        if (!boundsCheck) {
            throw new InvalidMoveException("move out of bounds");
        }

        ChessPiece startPiece = board.getPiece(start);
        ChessPiece endPiece = board.getPiece(end);

        if (endPiece != null && endPiece.getTeamColor() == startPiece.getTeamColor()) {
            throw new InvalidMoveException("move results in attacking teammate");
        }

        board.movePiece(start, end);
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

        
        for (int i = 1; i < 9 && !check; i++) {
            for(int j = 1; j < 9 && !check; j++) {
                // check for bishops & queens on the diagonal
                if (i + king_x < 9 && j + king_y < 9) {
                    ChessPiece potentialAttacker = board.getPiece(new ChessPosition(i + king_x, j + king_y));
                    if (potentialAttacker != null) {
                        PieceType attackerType = potentialAttacker.getPieceType();
                        if (attackerType == ChessPiece.PieceType.BISHOP || attackerType == ChessPiece.PieceType.QUEEN) {
                            check = true;
                        }
                    }
                }

                if (i + king_x < 9 && j - king_y > 0 && !check) {
                    ChessPiece potentialAttacker = board.getPiece(new ChessPosition(i + king_x, j - king_y));
                    if (potentialAttacker != null) {
                        PieceType attackerType = potentialAttacker.getPieceType();
                        if (attackerType == ChessPiece.PieceType.BISHOP || attackerType == ChessPiece.PieceType.QUEEN) {
                            check = true;
                        }
                    }
                }

                if (i - king_x > 0 && j + king_y < 9 && !check) {
                    ChessPiece potentialAttacker = board.getPiece(new ChessPosition(i + king_x, j + king_y));
                    if (potentialAttacker != null) {
                        PieceType attackerType = potentialAttacker.getPieceType();
                        if (attackerType == ChessPiece.PieceType.BISHOP || attackerType == ChessPiece.PieceType.QUEEN) {
                            check = true;
                        }
                    }
                }

                if (i - king_x > 0 && j - king_y > 0 && !check) {
                    ChessPiece potentialAttacker = board.getPiece(new ChessPosition(i + king_x, j - king_y));
                    if (potentialAttacker != null) {
                        PieceType attackerType = potentialAttacker.getPieceType();
                        if (attackerType == ChessPiece.PieceType.BISHOP || attackerType == ChessPiece.PieceType.QUEEN) {
                            check = true;
                        }
                    }
                }
            }

            // check for straight-line attacks
            if (i + king_x < 9 && !check) {
                ChessPiece potentialAttacker = board.getPiece(new ChessPosition(i + king_x, king_y));
                if (potentialAttacker != null) {
                    PieceType attackerType = potentialAttacker.getPieceType();
                    if (attackerType == ChessPiece.PieceType.ROOK || attackerType == ChessPiece.PieceType.QUEEN) {
                        check = true;
                    }
                }
            }

            if (i - king_x > 0 && !check) {
                ChessPiece potentialAttacker = board.getPiece(new ChessPosition(i + king_x, king_y));
                if (potentialAttacker != null) {
                    PieceType attackerType = potentialAttacker.getPieceType();
                    if (attackerType == ChessPiece.PieceType.ROOK || attackerType == ChessPiece.PieceType.QUEEN) {
                        check = true;
                    }
                }
            }

            if (i + king_y < 9 && !check) {
                ChessPiece potentialAttacker = board.getPiece(new ChessPosition(king_x, i + king_y));
                if (potentialAttacker != null) {
                    PieceType attackerType = potentialAttacker.getPieceType();
                    if (attackerType == ChessPiece.PieceType.ROOK || attackerType == ChessPiece.PieceType.QUEEN) {
                        check = true;
                    }
                }
            }

            if (i - king_y > 0 && !check) {
                ChessPiece potentialAttacker = board.getPiece(new ChessPosition(king_x, i - king_y));
                if (potentialAttacker != null) {
                    PieceType attackerType = potentialAttacker.getPieceType();
                    if (attackerType == ChessPiece.PieceType.ROOK || attackerType == ChessPiece.PieceType.QUEEN) {
                        check = true;
                    }
                }
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
        throw new RuntimeException("Not implemented");
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
        throw new RuntimeException("Not implemented");
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
