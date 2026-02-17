package engine;

import java.util.*;

public class Position {

    // 12 bitboards
    public long whitePawn;
    public long whiteKing;
    public long whiteQueen;
    public long whiteKnight;
    public long whiteBishop;
    public long whiteRook;
    public long blackPawn;
    public long blackKing;
    public long blackQueen;
    public long blackKnight;
    public long blackBishop;
    public long blackRook;

    // other state fields
    public boolean isWhiteTurn;
    public int halfMoveCount;

    //castling variables
    public boolean whiteCanCastleKingside = false;
    public boolean whiteCanCastleQueenside = false;
    public boolean blackCanCastleKingside = false;
    public boolean blackCanCastleQueenside = false;

    //en-passant square
    public int enPassantSquare;

    // functions starts from here
    public Position(String fen) {
        String[] parts = fen.split(" ");

        isWhiteTurn = parts[1].equals("w");

        if (parts[2].contains("K")) whiteCanCastleKingside = true;
        if (parts[2].contains("Q")) whiteCanCastleQueenside = true;
        if (parts[2].contains("k")) blackCanCastleKingside = true;
        if (parts[2].contains("q")) blackCanCastleQueenside = true;

        if (parts[3].contains("-")) {
            enPassantSquare = -1;
        } else {
            //some code to set en-passant value
            int file = parts[3].charAt(0) - 'a';
            int rank = parts[3].charAt(1) - '1';
            enPassantSquare = rank * 8 + file;
        }

        halfMoveCount = Integer.parseInt(parts[4]);

        //pieces placements
        String piecePlacement = parts[0];
        String[] ranks = piecePlacement.split("/");

        for (int rankIndex = 0; rankIndex < 8; rankIndex++) {
            String rank = ranks[rankIndex];

            int square = (7 - rankIndex) * 8;
            for (char c : rank.toCharArray()) {
                if (Character.isDigit(c)) {
                    square += (c - '0');
                } else {
                    // place the piece in board
                    switch (c) {
                        case 'P':
                            whitePawn |= (1L << square);
                            break;
                        case 'p':
                            blackPawn |= (1L << square);
                            break;
                        case 'R':
                            whiteRook |= (1L << square);
                            break;
                        case 'r':
                            blackRook |= (1L << square);
                            break;
                        case 'N':
                            whiteKnight |= (1L << square);
                            break;
                        case 'n':
                            blackKnight |= (1L << square);
                            break;
                        case 'B':
                            whiteBishop |= (1L << square);
                            break;
                        case 'b':
                            blackBishop |= (1L << square);
                            break;
                        case 'K':
                            whiteKing |= (1L << square);
                            break;
                        case 'k':
                            blackKing |= (1L << square);
                            break;
                        case 'Q':
                            whiteQueen |= (1L << square);
                            break;
                        case 'q':
                            blackQueen |= (1L << square);
                            break;
                    }
                    square++;
                }
            }
        }
    }


    public char getPieceAt(int square) {
        long mask = 1L << square;

        if ((whitePawn & mask) != 0) return 'P';
        if ((blackPawn & mask) != 0) return 'p';
        if ((whiteRook & mask) != 0) return 'R';
        if ((blackRook & mask) != 0) return 'r';
        if ((whiteKnight & mask) != 0) return 'N';
        if ((blackKnight & mask) != 0) return 'n';
        if ((whiteBishop & mask) != 0) return 'B';
        if ((blackBishop & mask) != 0) return 'b';
        if ((whiteKing & mask) != 0) return 'K';
        if ((blackKing & mask) != 0) return 'k';
        if ((whiteQueen & mask) != 0) return 'Q';
        if ((blackQueen & mask) != 0) return 'q';
        return '.'; //empty square
    }


    public GameState makeMove(Move move) {
        // 1. Save current state (for unmake)
        GameState savedState = new GameState(this);

        // 2. Get piece type at source square
        char piece = getPieceAt(move.from);

        // 3. Remove piece from source bitboard
        removePiece(move.from, piece);

        // 4. Handle captures (remove captured piece)
        if (move.capturedPiece != '\0' && move.capturedPiece != '.') {
            if (move.isEnPassant) {
                // En passant: captured pawn is NOT on the destination square
                int capturedSquare = isWhiteTurn ? (move.to - 8) : (move.to + 8);
                removePiece(capturedSquare, move.capturedPiece);
            } else {
                // Normal capture: remove from destination
                removePiece(move.to, move.capturedPiece);
            }
        }

        // 5. Place piece at destination (or promoted piece)
        if (move.isPromotion) {
            addPiece(move.to, move.promotionPiece);
        } else {
            addPiece(move.to, piece);
        }

        // 6. Handle castling (move the rook)
        if (move.isCastling) {
            handleCastlingRookMove(move.from, move.to);
        }

        // 7. Update castling rights (lose rights if king or rook moves)
        if (piece == 'K') {
            whiteCanCastleKingside = false;
            whiteCanCastleQueenside = false;
        } else if (piece == 'k') {
            blackCanCastleKingside = false;
            blackCanCastleQueenside = false;
        }

        // If rook moves from starting square, lose that side's castling
        if (move.from == 0) whiteCanCastleQueenside = false;  // a1
        if (move.from == 7) whiteCanCastleKingside = false;   // h1
        if (move.from == 56) blackCanCastleQueenside = false; // a8
        if (move.from == 63) blackCanCastleKingside = false;  // h8

        // 8. Update en passant square
        if (piece == 'P' && move.to - move.from == 16) {
            // White pawn moved 2 squares
            enPassantSquare = move.from + 8;
        } else if (piece == 'p' && move.from - move.to == 16) {
            // Black pawn moved 2 squares
            enPassantSquare = move.from - 8;
        } else {
            enPassantSquare = -1;
        }

        // 9. Update half move clock (reset on capture or pawn move)
        if (move.capturedPiece != '\0' || piece == 'P' || piece == 'p') {
            halfMoveCount = 0;
        } else {
            halfMoveCount++;
        }

        // 10. Switch turns
        isWhiteTurn = !isWhiteTurn;

        return savedState;
    }


    public void unmakeMove(Move move, GameState savedState) {
        // 1. Switch turns back
        isWhiteTurn = !isWhiteTurn;

        // 2. Get the piece that's currently at the destination
        char piece = getPieceAt(move.to);

        // 3. If it was a promotion, the piece at 'to' is the promoted piece
        //    We need to put back the original pawn
        if (move.isPromotion) {
            removePiece(move.to, piece);  // Remove promoted piece
            char originalPawn = isWhiteTurn ? 'P' : 'p';
            addPiece(move.from, originalPawn);  // Put pawn back at source
        } else {
            // Normal move: move piece back
            removePiece(move.to, piece);
            addPiece(move.from, piece);
        }

        // 4. Restore captured piece
        if (move.capturedPiece != '\0' && move.capturedPiece != '.') {
            if (move.isEnPassant) {
                // En passant: restore pawn to its actual square
                int capturedSquare = isWhiteTurn ? (move.to - 8) : (move.to + 8);
                addPiece(capturedSquare, move.capturedPiece);
            } else {
                // Normal capture: restore to destination square
                addPiece(move.to, move.capturedPiece);
            }
        }

        // 5. Undo castling (move rook back)
        if (move.isCastling) {
            undoCastlingRookMove(move.from, move.to);
        }

        // 6. Restore saved state
        whiteCanCastleKingside = savedState.whiteCanCastleKingside;
        whiteCanCastleQueenside = savedState.whiteCanCastleQueenside;
        blackCanCastleKingside = savedState.blackCanCastleKingside;
        blackCanCastleQueenside = savedState.blackCanCastleQueenside;
        enPassantSquare = savedState.enPassantSquare;
        halfMoveCount = savedState.halfMoveCount;
    }


    private void undoCastlingRookMove(int kingFrom, int kingTo) {
        // White kingside: move rook back from f1 (5) to h1 (7)
        if (kingFrom == 4 && kingTo == 6) {
            whiteRook &= ~(1L << 5);  // Remove from f1
            whiteRook |= (1L << 7);   // Add back to h1
        }
        // White queenside: move rook back from d1 (3) to a1 (0)
        if (kingFrom == 4 && kingTo == 2) {
            whiteRook &= ~(1L << 3);
            whiteRook |= (1L);
        }
        // Black kingside: move rook back from f8 (61) to h8 (63)
        if (kingFrom == 60 && kingTo == 62) {
            blackRook &= ~(1L << 61);
            blackRook |= (1L << 63);
        }
        // Black queenside: move rook back from d8 (59) to a8 (56)
        if (kingFrom == 60 && kingTo == 58) {
            blackRook &= ~(1L << 59);
            blackRook |= (1L << 56);
        }
    }


    private void removePiece(int square, char piece) {
        long mask = ~(1L << square);  // NOT of the bit at square

        switch (piece) {
            case 'P':
                whitePawn &= mask;
                break;
            case 'p':
                blackPawn &= mask;
                break;
            case 'N':
                whiteKnight &= mask;
                break;
            case 'n':
                blackKnight &= mask;
                break;
            case 'R':
                whiteRook &= mask;
                break;
            case 'r':
                blackRook &= mask;
                break;
            case 'B':
                whiteBishop &= mask;
                break;
            case 'b':
                blackBishop &= mask;
                break;
            case 'K':
                whiteKing &= mask;
                break;
            case 'k':
                blackKing &= mask;
                break;
            case 'Q':
                whiteQueen &= mask;
                break;
            case 'q':
                blackQueen &= mask;
                break;
        }
    }

    private void addPiece(int square, char piece) {
        long mask = 1L << square;

        switch (piece) {
            case 'P':
                whitePawn |= mask;
                break;
            case 'p':
                blackPawn |= mask;
                break;
            case 'N':
                whiteKnight |= mask;
                break;
            case 'n':
                blackKnight |= mask;
                break;
            case 'R':
                whiteRook |= mask;
                break;
            case 'r':
                blackRook |= mask;
                break;
            case 'B':
                whiteBishop |= mask;
                break;
            case 'b':
                blackBishop |= mask;
                break;
            case 'K':
                whiteKing |= mask;
                break;
            case 'k':
                blackKing |= mask;
                break;
            case 'Q':
                whiteQueen |= mask;
                break;
            case 'q':
                blackQueen |= mask;
                break;
        }
    }

    private void handleCastlingRookMove(int kingFrom, int kingTo) {
        // White kingside: move rook from h1 (7) to f1 (5)
        if (kingFrom == 4 && kingTo == 6) { //white king side castling
            whiteRook &= ~(1L << 7);  // Remove from h1
            whiteRook |= (1L << 5);   // Add to f1
        }
        if (kingFrom == 4 && kingTo == 2) { //white queen side castling
            whiteRook &= ~(1L);
            whiteRook |= (1L << 3);
        }
        if (kingFrom == 60 && kingTo == 62) { //black king side castling
            blackRook &= ~(1L << 63);
            blackRook |= (1L << 61);
        }
        if (kingFrom == 60 && kingTo == 58) { //black queen side castling
            blackRook &= ~(1L << 56);
            blackRook |= (1L << 59);
        }
    }


    public boolean isKingInCheck(boolean isWhite) {
        // 1. Find the king's position
        long king = isWhite ? whiteKing : blackKing;
        if (king == 0) return false;  // No king (shouldn't happen)

        int kingSquare = Long.numberOfTrailingZeros(king);

        // 2. Temporarily switch turns to generate opponent's moves
        boolean originalTurn = isWhiteTurn;
        isWhiteTurn = !isWhite;  // Switch to opponent's turn

        // 3. Generate all opponent's moves
        List<Move> opponentMoves = MoveGenerator.generateMoves(this);

        // 4. Restore the turn
        isWhiteTurn = originalTurn;

        // 5. Check if any opponent move attacks the king square
        for (Move move : opponentMoves) {
            if (move.to == kingSquare) {
                return true;  // King is under attack!
            }
        }

        return false;  // King is safe
    }


    public String toFEN() {
        StringBuilder fen = new StringBuilder();

        // 1. Piece placement
        for (int rank = 7; rank >= 0; rank--) {
            int emptyCount = 0;
            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                char piece = getPieceAt(square);
                if (piece == '.') {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(piece);
                }
            }
            if (emptyCount > 0) fen.append(emptyCount);
            if (rank > 0) fen.append('/');
        }

        // 2. Active color
        fen.append(isWhiteTurn ? " w " : " b ");

        // 3. Castling rights
        String castling = "";
        if (whiteCanCastleKingside) castling += "K";
        if (whiteCanCastleQueenside) castling += "Q";
        if (blackCanCastleKingside) castling += "k";
        if (blackCanCastleQueenside) castling += "q";
        fen.append(castling.isEmpty() ? "-" : castling).append(" ");

        // 4. En passant
        if (enPassantSquare == -1) {
            fen.append("- ");
        } else {
            int file = enPassantSquare % 8;
            int rank = enPassantSquare / 8;
            fen.append((char) ('a' + file)).append(rank + 1).append(" ");
        }

        // 5. Half move clock
        fen.append(halfMoveCount).append(" ");

        // 6. Full move number (just use 1 for simplicity)
        fen.append("1");

        return fen.toString();
    }
}