package engine;

public class Evaluator {

    // Piece values (in centipawns: 100 = 1 pawn)
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;

    // Evaluate position from white's perspective
    // Positive = good for white, Negative = good for black
    public static int evaluate(Position position) {
        int score = 0;

        // White material (positive)
        score += Long.bitCount(position.whitePawn) * PAWN_VALUE;
        score += Long.bitCount(position.whiteKnight) * KNIGHT_VALUE;
        score += Long.bitCount(position.whiteBishop) * BISHOP_VALUE;
        score += Long.bitCount(position.whiteRook) * ROOK_VALUE;
        score += Long.bitCount(position.whiteQueen) * QUEEN_VALUE;

        // Black material (negative)
        score -= Long.bitCount(position.blackPawn) * PAWN_VALUE;
        score -= Long.bitCount(position.blackKnight) * KNIGHT_VALUE;
        score -= Long.bitCount(position.blackBishop) * BISHOP_VALUE;
        score -= Long.bitCount(position.blackRook) * ROOK_VALUE;
        score -= Long.bitCount(position.blackQueen) * QUEEN_VALUE;


        return score;
    }
}