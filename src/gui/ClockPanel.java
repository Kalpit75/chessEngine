package gui;

import javax.swing.*;

public class ClockPanel extends JPanel {
    private JLabel timeLabel;

    public ClockPanel(){
        timeLabel = new JLabel("3:00");
        add(timeLabel);

    }

    public void updateTime(int timeSeconds){
        int seconds = timeSeconds % 60;
        int minutes = timeSeconds / 60;
        String formatted = minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
        timeLabel.setText(formatted);
    }
}
