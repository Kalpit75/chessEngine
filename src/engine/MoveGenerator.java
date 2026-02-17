package engine;

import java.util.*;

public class MoveGenerator {

    // Precomputed knight attack patterns
    private static final long[] KNIGHT_ATTACKS = new long[64];
    private static final long[] KING_ATTACKS = new long[64];

    // Initialize the lookup table
    static {
        for (int square = 0; square < 64; square++) {
            KNIGHT_ATTACKS[square] = computeKnightAttacks(square);
            KING_ATTACKS[square] = computeKingAttacks(square);
        }
    }


    public static List<Move> generateMoves(Position position) {
        List<Move> moves = new ArrayList<>();
        boolean isWhite = position.isWhiteTurn;
        generateKnightMoves(position, moves, isWhite);
        generateKingMoves(position, moves, isWhite);
        generatePawnMoves(position, moves, isWhite);
        generateRookMoves(position, moves, isWhite);
        generateBishopMoves(position, moves, isWhite);
        generateQueenMoves(position, moves, isWhite);
        generateCastlingMoves(position, moves, isWhite);
        return moves;
    }



    public static List<Move> generateLegalMoves(Position position) {
        // 1. Generate all pseudo-legal moves
        List<Move> pseudoLegalMoves =generateMoves(position);
        List<Move> legalMoves = new ArrayList<>();

        boolean isWhite = position.isWhiteTurn;

        // 2. Filter out moves that leave king in check
        for (Move move : pseudoLegalMoves) {
            // Try the move
            GameState saved = position.makeMove(move);

            // Is our king in check after this move?
            if (!position.isKingInCheck(isWhite)) {
                legalMoves.add(move);  // Legal!
            }

            // Undo the move
            position.unmakeMove(move, saved);
        }

        return legalMoves;
    }



    private static long computeKnightAttacks(int square) {
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        // All 8 possible knight moves (L-shapes)
        int[][] knightMoves = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        for (int[] move : knightMoves) {
            int newRank = rank + move[0];
            int newFile = file + move[1];

            // Check if the move is within board bounds
            if (newRank >= 0 && newRank < 8 && newFile >= 0 && newFile < 8) {
                int targetSquare = newRank * 8 + newFile;
                attacks |= (1L << targetSquare);
            }
        }
        return attacks;
    }


    // knight attack
    private static void generateKnightMoves(Position position, List<Move> moves, boolean isWhite) {
        // Get the bitboard for knights of the current color
        long knights = isWhite ? position.whiteKnight : position.blackKnight;

        // Get all friendly pieces (can't move to these squares)
        long friendlyPieces = getFriendlyPieces(position, isWhite);

        // For each knight on the board
        while (knights != 0) {
            int from = Long.numberOfTrailingZeros(knights);
            knights &= knights - 1;  // Remove this knight

            // Get all squares this knight can attack
            long attacks = KNIGHT_ATTACKS[from];

            // Remove friendly pieces (can't capture your own)
            long validMoves = attacks & ~friendlyPieces;

            // For each valid destination
            while (validMoves != 0) {
                int to = Long.numberOfTrailingZeros(validMoves);
                validMoves &= validMoves - 1;

                // Check if this square has an enemy piece
                char captured = position.getPieceAt(to);

                // Create the move
                moves.add(new Move(from, to, captured));
            }
        }
    }


    private static long computeKingAttacks(int square){
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        int[][] kingMoves = {
                {1,0}, {-1,0}, {0,1}, {0,-1},
                {1,1}, {1,-1}, {-1,1}, {-1,-1}
        };

        for (int[] move: kingMoves){
            int newRank = rank + move[0];
            int newFile = file + move[1];

            // Check if the move is within board bounds
            if (newRank >= 0 && newRank < 8 && newFile >= 0 && newFile < 8) {
                int targetSquare = newRank * 8 + newFile;
                attacks |= (1L << targetSquare);
            }
        }
        return attacks;
    }


    private static void generateKingMoves(Position position, List<Move> moves, boolean isWhite){
       long kings = isWhite? position.whiteKing : position.blackKing;
       long friendlyPieces = getFriendlyPieces(position, isWhite);

        // For each king on the board
        while (kings != 0) {
            int from = Long.numberOfTrailingZeros(kings);
            kings &= kings - 1;  // Remove this king

            // Get all squares this king can attack
            long attacks = KING_ATTACKS[from];

            // Remove friendly pieces (can't capture your own)
            long validMoves = attacks & ~friendlyPieces;

            // For each valid destination
            while (validMoves != 0) {
                int to = Long.numberOfTrailingZeros(validMoves);
                validMoves &= validMoves - 1;

                // Check if this square has an enemy piece
                char captured = position.getPieceAt(to);

                // Create the move
                moves.add(new Move(from, to, captured));
            }
        }
    }


    private static long getAllPieces(Position position) {
        return getFriendlyPieces(position, true) | getFriendlyPieces(position, false);
    }


    //pawns
    private static void generatePawnMoves(Position position, List<Move> moves, boolean isWhite) {
        // Get all pawns
        long pawns = isWhite? position.whitePawn: position.blackPawn;
        long allPieces = getAllPieces(position);

        int pushDirection = isWhite? 8: -8;

        //for each pawn
        while (pawns != 0){
            int from = Long.numberOfTrailingZeros(pawns);
            pawns &= pawns -1;

            //1. single push
            int to = from + pushDirection;
            if (to >=0 && to<64){ //check bounds
                long toMask = 1L << to;
                if ((allPieces & toMask) == 0){ //square is empty
                   //check if this is promotion
                   int toRank = to/8;
                   if ((isWhite && toRank == 7) || (!isWhite && toRank == 0)){
                       //promotion
                       char[] promotionPieces = isWhite? new char[]{'Q', 'R', 'B', 'N'}:
                               new char[]{'q', 'r', 'b', 'n'};
                       for (char piece : promotionPieces){
                           Move move = new Move(from, to, '\0');
                           move.setPromotion(piece);
                           moves.add(move);
                       }

                   }else{
                       //normal pawn push
                       moves.add(new Move(from, to, '\0'));
                   }
                }
            }

            // 2. Double push (only from starting rank)
            int fromRank = from / 8;
            boolean onStartingRank = (isWhite && fromRank == 1) || (!isWhite && fromRank == 6);

            if (onStartingRank) {
                int doublePushTo = from + (pushDirection * 2);  // Move 2 squares
                int singlePushTo = from + pushDirection;         // Square in between

                long doublePushMask = 1L << doublePushTo;
                long singlePushMask = 1L << singlePushTo;

                // Both squares must be empty
                if ((allPieces & doublePushMask) == 0 && (allPieces & singlePushMask) == 0) {
                    moves.add(new Move(from, doublePushTo, '\0'));
                }
            }


            // 3. Diagonal captures
            int fromFile = from % 8;
            long enemyPieces = getFriendlyPieces(position, !isWhite);

            //left capture
            int captureLeft = from + (isWhite? 7: -9);
            if (captureLeft >= 0 && captureLeft <64){
                int captureLeftFile = captureLeft % 8;
                // Check file difference is exactly 1 (prevent wrapping)
                if (Math.abs(captureLeftFile - fromFile) == 1) {
                    long captureMask = 1L << captureLeft;
                    if ((enemyPieces & captureMask) != 0) {  // Enemy piece present
                        char captured = position.getPieceAt(captureLeft);

                        // Check for promotion
                        int captureRank = captureLeft / 8;
                        if ((isWhite && captureRank == 7) || (!isWhite && captureRank == 0)) {
                            char[] promotionPieces = isWhite? new char[]{'Q', 'R', 'B', 'N'}:
                                    new char[]{'q', 'r', 'b', 'n'};
                            for (char piece : promotionPieces){
                                Move move = new Move(from, captureLeft, captured);
                                move.setPromotion(piece);
                                moves.add(move);
                            }
                        } else {
                            moves.add(new Move(from, captureLeft, captured));
                        }
                    }
                }
            }

            //right capture
            int captureRight = from + (isWhite? 9: -7);
            if (captureRight >= 0 && captureRight <64){
                int captureRightFile = captureRight % 8;
                // Check file difference is exactly 1 (prevent wrapping)
                if (Math.abs(captureRightFile - fromFile) == 1) {
                    long captureMask = 1L << captureRight;
                    if ((enemyPieces & captureMask) != 0) {  // Enemy piece present
                        char captured = position.getPieceAt(captureRight);

                        // Check for promotion
                        int captureRank = captureRight / 8;
                        if ((isWhite && captureRank == 7) || (!isWhite && captureRank == 0)) {
                            char[] promotionPieces = isWhite? new char[]{'Q', 'R', 'B', 'N'}:
                                    new char[]{'q', 'r', 'b', 'n'};
                            for (char piece : promotionPieces){
                                Move move = new Move(from, captureRight, captured);
                                move.setPromotion(piece);
                                moves.add(move);
                            }
                        } else {
                            moves.add(new Move(from, captureRight, captured));
                        }
                    }
                }
            }


            // 4. En passant
            if (position.enPassantSquare != -1) {
                int epSquare = position.enPassantSquare;
                int epFile = epSquare % 8;

                // Check if pawn can reach the en passant square (diagonally)
                int leftCapture = from + (isWhite ? 7 : -9);
                int rightCapture = from + (isWhite ? 9 : -7);

                if (leftCapture == epSquare && Math.abs(fromFile - epFile) == 1) {
                    // The captured pawn is NOT on epSquare, it's one rank behind
                    int capturedPawnSquare = isWhite ? (epSquare - 8) : (epSquare + 8);
                    char captured = position.getPieceAt(capturedPawnSquare);

                    Move move = new Move(from, epSquare, captured);
                    move.setEnPassant();
                    moves.add(move);
                }

                if (rightCapture == epSquare && Math.abs(fromFile - epFile) == 1) {
                    int capturedPawnSquare = isWhite ? (epSquare - 8) : (epSquare + 8);
                    char captured = position.getPieceAt(capturedPawnSquare);

                    Move move = new Move(from, epSquare, captured);
                    move.setEnPassant();
                    moves.add(move);
                }
            }

        }
    }



    //sliding pieces (rook, queen, bishop)
    private static void generateRookMoves(Position position, List<Move> moves, boolean isWhite) {
        long rooks = isWhite ? position.whiteRook : position.blackRook;
        long friendlyPieces = getFriendlyPieces(position, isWhite);
        long allPieces = getAllPieces(position);

        while (rooks != 0) {
            int from = Long.numberOfTrailingZeros(rooks);
            rooks &= rooks - 1;

            // Generate moves in 4 directions: North, South, East, West
            generateSlidingMoves(from, moves, position, friendlyPieces, allPieces,
                    new int[]{8, -8, 1, -1});  // Direction offsets
        }
    }


    private static void generateBishopMoves(Position position, List<Move> moves, boolean isWhite) {
        long bishops = isWhite ? position.whiteBishop : position.blackBishop;
        long friendlyPieces = getFriendlyPieces(position, isWhite);
        long allPieces = getAllPieces(position);

        while (bishops != 0) {
            int from = Long.numberOfTrailingZeros(bishops);
            bishops &= bishops - 1;

            // Generate moves in 4 diagonal directions
            generateSlidingMoves(from, moves, position, friendlyPieces, allPieces,
                    new int[]{9, 7, -7, -9});  // Northeast, Northwest, Southeast, Southwest
        }
    }

    private static void generateQueenMoves(Position position, List<Move> moves, boolean isWhite) {
        long queens = isWhite ? position.whiteQueen : position.blackQueen;
        long friendlyPieces = getFriendlyPieces(position, isWhite);
        long allPieces = getAllPieces(position);

        while (queens != 0) {
            int from = Long.numberOfTrailingZeros(queens);
            queens &= queens - 1;

            // Queen = Rook + Bishop (8 directions)
            generateSlidingMoves(from, moves, position, friendlyPieces, allPieces,
                    new int[]{8, -8, 1, -1, 9, 7, -7, -9});  // All 8 directions
        }
    }


    //generate sliding moves function
    private static void generateSlidingMoves(int from, List<Move> moves, Position position,
                                             long friendlyPieces, long allPieces, int[] directions) {

        for (int dir : directions) {
            int to = from + dir;

            while (true) {
                // Bounds check
                if (to < 0 || to >= 64) break;

                // Prevent wrapping for horizontal moves
                int toFile = to % 8;
                int prevFile = (to - dir) % 8;  // File of the previous square
                if (dir == 1 && toFile != prevFile + 1) break;  // Moving right
                if (dir == -1 && toFile != prevFile - 1) break; // Moving left

                // Prevent wrapping for diagonal moves
                if (dir == 9 && toFile != prevFile + 1) break;  // Northeast
                if (dir == 7 && toFile != prevFile - 1) break;  // Northwest
                if (dir == -7 && toFile != prevFile + 1) break; // Southeast
                if (dir == -9 && toFile != prevFile - 1) break; // Southwest

                long toMask = 1L << to;

                // Hit a friendly piece? Stop (can't move here)
                if ((friendlyPieces & toMask) != 0) {
                    break;
                }

                // Hit an enemy piece? Capture and stop
                if ((allPieces & toMask) != 0) {
                    char captured = position.getPieceAt(to);
                    moves.add(new Move(from, to, captured));
                    break;
                }

                // Empty square - add move and continue
                moves.add(new Move(from, to, '\0'));

                to += dir;
            }
        }
    }



    private static void generateCastlingMoves(Position position, List<Move> moves, boolean isWhite) {
        long allPieces = getAllPieces(position);

        if (isWhite) {
            // White kingside castling
            if (position.whiteCanCastleKingside) {
                // Check if f1 and g1 are empty (squares 5 and 6)
                long f1 = 1L << 5;
                long g1 = 1L << 6;
                if ((allPieces & f1) == 0 && (allPieces & g1) == 0) {
                    Move move = new Move(4, 6, '\0');  // e1 to g1
                    move.setCastling();
                    moves.add(move);
                }
            }

            // White queenside castling
            if (position.whiteCanCastleQueenside) {
                // Check if b1, c1, and d1 are empty (squares 1, 2, 3)
                long b1 = 1L << 1;
                long c1 = 1L << 2;
                long d1 = 1L << 3;
                if ((allPieces & b1) == 0 && (allPieces & c1) == 0 && (allPieces & d1) == 0) {
                    Move move = new Move(4, 2, '\0');  // e1 to c1
                    move.setCastling();
                    moves.add(move);
                }
            }
        } else {
            // Black kingside castling
            if (position.blackCanCastleKingside) {
                // Check if f8 and g8 are empty (squares 61 and 62)
                long f8 = 1L << 61;
                long g8 = 1L << 62;
                if ((allPieces & f8) == 0 && (allPieces & g8) == 0) {
                    Move move = new Move(60, 62, '\0');  // e8 to g8
                    move.setCastling();
                    moves.add(move);
                }
            }

            // Black queenside castling
            if (position.blackCanCastleQueenside) {
                // Check if b8, c8, and d8 are empty (squares 57, 58, 59)
                long b8 = 1L << 57;
                long c8 = 1L << 58;
                long d8 = 1L << 59;
                if ((allPieces & b8) == 0 && (allPieces & c8) == 0 && (allPieces & d8) == 0) {
                    Move move = new Move(60, 58, '\0');  // e8 to c8
                    move.setCastling();
                    moves.add(move);
                }
            }
        }
    }


    private static long getFriendlyPieces(Position position, boolean isWhite) {
        if (isWhite) {
            // OR together all white piece bitboards
            return position.whitePawn | position.whiteKnight | position.whiteBishop
                    | position.whiteRook | position.whiteQueen | position.whiteKing;
        } else {
            // OR together all black piece bitboards
            return position.blackPawn | position.blackKnight | position.blackBishop
                    | position.blackRook | position.blackQueen | position.blackKing;
        }
    }
}