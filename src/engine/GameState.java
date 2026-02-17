package engine;

public class GameState {
    // State that changes with each move
    boolean whiteCanCastleKingside;
    boolean whiteCanCastleQueenside;
    boolean blackCanCastleKingside;
    boolean blackCanCastleQueenside;
    int enPassantSquare;
    int halfMoveCount;

    // Constructor to save current state
    public GameState(Position position) {
        this.whiteCanCastleKingside = position.whiteCanCastleKingside;
        this.whiteCanCastleQueenside = position.whiteCanCastleQueenside;
        this.blackCanCastleKingside = position.blackCanCastleKingside;
        this.blackCanCastleQueenside = position.blackCanCastleQueenside;
        this.enPassantSquare = position.enPassantSquare;
        this.halfMoveCount = position.halfMoveCount;
    }
}