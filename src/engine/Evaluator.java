package engine;

public class Evaluator {

    // Piece values (in centipawns: 100 = 1 pawn)
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;

    private static final long LIGHT_SQUARES = 0x55AA55AA55AA55AAL;
    private static final long DARK_SQUARES  = 0xAA55AA55AA55AA55L;

    // File masks to prevent wrap-around
    private static final long FILE_A = 0x0101010101010101L;
    private static final long FILE_B = 0x0202020202020202L;
    private static final long FILE_G = 0x4040404040404040L;
    private static final long FILE_H = 0x8080808080808080L;

    private static final int[] PASSED_PAWN_BONUS = { 0, 10, 20, 40, 60, 100, 150, 0 };

    private static final int[] PAWN_MG_PST = {
    //  a    b    c    d    e    f    g    h
        0,   0,   0,   0,   0,   0,   0,   0,
        50,  50,  50,  50,  50,  50,  50,  50,
        10,  10,  20,  30,  30,  20,  10,  10,
        5,   5,  10,  25,  25,  10,   5,   5,
        0,   0,   0,  20,  20,   0,   0,   0,
        5,  -5, -10,   0,   0, -10,  -5,   5,
        5,  10,  10, -20, -20,  10,  10,   5,
        0,   0,   0,   0,   0,   0,   0,   0
    };

    private static final int[] PAWN_EG_PST = {
        0,   0,   0,   0,   0,   0,   0,   0,
        80,  80,  80,  80,  80,  80,  80,  80,
        60,  60,  60,  60,  60,  60,  60,  60,
        40,  40,  40,  40,  40,  40,  40,  40,
        20,  20,  20,  20,  20,  20,  20,  20,
        10,  10,  10,  10,  10,  10,  10,  10,
        5,   5,   5,   5,   5,   5,   5,   5,
        0,   0,   0,   0,   0,   0,   0,   0
    };

    private static final int[] KING_MG_PST = {
        -80, -70, -70, -70, -70, -70, -70, -80,
        -60, -60, -60, -60, -60, -60, -60, -60,
        -40, -50, -50, -60, -60, -50, -50, -40,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -20, -30, -30, -40, -40, -30, -30, -20,
        -10, -20, -20, -20, -20, -20, -20, -10,
        20,  20,   0,   0,   0,   0,  20,  20,
        30,  40,  20,   0,   0,  20,  40,  30
    };

    private static final int[] KING_EG_PST = {
        -50, -30, -20, -10, -10, -20, -30, -50,
        -30, -10,  10,  20,  20,  10, -10, -30,
        -20,  10,  30,  40,  40,  30,  10, -20,
        -10,  20,  40,  50,  50,  40,  20, -10,
        -10,  20,  40,  50,  50,  40,  20, -10,
        -20,  10,  30,  40,  40,  30,  10, -20,
        -30, -10,  10,  20,  20,  10, -10, -30,
        -50, -30, -20, -10, -10, -20, -30, -50
    };

    private static final int[] KNIGHT_PST = {
        -50, -40, -30, -30, -30, -30, -40, -50,
        -40, -20,   0,   5,   5,   0, -20, -40,
        -30,   5,  10,  15,  15,  10,   5, -30,
        -30,   0,  15,  20,  20,  15,   0, -30,
        -30,   5,  15,  20,  20,  15,   5, -30,
        -30,   0,  10,  15,  15,  10,   0, -30,
        -40, -20,   0,   0,   0,   0, -20, -40,
        -50, -40, -30, -30, -30, -30, -40, -50
    };

    private static final int[] BISHOP_PST = {
        -20, -10, -10, -10, -10, -10, -10, -20,
        -10,   0,   0,   0,   0,   0,   0, -10,
        -10,   0,   5,  10,  10,   5,   0, -10,
        -10,   5,   5,  10,  10,   5,   5, -10,
        -10,   0,  10,  10,  10,  10,   0, -10,
        -10,  10,  10,  10,  10,  10,  10, -10,
        -10,   5,   0,   0,   0,   0,   5, -10,
        -20, -10, -10, -10, -10, -10, -10, -20
    };

    private static final int[] ROOK_PST = {
        0,   0,   0,   0,   0,   0,   0,   0,
        5,  10,  10,  10,  10,  10,  10,   5,
        -5,   0,   0,   0,   0,   0,   0,  -5,
        -5,   0,   0,   0,   0,   0,   0,  -5,
        -5,   0,   0,   0,   0,   0,   0,  -5,
        -5,   0,   0,   0,   0,   0,   0,  -5,
        -5,   0,   0,   0,   0,   0,   0,  -5,
        0,   0,   0,   5,   5,   0,   0,   0
    };

    private static final int[] QUEEN_PST = {
        -20, -10, -10,  -5,  -5, -10, -10, -20,
        -10,   0,   0,   0,   0,   0,   0, -10,
        -10,   0,   5,   5,   5,   5,   0, -10,
        -5,   0,   5,   5,   5,   5,   0,  -5,
        0,   0,   5,   5,   5,   5,   0,  -5,
        -10,   5,   5,   5,   5,   5,   0, -10,
        -10,   0,   5,   0,   0,   0,   0, -10,
        -20, -10, -10,  -5,  -5, -10, -10, -20
    };



    public static int evaluate(Position position) {
        int score = 0;
        int phase = gamePhase(position); // only game stage logic you need

        score += evaluateMaterial(position);

        // PSTs — blend between middlegame and endgame tables
        int mgPST = evaluatePieceSquareTables(position, false);
        int egPST = evaluatePieceSquareTables(position, true);
        score += taperScore(mgPST, egPST, phase);

        // King safety — fades out as we approach endgame
        score += taperScore(evaluateKingSafety(position), 0, phase);

        // Passed pawns — fade in as we approach endgame
        score += taperScore(0, evaluatePassedPawns(position), phase);

        // These are roughly equal value in all phases
        score += evaluatePawnStructure(position);
        score += evaluateMobility(position);

        score += evaluateBadBishop(position);
        score += evaluateBishopPair(position);
        score += evaluateRookBonuses(position);

        return score;
    }

    // 256 = pure middlegame, 0 = pure endgame, opening handled by book/PSTs
    private static int gamePhase(Position position) {
        int phase = 0;
        phase += Long.bitCount(position.whiteKnight) * 1;
        phase += Long.bitCount(position.blackKnight) * 1;
        phase += Long.bitCount(position.whiteBishop) * 1;
        phase += Long.bitCount(position.blackBishop) * 1;
        phase += Long.bitCount(position.whiteRook)   * 2;
        phase += Long.bitCount(position.blackRook)   * 2;
        phase += Long.bitCount(position.whiteQueen)  * 4;
        phase += Long.bitCount(position.blackQueen)  * 4;
        // Starting position = 4 knights + 4 bishops + 4 rooks + 2 queens
        // = 4 + 4 + 8 + 8 = 24 max
        phase = Math.min(phase, 24);
        return (phase * 256) / 24;
    }


    private static int taperScore(int mgScore, int egScore, int phase) {
        return (mgScore * phase + egScore * (256 - phase)) / 256;
    }


    public static int evaluateMaterial(Position position){
        int score = 0;

        score += Long.bitCount(position.whitePawn) * PAWN_VALUE;
        score += Long.bitCount(position.whiteBishop)*BISHOP_VALUE;
        score += Long.bitCount(position.whiteRook)*ROOK_VALUE;
        score += Long.bitCount(position.whiteKnight)*KNIGHT_VALUE;
        score += Long.bitCount(position.whiteQueen)*QUEEN_VALUE;

        score -= Long.bitCount(position.blackPawn)*PAWN_VALUE;
        score -= Long.bitCount(position.blackBishop)*BISHOP_VALUE;
        score -= Long.bitCount(position.blackRook)*ROOK_VALUE;
        score -= Long.bitCount(position.blackKnight)*KNIGHT_VALUE;
        score -= Long.bitCount(position.blackQueen)*QUEEN_VALUE;

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
                score += PASSED_PAWN_BONUS[rank];
            }
        }
        // Black passed pawns
        long blackPawns = position.blackPawn;
        while (blackPawns != 0) {
            int square = Long.numberOfTrailingZeros(blackPawns);
            blackPawns &= blackPawns - 1;

            if (isPassedPawn(position, square, false)) {
                int rank = square / 8;
                score -= PASSED_PAWN_BONUS[7 - rank];
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

        score += evaluateIsolatedPawns(position.whitePawn, true);
        score += evaluateIsolatedPawns(position.blackPawn, false);

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

    private static int evaluateIsolatedPawns(long pawns, boolean isWhite) {
        int penalty = 0;

        for (int file = 0; file < 8; file++) {
            long fileMask = 0x0101010101010101L << file;

            // No pawn on this file — skip
            if ((pawns & fileMask) == 0) continue;

            // Build adjacent file mask
            long adjacentMask = 0L;
            if (file > 0) adjacentMask |= 0x0101010101010101L << (file - 1);
            if (file < 7) adjacentMask |= 0x0101010101010101L << (file + 1);

            // No friendly pawns on either neighbouring file = isolated
            if ((pawns & adjacentMask) == 0) {
                penalty -= Long.bitCount(pawns & fileMask) * 30;
            }
        }
        return isWhite ? penalty : -penalty;
    }


    //evaluate mobility of the piece
    private static int evaluateMobility(Position position) {
        return evaluatePieceMobility(position, true)
                - evaluatePieceMobility(position, false);
    }


    private static int evaluatePieceMobility(Position position, boolean isWhite) {
        int score = 0;
        long ownPieces  = isWhite ? getWhitePieces(position) : getBlackPieces(position);
        long allPieces  = getAllPieces(position);

        // Knights — 4cp per available square
        long knights = isWhite ? position.whiteKnight : position.blackKnight;
        while (knights != 0) {
            int sq = Long.numberOfTrailingZeros(knights);
            knights &= knights - 1;
            int mobility = Long.bitCount(knightAttacks(sq) & ~ownPieces);
            score += mobility * 4;
        }

        // Bishops — 3cp per available square
        long bishops = isWhite ? position.whiteBishop : position.blackBishop;
        while (bishops != 0) {
            int sq = Long.numberOfTrailingZeros(bishops);
            bishops &= bishops - 1;
            int mobility = Long.bitCount(slidingAttacks(sq, allPieces, true) & ~ownPieces);
            score += mobility * 3;
        }

        // Rooks — 2cp per available square
        long rooks = isWhite ? position.whiteRook : position.blackRook;
        while (rooks != 0) {
            int sq = Long.numberOfTrailingZeros(rooks);
            rooks &= rooks - 1;
            int mobility = Long.bitCount(slidingAttacks(sq, allPieces, false) & ~ownPieces);
            score += mobility * 2;
        }

        // Queens — 1cp per available square (they naturally have many squares)
        long queens = isWhite ? position.whiteQueen : position.blackQueen;
        while (queens != 0) {
            int sq = Long.numberOfTrailingZeros(queens);
            queens &= queens - 1;
            long diag     = slidingAttacks(sq, allPieces, true);
            long straight = slidingAttacks(sq, allPieces, false);
            int mobility  = Long.bitCount((diag | straight) & ~ownPieces);
            score += mobility * 1;
        }

        return score;
    }


    private static long knightAttacks(int sq) {
        long bb = 1L << sq;
        long attacks = 0L;
        attacks |= (bb << 17) & ~FILE_A;
        attacks |= (bb << 15) & ~FILE_H;
        attacks |= (bb << 10) & ~(FILE_A | FILE_B);
        attacks |= (bb <<  6) & ~(FILE_G | FILE_H);
        attacks |= (bb >> 17) & ~FILE_H;
        attacks |= (bb >> 15) & ~FILE_A;
        attacks |= (bb >> 10) & ~(FILE_G | FILE_H);
        attacks |= (bb >>  6) & ~(FILE_A | FILE_B);
        return attacks;
    }

    private static long slidingAttacks(int sq, long blockers, boolean diagonal) {
        long attacks = 0L;
        int[][] dirs = diagonal
                ? new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}}
                : new int[][]{{1,0},{-1,0},{0,1},{0,-1}};

        int file = sq % 8;
        int rank = sq / 8;

        for (int[] dir : dirs) {
            int f = file + dir[0];
            int r = rank + dir[1];
            while (f >= 0 && f < 8 && r >= 0 && r < 8) {
                int target = r * 8 + f;
                attacks |= 1L << target;
                if ((blockers & (1L << target)) != 0) break; // blocked
                f += dir[0];
                r += dir[1];
            }
        }
        return attacks;
    }

    private static long getWhitePieces(Position position) {
        return position.whitePawn | position.whiteKnight | position.whiteBishop
                | position.whiteRook | position.whiteQueen | position.whiteKing;
    }

    private static long getBlackPieces(Position position) {
        return position.blackPawn | position.blackKnight | position.blackBishop
                | position.blackRook | position.blackQueen | position.blackKing;
    }

    private static long getAllPieces(Position position) {
        return getWhitePieces(position) | getBlackPieces(position);
    }


    private static int evaluateKingSafety(Position position) {
        int score = 0;

        score += evaluateSingleKingSafety(
                Long.numberOfTrailingZeros(position.whiteKing),
                position.whitePawn,
                position.blackPawn,
                position,
                true
        );

        score -= evaluateSingleKingSafety(
                Long.numberOfTrailingZeros(position.blackKing),
                position.blackPawn,
                position.whitePawn,
                position,
                false
        );

        return score;
    }

    private static int evaluateSingleKingSafety(
            int kingSq,
            long ownPawns,
            long enemyPawns,
            Position position,
            boolean isWhite) {

        int score = 0;
        int file = kingSq % 8;
        int rank = kingSq / 8;

        // ---------------------------------------------------------------
        // 1. PAWN SHIELD
        // Reward pawns on the two ranks directly in front of the king.
        // Only applies when king is castled (files a-c or f-h).
        // ---------------------------------------------------------------
        boolean isCastled = file <= 2 || file >= 5;

        if (isCastled) {
            for (int f = Math.max(0, file - 1); f <= Math.min(7, file + 1); f++) {
                int shieldRank1 = isWhite ? rank + 1 : rank - 1; // directly in front
                int shieldRank2 = isWhite ? rank + 2 : rank - 2; // one further

                if (shieldRank1 >= 0 && shieldRank1 < 8) {
                    int sq = shieldRank1 * 8 + f;
                    if ((ownPawns & (1L << sq)) != 0) score += 15; // close shield pawn
                }
                if (shieldRank2 >= 0 && shieldRank2 < 8) {
                    int sq = shieldRank2 * 8 + f;
                    if ((ownPawns & (1L << sq)) != 0) score += 5;  // further shield pawn
                }
            }
        }

        // ---------------------------------------------------------------
        // 2. OPEN FILE PENALTY
        // Open/semi-open files near the king are dangerous — enemy rooks
        // and queens can use them to attack.
        // ---------------------------------------------------------------
        for (int f = Math.max(0, file - 1); f <= Math.min(7, file + 1); f++) {
            long fileMask = 0x0101010101010101L << f;
            boolean noOwnPawn   = (ownPawns   & fileMask) == 0;
            boolean noEnemyPawn = (enemyPawns & fileMask) == 0;

            if (noOwnPawn && noEnemyPawn) score -= 20; // fully open — very dangerous
            else if (noOwnPawn)           score -= 10; // semi-open — somewhat dangerous
        }

        // ---------------------------------------------------------------
        // 3. ATTACKER COUNT
        // Count enemy pieces bearing on the 3x3 zone around the king.
        // Danger is exponential — 3 attackers is much worse than 3x 1.
        // ---------------------------------------------------------------
        long kingZone = kingZone(kingSq);
        long allPieces = getAllPieces(position);
        int attackCount = 0;

        // Enemy knights
        long enemyKnights = isWhite ? position.blackKnight : position.whiteKnight;
        while (enemyKnights != 0) {
            int sq = Long.numberOfTrailingZeros(enemyKnights);
            enemyKnights &= enemyKnights - 1;
            if ((knightAttacks(sq) & kingZone) != 0) attackCount++;
        }

        // Enemy bishops
        long enemyBishops = isWhite ? position.blackBishop : position.whiteBishop;
        while (enemyBishops != 0) {
            int sq = Long.numberOfTrailingZeros(enemyBishops);
            enemyBishops &= enemyBishops - 1;
            if ((slidingAttacks(sq, allPieces, true) & kingZone) != 0) attackCount++;
        }

        // Enemy rooks
        long enemyRooks = isWhite ? position.blackRook : position.whiteRook;
        while (enemyRooks != 0) {
            int sq = Long.numberOfTrailingZeros(enemyRooks);
            enemyRooks &= enemyRooks - 1;
            if ((slidingAttacks(sq, allPieces, false) & kingZone) != 0) attackCount++;
        }

        // Enemy queens — count double, they're the most dangerous attacker
        long enemyQueens = isWhite ? position.blackQueen : position.whiteQueen;
        while (enemyQueens != 0) {
            int sq = Long.numberOfTrailingZeros(enemyQueens);
            enemyQueens &= enemyQueens - 1;
            long qAttacks = slidingAttacks(sq, allPieces, true)
                    | slidingAttacks(sq, allPieces, false);
            if ((qAttacks & kingZone) != 0) attackCount += 2;
        }

        // Exponential danger table — indexed by attacker count
        int[] dangerTable = { 0, 20, 50, 90, 140, 200, 270, 350 };
        int index = Math.min(attackCount, dangerTable.length - 1);
        score -= dangerTable[index];

        return score;
    }


    private static int evaluatePieceSquareTables(Position position, boolean isEndgame) {
        int score = 0;

        // White pieces (normal orientation)
        score += pstScore(position.whitePawn,   isEndgame ? PAWN_EG_PST   : PAWN_MG_PST,   false);
        score += pstScore(position.whiteKnight, KNIGHT_PST,                                 false);
        score += pstScore(position.whiteBishop, BISHOP_PST,                                 false);
        score += pstScore(position.whiteRook,   ROOK_PST,                                   false);
        score += pstScore(position.whiteQueen,  QUEEN_PST,                                  false);
        score += pstScore(position.whiteKing,   isEndgame ? KING_EG_PST   : KING_MG_PST,   false);

        // Black pieces (mirrored — black's rank 8 = white's rank 1)
        score -= pstScore(position.blackPawn,   isEndgame ? PAWN_EG_PST   : PAWN_MG_PST,   true);
        score -= pstScore(position.blackKnight, KNIGHT_PST,                                 true);
        score -= pstScore(position.blackBishop, BISHOP_PST,                                 true);
        score -= pstScore(position.blackRook,   ROOK_PST,                                   true);
        score -= pstScore(position.blackQueen,  QUEEN_PST,                                  true);
        score -= pstScore(position.blackKing,   isEndgame ? KING_EG_PST   : KING_MG_PST,   true);

        return score;
    }


    private static int pstScore(long bitboard, int[] pst, boolean mirrorForBlack) {
        int score = 0;
        while (bitboard != 0) {
            int sq = Long.numberOfTrailingZeros(bitboard); // which square is this piece on?
            bitboard &= bitboard - 1;                      // clear that bit, move to next piece
            score += pst[mirrorForBlack ? mirror(sq) : sq];
        }
        return score;
    }

    private static int mirror(int square) {
        return (7 - square / 8) * 8 + (square % 8);
        // Flips rank: square 0 (a1) → 56 (a8), square 63 (h8) → 7 (h1)
    }



    private static int evaluateBadBishop(Position position) {
        int score = 0;

        // White bad bishop — hurts white so subtract
        long whiteBishops = position.whiteBishop;
        while (whiteBishops != 0) {
            int sq = Long.numberOfTrailingZeros(whiteBishops);
            whiteBishops &= whiteBishops - 1;

            boolean isLightSquared = (LIGHT_SQUARES & (1L << sq)) != 0;
            long relevantPawns = isLightSquared
                    ? position.whitePawn & LIGHT_SQUARES
                    : position.whitePawn & DARK_SQUARES;

            score -= Long.bitCount(relevantPawns) * 8;
        }

        // Black bad bishop — hurts black so add (good for white)
        long blackBishops = position.blackBishop;
        while (blackBishops != 0) {
            int sq = Long.numberOfTrailingZeros(blackBishops);
            blackBishops &= blackBishops - 1;

            boolean isLightSquared = (LIGHT_SQUARES & (1L << sq)) != 0;
            long relevantPawns = isLightSquared
                    ? position.blackPawn & LIGHT_SQUARES
                    : position.blackPawn & DARK_SQUARES;

            score += Long.bitCount(relevantPawns) * 8;
        }

        return score;
    }


    private static long kingZone(int sq) {
        long bb = 1L << sq;
        long zone = bb;
        zone |= (bb << 8) | (bb >> 8);                          // rank above and below
        zone |= ((bb | (bb << 8) | (bb >> 8)) << 1) & ~FILE_A; // files to the right
        zone |= ((bb | (bb << 8) | (bb >> 8)) >> 1) & ~FILE_H; // files to the left
        return zone; // 3x3 area around the king
    }


    private static int evaluateBishopPair(Position position) {
        int score = 0;
        if (Long.bitCount(position.whiteBishop) >= 2) score += 50;
        if (Long.bitCount(position.blackBishop) >= 2) score -= 50;
        return score;
    }


    private static int evaluateRookBonuses(Position position) {
        int score = 0;

        long whiteRooks = position.whiteRook;
        while (whiteRooks != 0) {
            int sq = Long.numberOfTrailingZeros(whiteRooks);
            whiteRooks &= whiteRooks - 1;

            int file = sq % 8;
            long fileMask = 0x0101010101010101L << file;

            boolean noWhitePawn = (position.whitePawn & fileMask) == 0;
            boolean noBlackPawn = (position.blackPawn & fileMask) == 0;

            if (noWhitePawn && noBlackPawn) score += 25; // open file
            else if (noWhitePawn)           score += 12; // semi-open file

            if (sq / 8 == 6) score += 20; // 7th rank
        }

        long blackRooks = position.blackRook;
        while (blackRooks != 0) {
            int sq = Long.numberOfTrailingZeros(blackRooks);
            blackRooks &= blackRooks - 1;

            int file = sq % 8;
            long fileMask = 0x0101010101010101L << file;

            boolean noBlackPawn = (position.blackPawn & fileMask) == 0;
            boolean noWhitePawn = (position.whitePawn & fileMask) == 0;

            if (noBlackPawn && noWhitePawn) score -= 25;
            else if (noBlackPawn)           score -= 12;

            if (sq / 8 == 1) score -= 20; // 7th rank for black (rank index 1)
        }

        return score;
    }
}