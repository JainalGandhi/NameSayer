package sample.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import sample.model.MicrophoneTester;
import sample.model.Player;
import sample.model.Score;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class PracticeNameController implements Initializable {

    private PopupAlert alert = new PopupAlert();
    private Boolean functioning;
    private Label scoreLabel;
    private Player player;


    @FXML private AnchorPane databaseSquare;
    @FXML private AnchorPane yourSquare;
    @FXML private AnchorPane compareSquare;
    @FXML private Label titleLabel;
    @FXML private ProgressBar mivLevel;
    @FXML private Button playUserButton;
    @FXML private Button saveUserButton;
    @FXML private Button compareButton;
    @FXML private Button recordButton;
    @FXML private Spinner<Integer> compareSpinner;

    private Score score = Score.getInstance();
    private double volume;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");

    /**
     * Sets the initial requirements of the componenetes of the scene on startup of the stage
     * @param location unused
     * @param resources unused
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Overrides stage closure so it stops audio playback
        Platform.runLater(()-> this.titleLabel.getScene().getWindow().setOnCloseRequest(event -> {
            event.consume();
            doneButtonPressed();
        }));

        //Updates style of the progress bad color
        this.mivLevel.setStyle("-fx-accent: #b7deff");

        //Sets color style of the squares to reflect current score
        Platform.runLater( ()-> {
            this.databaseSquare.setStyle("-fx-background-color: " + this.score.getColor());
            this.yourSquare.setStyle("-fx-background-color: " + this.score.getColor());
            this.compareSquare.setStyle("-fx-background-color: " + this.score.getColor());
            this.titleLabel.setText(this.player.getCurrentPlaylistName());
        });

        //Starts microphone tester
        testMicrophoneWorking();

        //Sets inital value and range of compareSpinner
        this.compareSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1));

    }

    /**
     * Sets the lable information before loading of the stage. To be used to reflect score updates
     * @param label label on main gui window to hold score
     */
    public void setScoreLabel(Label label) {
        this.scoreLabel = label;
    }

    /**
     * Lgoic for updating progress bar to reflect audio level
     */
    private void testMicrophoneWorking() {
        Runnable task = new Thread( ()-> {
            try {
                MicrophoneTester tester = new MicrophoneTester();
                this.functioning = true;
                while(this.functioning) {
                    try {
                        double maxVolume = tester.determineFunctioning();
                        this.mivLevel.setProgress((-91-maxVolume)/-91);
                    }catch(IOException | InterruptedException e){
                        Platform.runLater(()-> {
                            this.functioning = false;
                            this.alert.unknownError();
                        });
                    }
                }
            } catch(Exception e){
            }
        });
        new Thread((task)).start();
    }

    /**
     * Plays the database recording on button press
     */
    public void playDatabaseButtonPressed() {
    	this.player.stopAudioPlayback();
        this.player.playCurrentName(this.volume);
    }

    /**
     * One record button pressed, will disable the gui and record the user recording
     */
    public void recordUserButtonPressed() {
        if(this.recordButton.getText().equals("Record")) {
            //Upadates score to reflect new recording made
            this.scoreLabel.setText("Current Score: " + score.nameRecorded());

            //Changes color of miclevel to show recording in progress
            this.mivLevel.setStyle("-fx-accent: rgba(255,113,133,0.92)");

            //Puts gui in recoridng state
            this.player.stopAudioPlayback();
            this.recordButton.setText("Stop");
            configureRecordingState(true);

            //Records the user
            String latestRecordedName = "se206_" + formatter.format(new Date()) + "_" + this.player.getFileNamePart(this.player.getCurrentPlaylistName());
            Runnable task = new Thread(() -> {
                try {
                    this.player.recordAttempt(latestRecordedName);
                    Platform.runLater(() -> {
                        // Enables gui on recording complete
                        configureRecordingState(false);
                        this.playUserButton.setDisable(false);
                        this.saveUserButton.setDisable(false);
                        this.compareButton.setDisable(false);
                        this.recordButton.setText("Record");
                        this.functioning=false;
                        testMicrophoneWorking();
                        this.mivLevel.setStyle("-fx-accent: #b7deff");
                    });
                } catch (IOException | InterruptedException e) {
                    alert.unknownError();
                }
            });
            new Thread(task).start();
        }else {
            this.recordButton.setText("Record");
            Runnable task = new Thread(() -> {
                try {
                    this.player.stopRecordAttempt();
                } catch (InterruptedException | IOException e) {
                    alert.unknownError();
                }
            });
            new Thread(task).start();
        }
    }


    /**
     * Plays the user recording on button press
     */
    public void playUserButtonPressed() {
        Runnable task = new Thread(()-> {
            this.player.stopAudioPlayback();
            this.player.playLatestUserRecording();
        });
        new Thread(task).start();
    }

    /**
     * Saves user recording into the user file. Displays alert upon success
     */
    public void saveUserButtonPressed() {
        Runnable task = new Thread(()-> {
            // Remove any old user recordings for the same name
            try {
                this.player.saveAttempt();
                Platform.runLater(()-> this.alert.recordingSaved());
            } catch (IOException e) {
                Platform.runLater(()-> this.alert.unknownError());
            }
        });
        new Thread(task).start();
    }

    /**
     * Closes window on done button pressed
     */
    public void doneButtonPressed() {
        this.player.stopAudioPlayback();
        this.functioning = false;
        if(this.recordButton.getText().equals("Stop")){
            try {
                this.player.stopRecordAttempt();
            } catch (InterruptedException | IOException e) { }
        }
        ((Stage) titleLabel.getScene().getWindow()).close();
    }

    /**
     * On compare button pressed, will compare the database name and user recording the selected number of times
     */
    public void compareButtonPressed() {
        this.player.stopAudioPlayback();
        Runnable task = new Thread(()-> {
            // Recursively compare
            this.player.compareRecordings(this.compareSpinner.getValue());
        });
        new Thread(task).start();
    }

    /**
     * Sets the player information before loading of the stage
     * @param player the player instance
     * @param volume the current set volume level
     */
    public void setPlayer(Player player, double volume) {
        this.player = player;
        this.volume = volume;
    }

    /**
     * Configures Gui for recording of a name. This disables/enables databaseSqaure, compareSquare and yourSquare panes
     * @param state disables if true, enables if false
     */
    public void configureRecordingState(boolean state) {
        this.databaseSquare.setDisable(state);
        this.compareSquare.setDisable(state);
        //this.yourSquare.setDisable(state);
    }
}