package sample.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.gui.customcomponent.DynamicAutocompleteTextBox;
import sample.model.DirectoryMaintainer;
import sample.model.NamesCollection;
import sample.model.Player;
import sample.model.Score;

import java.io.*;
import java.net.URL;
import java.util.*;

public class MainGuiController implements Initializable {

    private final static int MIN_WINDOW_WIDTH = 1150;
    private final static int MIN_WINDOW_HEIGHT = 800;
    private final static String[] colorProgression = new String[]{"rgba(216,233,238,0.91)", "rgba(238,169,171,0.75)", "rgba(255,0,22,0.68)", "rgba(197,238,188,0.91)", "rgba(142,30,255,0.4)"};
    private final static int maxLevel = colorProgression.length-1;

    private int level = 0;
    private int nextGoal = 10;
    private DirectoryMaintainer directoryMaintainer = new DirectoryMaintainer();
    private Player player = new Player();
    private Score score = Score.getInstance();
    private NamesCollection namesCollection = new NamesCollection();
    private PopupAlert alert = new PopupAlert();

    @FXML private AnchorPane rootPane;
    @FXML private AnchorPane topPane;
    @FXML private AnchorPane sessionPlaybackControl;
    @FXML private GridPane mediaControl;
    @FXML private Label currentScore;
    @FXML private DynamicAutocompleteTextBox searchTextBox;
    @FXML private TextArea mainTextArea;
    @FXML private TextField nowPlayingText;
    @FXML private Slider volumeSlider;
    @FXML private ToggleButton shuffleToggle;
    @FXML private Button playPastRecordingButton;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater( ()-> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setMinWidth(MIN_WINDOW_WIDTH);
            stage.setMinHeight(MIN_WINDOW_HEIGHT);
            this.rootPane.requestFocus();
        });

        try {
            this.namesCollection.solveAllNames();
        }catch (IOException e) {
            this.alert.unkownError();
        }

        this.mainTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if(this.mainTextArea.getText().isEmpty()) {
                this.mediaControl.setDisable(true);
            }else {
                this.mediaControl.setDisable(false);
            }
        });

        this.searchTextBox.setAutocompleteList(this.namesCollection.getAllNamesFirstCap());

        this.volumeSlider.setValue(80);

    }

    /**
     * Add search bar contents to text area
     */
    public void addButtonPressed() {
        player.stopAudioPlayback();
        if(this.searchTextBox.getText()!=null && !this.searchTextBox.getText().trim().isEmpty()) {
            this.searchTextBox.resetAutocompleteTextBox();
            this.mainTextArea.appendText(this.searchTextBox.getText() + "\n");

            if(this.mainTextArea.getText().split("\n").length == 1) {
                startStopSessionButtonPressed();
            }else {
                try {
                    player.addToPlaylist(this.searchTextBox.getText());
                } catch (IOException e) {
                    alert.unkownError();
                }
            }
            this.searchTextBox.clear();
        }
    }

    public void clearPlaylistButton() {
        clearTempData();
        this.currentScore.setText("Current Score: " + score.differentNameRequested());
        player.stopAudioPlayback();
        this.nowPlayingText.clear();

        try {
            BufferedWriter bf = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/pastplaylists/" + new Date() +  ".txt"));
            bf.write(this.mainTextArea.getText());
            bf.flush();
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.mainTextArea.clear();
    }

    /**
     * Add contents of one or more text files to the text area
     */
    public void importTextFileButtonPressed() {
        clearPlaylistButton();
    	FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add names list text document");
        FileChooser.ExtensionFilter fileExtensions = new FileChooser.ExtensionFilter("Text Files", "*.txt");
        fileChooser.getExtensionFilters().setAll(fileExtensions);
        List<File> allFiles = fileChooser.showOpenMultipleDialog(this.rootPane.getScene().getWindow());
        if(allFiles != null) {
        	for(File file : allFiles) {
                try {
					BufferedReader br = new BufferedReader(new FileReader(file));
					String str;
					while ((str = br.readLine()) != null) {
						this.mainTextArea.appendText(str + "\n");
					}
					br.close();
				} catch (IOException e) {
					this.alert.unkownError();
				}
            }
        }
        if(!this.mainTextArea.getText().isEmpty()) {
            startStopSessionButtonPressed();
        }
    }

    private void startStopSessionButtonPressed() {
        player.setText(mainTextArea.getText());
        try {
            player.formPlaylist();
        } catch (IOException e) {
            alert.unkownError();
        }
        setNowPlaying();
    }

    private void setNowPlaying() {
    	nowPlayingText.setText(player.getNowPlaying());
    }

    public void badQualityButtonPressed() {
        player.stopAudioPlayback();
        try {
            List<String> names = Arrays.asList(this.player.getSegmentedNames(this.player.getCurrentPlaylistName()));
            List<String> paths = new ArrayList<>();
            for(String i : names) {
                paths.add(this.player.createFilePath(i));
            }

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("QualityRater.fxml"));
            Parent root = fxmlLoader.load();
            QualityRaterController controller = fxmlLoader.getController();
            controller.setNames(names, paths);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 380, 420));
            stage.setTitle("Mark Bad Quality");
            stage.setResizable(false);
            stage.showAndWait();
        }catch(IOException e){
            this.alert.unkownError();
        }
    }

    public void shuffleButtonPressed(){
    	if (shuffleToggle.isSelected()) {
    		player.shufflePlayList();
    		nowPlayingText.setText(player.getNowPlaying());
    	}
    	else {
    		player.orderPlayList();
    		nowPlayingText.setText(player.getNowPlaying());
    	}
    }

    public void previousNameButtonPressed(){
        player.stopAudioPlayback();
        this.currentScore.setText("Current Score: " + score.differentNameRequested());
        player.prevName();
        nowPlayingText.setText(player.getNowPlaying());
    }

    public void playButtonPressed(){
        player.stopAudioPlayback();
        player.playCurrentName(this.volumeSlider.getValue()/100);
    }

    public void playPastRecordingButtonPressed() {
        player.stopAudioPlayback();
        try {
            player.playPastRecording();
        } catch (IOException e) {
            this.alert.unkownError();
        }
    }

    public void nextNameButtonPressed(){
        player.stopAudioPlayback();
        this.currentScore.setText("Current Score: " + score.differentNameRequested());
        player.nextName();
        nowPlayingText.setText(player.getNowPlaying());
    }

    public void practiceNameButtonPressed(){
        player.stopAudioPlayback();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PracticeName.fxml"));
            Parent root = fxmlLoader.load();
            PracticeNameController controller = fxmlLoader.getController();
            controller.setScoreLabel(this.currentScore);
            controller.setColor(colorProgression[this.level]);
            controller.setPlayer(this.player, this.volumeSlider.getValue()/100);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 680, 500));
            stage.setTitle("Practicing Name");
            stage.setResizable(false);
            stage.showAndWait();
            if(this.level!=maxLevel && this.score.getCurrentScore()>=nextGoal){
                this.nextGoal+=this.nextGoal;
                if(this.alert.equipNewLevelRequest()) {
                    this.level++;
                    this.topPane.setStyle("-fx-background-color: " + colorProgression[this.level]);
                    this.sessionPlaybackControl.setStyle("-fx-background-color: " + colorProgression[this.level]);
                }
            }
        }catch(IOException e){
            this.alert.unkownError();
        }
    }

    private void clearTempData() {
        Runnable task = new Thread( ()-> {
            try {
                this.directoryMaintainer.clearTempDirectory();
            } catch (IOException e) {
                this.alert.unkownError();
            }
        });
        new Thread(task).start();
    }
}
