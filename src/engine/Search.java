package engine;

import java.util.ArrayList;
import java.util.List;

public class Search {

    // Find the best move for the current position
    public static Move findBestMove(Position position, int depth) {
        List<Move> legalMoves = MoveGenerator.generateLegalMoves(position);

        if (legalMoves.isEmpty()) {
            return null;
        }

        Move bestMove = null;
        boolean isWhiteTurn = position.isWhiteTurn;  // ← Save turn BEFORE loop
        int bestScore = isWhiteTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move move : legalMoves) {
            GameState saved = position.makeMove(move);
            int score = minimax(position, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            position.unmakeMove(move, saved);

            // Update best move
            if (isWhiteTurn) {  // ← Use saved turn
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
        }

        return bestMove;
    }

    // Minimax with alpha-beta pruning
    private static int minimax(Position position, int depth, int alpha, int beta) {
        // Base case: reached depth limit or game over
        if (depth == 0) {
            return quiesce(position, alpha, beta, 5);
        }

        List<Move> legalMoves = MoveGenerator.generateLegalMoves(position);

        if (legalMoves.isEmpty()) {
            if (position.isKingInCheck(position.isWhiteTurn)) {
                // Checkmate
                return position.isWhiteTurn
                        ? Integer.MIN_VALUE + depth
                        : Integer.MAX_VALUE - depth;
            } else {
                // Stalemate
                return 0;
            }
        }

        if (position.isWhiteTurn) {
            // Maximizing player (white)
            int maxScore = Integer.MIN_VALUE;
            for (Move move : legalMoves) {
                GameState saved = position.makeMove(move);
                int score = minimax(position, depth - 1, alpha, beta);
                position.unmakeMove(move, saved);

                maxScore = Math.max(maxScore, score);
                alpha = Math.max(alpha, score);

                if (alpha >= beta) {
                    break;  // Beta cutoff (pruning!)
                }
            }
            return maxScore;
        } else {
            // Minimizing player (black)
            int minScore = Integer.MAX_VALUE;
            for (Move move : legalMoves) {
                GameState saved = position.makeMove(move);
                int score = minimax(position, depth - 1, alpha, beta);
                position.unmakeMove(move, saved);

                minScore = Math.min(minScore, score);
                beta = Math.min(beta, score);

                if (alpha >= beta) {
                    break;  // Beta cutoff (pruning!)
                }
            }
            return minScore;
        }
    }



    private static int quiesce(Position position, int alpha, int beta, int depth){
        //get static evaluation of current position
        int standPat = Evaluator.evaluate(position);

        if (depth <= 0){
            return standPat;
        }

        // If this position is already too good (for the side to move),
        // we can prune—no need to search further
        if (position.isWhiteTurn) {
            if (standPat >= beta) {
                return beta;
            }
            if (standPat > alpha) {
                alpha = standPat;
            }
        } else {
            if (standPat <= alpha) {
                return alpha;
            }
            if (standPat < beta) {
                beta = standPat;
            }
        }

        List<Move> allMoves = MoveGenerator.generateMoves(position);
        List<Move> forcingMoves = new ArrayList<>();
        for (Move move : allMoves) {
            if (move.isCapture()) {
                forcingMoves.add(move);
            }
        }

        // If no forcing moves, return the standing pat score
        if (forcingMoves.isEmpty()) {
            return standPat;
        }

        if (position.isWhiteTurn) {
            int maxScore = standPat;  // Start with standing pat

            for (Move move : forcingMoves) {
                GameState saved = position.makeMove(move);
                int score = quiesce(position, alpha, beta, depth -1);  // Recursive call
                position.unmakeMove(move, saved);

                maxScore = Math.max(maxScore, score);
                alpha = Math.max(alpha, score);

                if (alpha >= beta) {
                    break;  // Beta cutoff
                }
            }
            return maxScore;
        } else {
            int minScore = standPat;  // Start with standing pat

            for (Move move : forcingMoves) {
                GameState saved = position.makeMove(move);
                int score = quiesce(position, alpha, beta, depth -1);  // Recursive call
                position.unmakeMove(move, saved);

                minScore = Math.min(minScore, score);
                beta = Math.min(beta, score);

                if (alpha >= beta) {
                    break;  // Beta cutoff
                }
            }
            return minScore;
        }

    }
}