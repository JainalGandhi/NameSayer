package sample.model;

import sample.gui.PopupAlert;

public class Score {

    private int STARTING_SCORE_FOR_NAME = 3;
    private final static String[] colorProgression = new String[]{"rgba(216,233,238,0.91)", "rgba(238,169,171,0.75)", "rgba(255,0,22,0.68)", "rgba(197,238,188,0.91)", "rgba(142,30,255,0.4)"};
    private final static int maxLevel = colorProgression.length-1;

    private int level = 0;
    private int nextGoal = 10;
    private static Score scoreInstance;
    private int currentScore;
    private int timesCurrentNameRecordedCountdown;
    private PopupAlert alert = new PopupAlert();

    /**
     * Class holds and maintains the current score of the game and the rewarding of skins.
     * Utilises singleton design pattern to ensure the color reward is always the same
     */
    private Score() {
        this.currentScore = 0;
        this.timesCurrentNameRecordedCountdown = STARTING_SCORE_FOR_NAME;
    }

    /**
     * Gets the singleton instance of the class
     * @return the single Score instance
     */
    public static Score getInstance() {
        if(scoreInstance == null){
            scoreInstance = new Score();
        }
        return scoreInstance;
    }

    /**
     * Updates score to reflect a new name being requested.
     * @return current score after updating to reflect new name request
     */
    public int differentNameRequested() {
        //If previous name had not been practiced, will reset score to 0
        if(this.timesCurrentNameRecordedCountdown == STARTING_SCORE_FOR_NAME){
            this.currentScore = 0;
        }
        this.timesCurrentNameRecordedCountdown = STARTING_SCORE_FOR_NAME;

        //Returns current score
        return this.currentScore;
    }

    /**
     * Updates score to reflect a recording being completed
     * @return current score after updating to reflect recording complete
     */
    public int nameRecorded() {
        //Adds points if recording is made and points are still eligible (ie. 3 recordings for current name has not beein made)
        if(this.timesCurrentNameRecordedCountdown > 0) {
            this.currentScore+=this.timesCurrentNameRecordedCountdown;
            this.timesCurrentNameRecordedCountdown--;
        }
        //Returns current score
        return this.currentScore;
    }

    /**
     * Returns the current score to the user
     * @return current score
     */
    public int getCurrentScore() {
        return this.currentScore;
    }

    /**
     * Returns the current color selected
     * @return current color
     */
    public String getColor() {
        //Checks if new skin has been unlocked and user wishes to equip
        if(this.level!=maxLevel && getCurrentScore()>=nextGoal){
            //Increases the score required for the next level
            this.nextGoal+=this.nextGoal;
            if(this.alert.equipNewLevelRequest()) {
                this.level++;
            }
        }
        //Returns current color level
        return colorProgression[this.level];
    }
}