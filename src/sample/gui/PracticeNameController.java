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
    private Boolean functioning = true;
    private Label scoreLabel;
    private String color;
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


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(()-> titleLabel.getScene().getWindow().setOnCloseRequest(event -> {
            event.consume();
            doneButtonPressed();
        }));

        this.mivLevel.setStyle("-fx-accent: #b7deff");

        Platform.runLater( ()-> {
            this.databaseSquare.setStyle("-fx-background-color: " + this.color);
            this.yourSquare.setStyle("-fx-background-color: " + this.color);
            this.compareSquare.setStyle("-fx-background-color: " + this.color);
            this.titleLabel.setText(this.player.getCurrentPlaylistName());
        });

        Runnable task = new Thread( ()-> {
            try {
                testMicrophoneWorking();
            } catch (Exception e) {
                this.alert.unknownError();
            }
        });
        new Thread((task)).start();

        this.compareSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1));

    }

    public void setScoreLabel(Label label) {
        this.scoreLabel = label;
    }

    private void testMicrophoneWorking() {
        MicrophoneTester tester = new MicrophoneTester();
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
    }

    public void playDatabaseButtonPressed() {
    	this.player.stopAudioPlayback();
        this.player.playCurrentName(this.volume);
    }

    public void recordUserButtonPressed() {
        this.scoreLabel.setText("Current Score: " + score.nameRecorded());
        this.mivLevel.setStyle("-fx-accent: rgba(255,113,133,0.92)");
        
        this.player.stopAudioPlayback();
        this.recordButton.setText("Recording...");
        configureRecordingState(true);
        
        String latestRecordedName = "se206_" + formatter.format(new Date()) + "_" + this.player.getFileNamePart(this.player.getCurrentPlaylistName());
        Runnable task = new Thread( ()-> {
            try {
                this.player.recordAttempt(latestRecordedName);
                Platform.runLater( ()-> {
                	// Enable play, save and compare after recording is made
                    configureRecordingState(false);
                    this.playUserButton.setDisable(false);
                    this.saveUserButton.setDisable(false);
                    this.compareButton.setDisable(false);
                    this.recordButton.setText("Record");
                    this.mivLevel.setStyle("-fx-accent: #b7deff");

                });
            } catch (InterruptedException | IOException e) {
                alert.unknownError();
            }
        });
        new Thread(task).start();
    }
    


    public void playUserButtonPressed() {
        this.player.stopAudioPlayback();
    	this.player.playLatestUserRecording();
    }

    public void saveUserButtonPressed() throws IOException {
    	// Remove any old user recordings for the same name
    	this.player.saveAttempt();
        // Alert user to successful save
        alert.recordingSaved();
    }

    public void doneButtonPressed() {
        this.player.stopAudioPlayback();
        this.functioning = false;
        ((Stage) titleLabel.getScene().getWindow()).close();
    }

    public void compareButtonPressed() {
        this.player.stopAudioPlayback();
    	
    	// Recursively compare
    	this.player.compareRecordings((int) this.compareSpinner.getValue());
    }
    


    public void setColor(String color) {
        this.color = color;
    }

    public void setPlayer(Player player, double volume) {
        this.player = player;
        this.volume = volume;
    }

    public void configureRecordingState(boolean state) {
        this.databaseSquare.setDisable(state);
        this.compareSquare.setDisable(state);
        this.yourSquare.setDisable(state);
    }
}
