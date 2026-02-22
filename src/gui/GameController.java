package gui;

import engine.Move;
import engine.MoveGenerator;
import engine.Position;
import engine.Search;

import javax.swing.*;

public class GameController {
    //fields
    private Position position;
    private Move lastMove;
    private Runnable onMoveComplete;
    private boolean playerIsWhite;
    private boolean playingAgainstAI;

    public GameController(boolean playerIsWhite, Runnable onMoveComplete){
        this.playerIsWhite = playerIsWhite;
        this.onMoveComplete = onMoveComplete;
        this.playingAgainstAI = true;
        this.position = new Position("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }


    //getter methods
    public Position getPosition() { return position;}
    public Move getLastMove() { return lastMove;}
    public boolean isPlayerWhite(){ return playerIsWhite;}
    public boolean isPlayerTurn(){return position.isWhiteTurn == playerIsWhite;}


    public String isGameOver(){
        if (MoveGenerator.generateLegalMoves(position).isEmpty()){
            if (position.isKingInCheck(position.isWhiteTurn)){
                return "Checkmate!";
            } else {
                return  "Stalemate!";
            }
        }
        return null;
    }


    public void makeAIMove() {
        // Make a copy of the position for search!
        final String currentFEN = position.toFEN();

        SwingWorker<Move, Void> worker = new SwingWorker<>() {
            @Override
            protected Move doInBackground() {
                // Search on a COPY, not the GUI's position!
                Position searchPosition = new Position(currentFEN);
                return Search.findBestMove(searchPosition, 3);
            }

            @Override
            protected void done() {
                try {
                    Move aiMove = get();
                    if (aiMove != null) {
                        position.makeMove(aiMove);  // Only modify GUI position once
                        lastMove = aiMove;

                        onMoveComplete.run();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    public void makeMove(Move move) {
        position.makeMove(move);
        lastMove = move;

        onMoveComplete.run();

        // AI move
        if (playingAgainstAI && position.isWhiteTurn != playerIsWhite) {
            makeAIMove();
        }
    }

}
