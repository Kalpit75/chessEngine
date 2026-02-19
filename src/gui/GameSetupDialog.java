package gui;

import javax.swing.*;

public class GameSetupDialog {

    public static boolean askPlayerColor() {
        String[] options = {"White", "Black"};
        int playerIsWhite = JOptionPane.showOptionDialog(null, "Play as:", "Game Setup", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        return playerIsWhite == 0;

    }
}