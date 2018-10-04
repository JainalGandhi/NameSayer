package sample.model;

public class Score {

    private int STARTING_SCORE_FOR_NAME = 3;

    private static Score scoreInstance;
    private int currentScore;
    private int timesCurrentNameRecordedCountdown;

    private Score() {
        this.currentScore = 0;
        this.timesCurrentNameRecordedCountdown = STARTING_SCORE_FOR_NAME;
    }

    public static Score getInstance() {
        if(scoreInstance == null){
            scoreInstance = new Score();
        }
        return scoreInstance;
    }

    public int differentNameRequested() {
        if(this.timesCurrentNameRecordedCountdown == STARTING_SCORE_FOR_NAME){
            this.currentScore = 0;
        }
        this.timesCurrentNameRecordedCountdown = STARTING_SCORE_FOR_NAME;
        return this.currentScore;
    }

    public int nameRecorded() {
        if(this.timesCurrentNameRecordedCountdown > 0) {
            this.currentScore+=this.timesCurrentNameRecordedCountdown;
            this.timesCurrentNameRecordedCountdown--;
        }
        return this.currentScore;
    }

}