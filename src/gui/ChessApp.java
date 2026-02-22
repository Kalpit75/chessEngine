package gui;

import javax.swing.*;

public class ChessApp {
    public static void main(String[] args) {
        boolean playerIsWhite = GameSetupDialog.askPlayerColor();
        JFrame frame = new JFrame("Chess Engine");
        BoardPanel board = new BoardPanel(playerIsWhite);
        frame.add(board);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
