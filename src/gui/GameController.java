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
    private final ChessClock clock;

    public GameController(boolean playerIsWhite, Runnable onMoveComplete, Runnable onTick){
        clock = new ChessClock(3, onTick);
        clock.start();

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
    public ChessClock getClock() {return clock;}


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
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
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
                        clock.switchClock();

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
        clock.switchClock();
        onMoveComplete.run();

        // AI move
        if (playingAgainstAI && position.isWhiteTurn != playerIsWhite) {
            makeAIMove();
        }
    }

}
