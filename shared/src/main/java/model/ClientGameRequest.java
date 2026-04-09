package model;

import chess.ChessGame;

public record ClientGameRequest(ChessGame.TeamColor color, String gameName) {
    
}
