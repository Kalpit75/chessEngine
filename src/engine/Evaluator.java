package engine;

public class Evaluator {

    // Piece values (in centipawns: 100 = 1 pawn)
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;



    //calculate game stage
    private static final int OPENING = 0;
    private static final int MIDDLEGAME = 1;
    private static final int ENDGAME = 2;

    private static int detectGameStage(Position position) {
        // Count total pieces (excluding pawns and kings)
        int whitePieces = Long.bitCount(position.whiteKnight)
                + Long.bitCount(position.whiteBishop)
                + Long.bitCount(position.whiteRook)
                + Long.bitCount(position.whiteQueen);

        int blackPieces = Long.bitCount(position.blackKnight)
                + Long.bitCount(position.blackBishop)
                + Long.bitCount(position.blackRook)
                + Long.bitCount(position.blackQueen);

        int totalPieces = whitePieces + blackPieces;

        if (totalPieces > 14) return OPENING;
        if (totalPieces > 8) return MIDDLEGAME;
        return ENDGAME;
    }


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

        //positional factors
        score += evaluatePawnStructure(position);
        score += evaluateMobility(position);
        score += evaluateQueenActivity(position);
        score += evaluateKingSafety(position);

        //game stage specific
        int gameStage = detectGameStage(position);
        if (gameStage == ENDGAME){
            score += evaluatePassedPawns(position);
        }

        return score;
    }



    private static int evaluatePassedPawns(Position position) {
        int score = 0;

        // White passed pawns
        long whitePawns = position.whitePawn;
        while (whitePawns != 0) {
            int square = Long.numberOfTrailingZeros(whitePawns);
            whitePawns &= whitePawns - 1;  // Remove this bit

            if (isPassedPawn(position, square, true)) {
                int rank = square / 8;
                int distanceToPromotion = 7 - rank;
                int bonus = 200 / (distanceToPromotion + 1);  // Closer = stronger
                score += bonus;
            }
        }

        // Black passed pawns
        long blackPawns = position.blackPawn;
        while (blackPawns != 0) {
            int square = Long.numberOfTrailingZeros(blackPawns);
            blackPawns &= blackPawns - 1;

            if (isPassedPawn(position, square, false)) {
                int rank = square / 8;
                int distanceToPromotion = rank;
                int bonus = 200 / (distanceToPromotion + 1);
                score -= bonus;
            }
        }

        return score;
    }

    private static boolean isPassedPawn(Position position, int square, boolean isWhite) {
        int file = square % 8;
        int rank = square / 8;

        // Check files to left, center, and right
        for (int f = Math.max(0, file - 1); f <= Math.min(7, file + 1); f++) {
            // Scan up (for white) or down (for black)
            if (isWhite) {
                for (int r = rank + 1; r < 8; r++) {
                    int checkSquare = r * 8 + f;
                    if ((position.blackPawn & (1L << checkSquare)) != 0) {
                        return false;  // Enemy pawn blocks
                    }
                }
            } else {
                for (int r = rank - 1; r >= 0; r--) {
                    int checkSquare = r * 8 + f;
                    if ((position.whitePawn & (1L << checkSquare)) != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }



    private static int evaluatePawnStructure(Position position) {
        int score = 0;

        // Penalize doubled pawns (same file)
        score += evaluateDoubledPawns(position.whitePawn, true);
        score += evaluateDoubledPawns(position.blackPawn, false);

        return score;
    }

    private static int evaluateDoubledPawns(long pawns, boolean isWhite) {
        int penalty = 0;

        // Check each file for doubled pawns
        for (int file = 0; file < 8; file++) {
            int count = 0;
            long fileMask = 0x0101010101010101L << file;  // All squares in this file

            // Count pawns in this file
            long pawnsInFile = pawns & fileMask;
            count = Long.bitCount(pawnsInFile);

            // Each doubled pawn costs 50 centipawns
            if (count > 1) {
                penalty -= (count - 1) * 50;
            }
        }
        return isWhite ? penalty : -penalty;
    }


    //evaluate mobility of the piece
    private static int evaluateMobility(Position position) {
        int score = 0;

        // White mobility (count available squares for pieces)
        score += evaluatePieceMobility(position, true);

        // Black mobility
        score -= evaluatePieceMobility(position, false);

        return score;
    }

    private static int evaluatePieceMobility(Position position, boolean isWhite) {
        int score = 0;

        // Knights: roughly 1 point per available square
        long knights = isWhite ? position.whiteKnight : position.blackKnight;
        while (knights != 0) {
            int square = Long.numberOfTrailingZeros(knights);
            knights &= knights - 1;

            // Count squares knight can move to (simplified—doesn't check legality)
            int mobility = countKnightMoves(square, position, isWhite);
            score += mobility * 5;  // 5 centipawns per square
        }
        return score;
    }

    private static int countKnightMoves(int square, Position position, boolean isWhite) {
        int count = 0;
        int[] knightMoves = {-17, -15, -10, -6, 6, 10, 15, 17};

        for (int offset : knightMoves) {
            int target = square + offset;
            if (target >= 0 && target < 64) {
                // Rough bounds check (doesn't handle wrapping perfectly)
                if (Math.abs((target % 8) - (square % 8)) <= 2 && Math.abs((target / 8) - (square / 8)) <= 2) {
                    count++;
                }
            }
        }
        return count;
    }



    private static int evaluateKingSafety(Position position) {
        int score = 0;
        int gameStage = detectGameStage(position);

        // Only penalize unsafe kings in opening/middlegame
        if (gameStage != ENDGAME) {
            // White king safety
            long whiteKing = position.whiteKing;
            if (whiteKing != 0) {
                int kingSquare = Long.numberOfTrailingZeros(whiteKing);

                // King should be castled or on back rank
                int rank = kingSquare / 8;
                if (rank != 0) {  // Not on back rank
                    score -= 200;  // Heavy penalty for exposed king
                }

                // Bonus for castling (king on g1 or c1)
                if (kingSquare == 6 || kingSquare == 2) {
                    score += 100;  // Safe castled position
                }
            }

            // Black king safety (similar)
            long blackKing = position.blackKing;
            if (blackKing != 0) {
                int kingSquare = Long.numberOfTrailingZeros(blackKing);

                int rank = kingSquare / 8;
                if (rank != 7) {  // Not on back rank
                    score -= 200;
                }

                if (kingSquare == 62 || kingSquare == 58) {
                    score -= 100;  // Negative because we're subtracting for black
                }
            }
        }

        return score;
    }



    private static int evaluateQueenActivity(Position position) {
        int score = 0;

        // White queen
        long whiteQueens = position.whiteQueen;
        while (whiteQueens != 0) {
            int square = Long.numberOfTrailingZeros(whiteQueens);
            whiteQueens &= whiteQueens - 1;

            // Bonus for being near center (centralization)
            int file = square % 8;
            int rank = square / 8;
            int distanceFromCenter = Math.max(Math.abs(file - 3), Math.abs(rank - 3));
            score += (5 - (int)distanceFromCenter) * 10;  // More centered = better

            // Bonus for attacking pieces
            int attacks = countQueenAttacks(position, square, true);
            score += attacks * 20;  // 20 centipawns per piece attacked
        }

        // Black queen (similar, but negative)
        long blackQueens = position.blackQueen;
        while (blackQueens != 0) {
            int square = Long.numberOfTrailingZeros(blackQueens);
            blackQueens &= blackQueens - 1;

            int file = square % 8;
            int rank = square / 8;
            int distanceFromCenter = Math.max(Math.abs(file - 3), Math.abs(rank - 3));
            score -= (5 - (int)distanceFromCenter) * 10;

            int attacks = countQueenAttacks(position, square, false);
            score -= attacks * 20;
        }

        return score;
    }

    private static int countQueenAttacks(Position position, int square, boolean isWhite) {
        int count = 0;

        // Queens attack along ranks, files, and diagonals
        // Check all 8 directions
        int[][] directions = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1},  // Ranks and files
                {-1, -1}, {-1, 1}, {1, -1}, {1, 1}  // Diagonals
        };

        int file = square % 8;
        int rank = square / 8;

        for (int[] dir : directions) {
            int f = file + dir[0];
            int r = rank + dir[1];

            while (f >= 0 && f < 8 && r >= 0 && r < 8) {
                int targetSquare = r * 8 + f;
                char piece = getBitboardPiece(position, targetSquare);

                if (piece != '.') {
                    // Count enemy pieces
                    if (isWhite && Character.isLowerCase(piece)) {
                        count++;
                    } else if (!isWhite && Character.isUpperCase(piece)) {
                        count++;
                    }
                    break;  // Queen can't attack through pieces
                }

                f += dir[0];
                r += dir[1];
            }
        }

        return count;
    }

    private static char getBitboardPiece(Position position, int square) {
        long mask = 1L << square;

        if ((position.whitePawn & mask) != 0) return 'P';
        if ((position.whiteKnight & mask) != 0) return 'N';
        if ((position.whiteBishop & mask) != 0) return 'B';
        if ((position.whiteRook & mask) != 0) return 'R';
        if ((position.whiteQueen & mask) != 0) return 'Q';
        if ((position.whiteKing & mask) != 0) return 'K';

        if ((position.blackPawn & mask) != 0) return 'p';
        if ((position.blackKnight & mask) != 0) return 'n';
        if ((position.blackBishop & mask) != 0) return 'b';
        if ((position.blackRook & mask) != 0) return 'r';
        if ((position.blackQueen & mask) != 0) return 'q';
        if ((position.blackKing & mask) != 0) return 'k';

        return '.';
    }
}