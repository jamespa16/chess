package model;

import chess.ChessGame;

public record JoinRequest(ChessGame.TeamColor color, int gameID) {
}
