package gui;

import javax.swing.*;
import java.awt.*;

public class ChessApp {

    public static void main(String[] args) {
        boolean playerIsWhite = GameSetupDialog.askPlayerColor();

        ClockPanel topClock = new ClockPanel();
        ClockPanel bottomClock = new ClockPanel();

        JFrame frame = new JFrame("Chess Engine");

        BoardPanel[] boardRef = new BoardPanel[1];

        boardRef[0] = new BoardPanel(playerIsWhite, () -> {
            ChessClock clock = boardRef[0].getController().getClock();
            if (playerIsWhite) {
                topClock.updateTime(clock.getBlackTimeSeconds());
                bottomClock.updateTime(clock.getWhiteTimeSeconds());
            } else {
                topClock.updateTime(clock.getWhiteTimeSeconds());
                bottomClock.updateTime(clock.getBlackTimeSeconds());
            }
        });

        BoardPanel board = boardRef[0];

        frame.setLayout(new BorderLayout());
        frame.add(topClock, BorderLayout.NORTH);
        frame.add(board, BorderLayout.CENTER);
        frame.add(bottomClock, BorderLayout.SOUTH);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
