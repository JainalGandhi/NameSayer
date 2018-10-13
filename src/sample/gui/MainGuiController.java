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
    @FXML private Button clearPlaylistButton;
    @FXML private Button playPastRecordingButton;


    /**
     * Sets the initial requirements of the componenetes of the scene on startup of the stage
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Sets the minimum size of the size to avoid overlapping on resizing
        Platform.runLater( ()-> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setMinWidth(MIN_WINDOW_WIDTH);
            stage.setMinHeight(MIN_WINDOW_HEIGHT);
            this.rootPane.requestFocus();
        });

        //Determines all names/audio files in the database
        try {
            this.namesCollection.solveAllNames();
        }catch (IOException e) {
            this.alert.unkownError();
        }

        //Disables the media control(play, next...) buttons when the playlist is empty
        this.mainTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if(this.mainTextArea.getText().isEmpty()) {
                this.mediaControl.setDisable(true);
                this.clearPlaylistButton.setDisable(true);
            }else {
                this.mediaControl.setDisable(false);
                this.clearPlaylistButton.setDisable(false);
            }
        });

        //Assigns the autocomplete names list to the custom search text box to be the names in the database
        this.searchTextBox.setAutocompleteList(this.namesCollection.getAllNamesFirstCap());

        //Assigns the default volume slider audio level to 80/100
        this.volumeSlider.setValue(80);

    }

    /**
     * When Add Button pressed, takes the searchbar contents and adds to the main text area and adds to the playlist
     */
    public void addButtonPressed() {
        player.stopAudioPlayback();

        //Checks if the searchbox is null or empty
        if(this.searchTextBox.getText()!=null && !this.searchTextBox.getText().trim().isEmpty()) {
            //Adds searchbar content to the main text area and the playlist
            this.mainTextArea.appendText(this.searchTextBox.getText() + "\n");
            if(this.mainTextArea.getText().split("\n").length == 1) {
                startSession();
            }else {
                try {
                    player.addToPlaylist(this.searchTextBox.getText());
                } catch (IOException e) {
                    alert.unkownError();
                }
            }

            //Resets the searchbox for future user input
            this.searchTextBox.clear();
            this.searchTextBox.resetAutocompleteTextBox();
        }
    }

    /**
     * When Clear Playlist Button pressed, Clears the gui and exports the current playlist to an external text file
     */
    public void clearPlaylistButtonPressed() {
        clearTempData();
        player.stopAudioPlayback();
        String str = this.mainTextArea.getText();

        //Exports the main text area playlist to a text file for future user use
        Runnable task = new Thread(()->{
            try {
                BufferedWriter bf = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/pastplaylists/" + new Date() +  ".txt"));
                bf.write(str);
                bf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        new Thread(task).start();

        //Clears gui
        this.currentScore.setText("Current Score: " + score.differentNameRequested());
        this.nowPlayingText.clear();
        this.mainTextArea.clear();
    }

    /**
     * When Import File button pressed, imports a text file(s) and adds to playlist and main text area. User is able
     * to pick if they want to select only first name or full name to import, as not all users want to learn full name
     */
    public void importTextFileButtonPressed() {
        clearPlaylistButtonPressed();
        //Opens file chooser to allow user to choose any valid text file(s)
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add names list text document");
        FileChooser.ExtensionFilter fileExtensions = new FileChooser.ExtensionFilter("Text Files", "*.txt");
        fileChooser.getExtensionFilters().setAll(fileExtensions);
        List<File> allFiles = fileChooser.showOpenMultipleDialog(this.rootPane.getScene().getWindow());
        if(allFiles != null) {
            //Asks user if they want to only input first name
            boolean importFirstName = this.alert.importFirstNameStyle();
            //Loops through all selected files by the user
            for(File file : allFiles) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String str;
                    while ((str = br.readLine()) != null) {
                        if(!str.isEmpty()) {
                            if (importFirstName) {
                                //Adds first name in file
                                String[] strSplit = str.split(" |_");
                                this.mainTextArea.appendText(strSplit[0] + "\n");
                            } else {
                                //Adds entire name in file
                                this.mainTextArea.appendText(str + "\n");
                            }
                        }
                    }
                    br.close();
                } catch (IOException e) {
                    this.alert.unkownError();
                }
            }
            startSession();
        }
    }

    /**
     * If the text area is empty and files are inputted, prepares and constructs playlist and associated fields
     */
    private void startSession() {
        player.setText(mainTextArea.getText());
        try {
            //Creates playlist
            player.formPlaylist();
        } catch (IOException e) {
            alert.unkownError();
        }
        //Sets the now playing label to show current name from playlist
        setNowPlaying();
    }

    /**
     * Sets the now playing label to show current name from playlist
     */
    private void setNowPlaying() {
        nowPlayingText.setText(player.getNowPlaying());
    }

    /**
     * When Bad Quality button pressed, allows user to select the bad quality wav files from the current name. Inserts
     * these into a text file to inform database owner. Is used for future selection of names from playlist - bad quality
     * names are avoided
     */
    public void badQualityButtonPressed() {
        player.stopAudioPlayback();
        try {
            //Gets sub-names and paths for the current name
            List<String> names = Arrays.asList(this.player.getSegmentedNames(this.player.getCurrentPlaylistName()));
            List<String> paths = new ArrayList<>();
            for(String i : names) {
                paths.add(this.player.createFilePath(i));
            }

            //Loads QualityRater stage to let user select bad quality files
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("QualityRater.fxml"));
            Parent root = fxmlLoader.load();
            QualityRaterController controller = fxmlLoader.getController();
            //Passes as parameters split names and path to recordings used for writing into text file
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

    /**
     * On shuffle button toggle, randomises the order of the playlist to simulate shuffeling the playlist. On toggle
     * off, will go back to the original order.
     */
    public void shuffleButtonPressed(){
        if (shuffleToggle.isSelected()) {
            //Shuffle the order of the playlist
            player.shufflePlayList();
            nowPlayingText.setText(player.getNowPlaying());
        }
        else {
            //Goes back to original oder of playlist
            player.orderPlayList();
            nowPlayingText.setText(player.getNowPlaying());
        }
    }

    /**
     * Gets previous name in playlist and changes media control to reflect new name
     */
    public void previousNameButtonPressed(){
        player.stopAudioPlayback();
        this.currentScore.setText("Current Score: " + score.differentNameRequested());
        player.prevName();
        nowPlayingText.setText(player.getNowPlaying());
    }

    /**
     * Plays the database version of the current name with the selected volume level
     */
    public void playButtonPressed(){
        player.stopAudioPlayback();
        player.playCurrentName(this.volumeSlider.getValue()/100);
    }

    /**
     * Plays the latest user recording of the current name with the selected volume level
     */
    public void playPastRecordingButtonPressed() {
        player.stopAudioPlayback();
        try {
            player.playPastRecording(this.volumeSlider.getValue()/100);
        } catch (IOException e) {
            this.alert.unkownError();
        }
    }

    /**
     * Gets next name in playlist and changes media control to reflect new name
     */
    public void nextNameButtonPressed(){
        player.stopAudioPlayback();
        this.currentScore.setText("Current Score: " + score.differentNameRequested());
        player.nextName();
        nowPlayingText.setText(player.getNowPlaying());
    }

    /**
     * On practice name button pressed, will load the practice module in a new stage in modial mode. Displays to user.
     * Passes the player to allow playback of the current name.
     */
    public void practiceNameButtonPressed(){
        player.stopAudioPlayback();
        try {
            //Loads the practice module in a new stage
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PracticeName.fxml"));
            Parent root = fxmlLoader.load();
            PracticeNameController controller = fxmlLoader.getController();
            controller.setScoreLabel(this.currentScore);
            controller.setColor(colorProgression[this.level]);
            //Passes player and volume as parameters to control playback of the current name
            controller.setPlayer(this.player, this.volumeSlider.getValue()/100);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 680, 500));
            stage.setTitle("Practicing Name");
            stage.setResizable(false);
            stage.showAndWait();
            //On close of practice module, checks if user has unlocked new skin color
            if(this.level!=maxLevel && this.score.getCurrentScore()>=nextGoal){
                this.nextGoal+=this.nextGoal;
                if(this.alert.equipNewLevelRequest()) {
                    //Upgrades skin structure if meets requirements and user says yes to alert
                    this.level++;
                    this.topPane.setStyle("-fx-background-color: " + colorProgression[this.level]);
                    this.sessionPlaybackControl.setStyle("-fx-background-color: " + colorProgression[this.level]);
                }
            }
        }catch(IOException e){
            this.alert.unkownError();
        }
    }

    /**
     * Clears the data in the temp folder to minimise disk space and search operation speed. Does this by deleting
     * files which are able to be deleted without affecting program run.
     */
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
