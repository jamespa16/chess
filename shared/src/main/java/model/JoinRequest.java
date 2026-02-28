package model;

import chess.ChessGame.TeamColor;

public record JoinRequest(TeamColor color, int gameID) {
}
