package sample.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import sample.model.MicrophoneTester;
import sample.model.Score;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PracticeNameController implements Initializable {

    private PopupAlert alert = new PopupAlert();
    private Boolean functioning = true;
    private Label scoreLabel;


    @FXML private Label titleLabel;
    @FXML private ProgressBar mivLevel;
    @FXML private Button playUserButton;
    @FXML private Button saveUserButton;
    @FXML private Button compareButton;
    @FXML private Spinner<Integer> compareSpinner;
    private Score score = Score.getInstance();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(()-> titleLabel.getScene().getWindow().setOnCloseRequest(event -> {
            event.consume();
            doneButtonPressed();
        }));

        this.mivLevel.setStyle("-fx-accent: #b7deff");

        Runnable task = new Thread( ()-> {
            try {
                testMicrophoneWorking();
            } catch (Exception e) {
                this.alert.unkownError();
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
                    this.alert.unkownError();
                });
            }
        }
    }

    public void playDatabaseButtonPressed() {

    }

    public void recordUserButtonPressed() {
        this.scoreLabel.setText("Current Score: " + score.nameRecorded());
        this.mivLevel.setStyle("-fx-accent: rgba(255,113,133,0.92)");
        this.playUserButton.setDisable(false);
        this.saveUserButton.setDisable(false);
        this.compareButton.setDisable(false);
        this.mivLevel.setStyle("-fx-accent: rgba(255,113,133,0.92)");
    }

    public void playUserButtonPressed() {
    }

    public void saveUserButtonPressed() {
    }

    public void doneButtonPressed() {
        this.functioning = false;
        ((Stage) titleLabel.getScene().getWindow()).close();
    }

    public void compareButtonPressed() {
    }
}
