package gui;

import javax.swing.Timer;

public class ChessClock {
    int whiteTimeSeconds;
    int blackTimeSeconds;
    boolean whiteTicking;
    Timer ticker;
    Runnable onTick;

    public ChessClock(int minutes, Runnable onTick){
        this.whiteTimeSeconds = minutes * 60;
        this.blackTimeSeconds = minutes * 60;
        this.whiteTicking = true;
        this.onTick = onTick;

        ticker = new Timer(1000, e -> {
            if (whiteTicking) {
                whiteTimeSeconds--;
            } else {
                blackTimeSeconds--;
            }
            onTick.run();
        });
    }

    public void start(){
        ticker.start();
    }

    public void switchClock(){
        whiteTicking = !whiteTicking;
    }

    public void stop(){
        ticker.stop();
    }

    public int getWhiteTimeSeconds(){
        return whiteTimeSeconds;
    }

    public int getBlackTimeSeconds(){
        return blackTimeSeconds;
    }


}
