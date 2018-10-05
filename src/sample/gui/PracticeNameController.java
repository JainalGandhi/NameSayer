package sample.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import sample.model.MicrophoneTester;
import sample.model.Player;
import sample.model.Score;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private String latestRecordedName;
    private MediaPlayer mediaPlayer;
    private File currentDatabaseWavFile;
    private int compareCount;


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
    	this.player.stopAudioPlayback();
        this.player.playCurrentName(this.volume);
    }

    public void recordUserButtonPressed() {
        this.scoreLabel.setText("Current Score: " + score.nameRecorded());
        this.mivLevel.setStyle("-fx-accent: rgba(255,113,133,0.92)");
        this.mivLevel.setStyle("-fx-accent: #b7deff");
        
        this.player.stopAudioPlayback();
        this.recordButton.setText("Recording...");
        this.recordButton.setDisable(true);
        
        this.latestRecordedName = "se206_" + formatter.format(new Date()) + "_" + this.player.getCurrentPlaylistName().replaceAll(" ", "_");
        Runnable task = new Thread( ()-> {
            try {
                recordAttempt();
                Platform.runLater( ()-> {
                	// Enable play, save and compare after recording is made
                	this.recordButton.setDisable(false);
                	this.playUserButton.setDisable(false);
                    this.saveUserButton.setDisable(false);
                    this.compareButton.setDisable(false);
                });
            } catch (InterruptedException | IOException e) {
                alert.unkownError();
            }
        });
        new Thread(task).start();
    }
    
    public void recordAttempt() throws InterruptedException, IOException {
        String RECORD_NAME_ATTEMPT_COMMAND = "rm names/temp/temp.wav;"
        		+ " ffmpeg -f alsa -i default -t 5 -acodec pcm_s16le -ar 22050 -ac 1 ./names/temp/temp.wav 2> /dev/null; " +
                "ffmpeg -hide_banner -i ./names/temp/temp.wav -af silenceremove=0:0:0:-1:2:-45dB ./names/temp/" + this.latestRecordedName + ".wav 2> /dev/null";
        Process process = new ProcessBuilder("/bin/bash", "-c", RECORD_NAME_ATTEMPT_COMMAND).start();
        process.waitFor();
    }

    public void playUserButtonPressed() {
    	this.player.stopAudioPlayback();
    	String path = System.getProperty("user.dir") + "/names/temp/" + this.latestRecordedName + ".wav";
    	this.mediaPlayer = new MediaPlayer(new Media(new File(path).toURI().toString()));
    	this.mediaPlayer.play();
    }

    public void saveUserButtonPressed() throws IOException {
    	// Remove any old user recordings for the same name
    	String searchUser = "ls names/user | grep -i [[:digit:]]_" + this.player.getCurrentPlaylistName().replaceAll(" ", "_") + ".wav";
		Process process = new ProcessBuilder("/bin/bash", "-c", searchUser).start();
		InputStream stdout = process.getInputStream();
		BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
		String creation;
		while ((creation = stdoutBuffered.readLine()) != null) {
			String delete = "rm names/user/" + creation;
	    	ProcessBuilder process2 = new ProcessBuilder("/bin/bash", "-c", delete);
	    	process2.start();
		}
    	
    	// Save current recording in names/user
    	String saveCommand = "cp ./names/temp/" + this.latestRecordedName + ".wav ./names/user";
    	ProcessBuilder process3 = new ProcessBuilder("/bin/bash", "-c", saveCommand);
		process3.start();
    }

    public void doneButtonPressed() {
        this.functioning = false;
        ((Stage) titleLabel.getScene().getWindow()).close();
    }

    public void compareButtonPressed() {
    	this.currentDatabaseWavFile = this.player.getCurrentWav();
    	this.player.stopAudioPlayback();
    	int compare = compareSpinner.getValue();
    	this.compareCount = compare;
    	
    	// Recursively compare
    	compareRecordings();
    }
    
    public void compareRecordings() {
    	if (this.compareCount == 0) {
    		return;
    	}
    	
    	this.compareCount--;
    	this.mediaPlayer = new MediaPlayer(new Media(this.currentDatabaseWavFile.toURI().toString()));
		this.mediaPlayer.play();
		
		this.mediaPlayer.setOnEndOfMedia(new Runnable() {
			@Override
			public void run() {
				String path = System.getProperty("user.dir") + "/names/temp/" + latestRecordedName + ".wav";
		    	mediaPlayer = new MediaPlayer(new Media(new File(path).toURI().toString()));
	    		mediaPlayer.play();
	    		
	    		mediaPlayer.setOnEndOfMedia(new Runnable() {
	    			@Override
	    			public void run() {
	    				compareRecordings();
	    			}
	    		});
			}
		});
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setPlayer(Player player, double volume) {
        this.player = player;
        this.volume = volume;
    }
}
