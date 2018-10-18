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
    @FXML private Button practiceButton;


    /**
     * Sets the initial requirements of the components of the scene on startup of the stage
     * @param location unused
     * @param resources unused
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

        //Determines all names/audio files in the database and loads into search text box
        Runnable task = new Thread(()-> {
            try {
                this.namesCollection.solveAllNames();
                Platform.runLater(()->this.searchTextBox.setAutocompleteList(this.namesCollection.getAllNamesFirstCap()));
            } catch (IOException e) {
                Platform.runLater(()->this.alert.unknownError());
            }
        });
        new Thread(task).start();

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

        //Assigns the default volume slider audio level to 80/100
        this.volumeSlider.setValue(80);

    }
    
    public void changePracticeButton(Boolean change) {
    	this.practiceButton.setDisable(change);
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
                this.searchTextBox.clear();
                this.searchTextBox.resetAutocompleteTextBox();
            }else {
                Runnable task = new Thread(()->{
                    try {
                        player.addToPlaylist(this.searchTextBox.getText());
                    } catch (IOException e) {
                        Platform.runLater(()->alert.unknownError());
                    }
                    Platform.runLater(()-> {
                        this.searchTextBox.clear();
                        this.searchTextBox.resetAutocompleteTextBox();
                    });
                });
                new Thread(task).start();
            }
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
                Platform.runLater(()->alert.unknownError());
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
        if(allFiles != null && textFilesNotEmpty(allFiles)) {
            //Asks user if they want to only input first name
            boolean importFirstName = this.alert.importFirstNameStyle();
            Runnable task = new Thread(()-> {
                //Loops through all selected files by the user
                for (File file : allFiles) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String str;
                        while ((str = br.readLine()) != null) {
                            if (!str.isEmpty()) {
                                if (importFirstName) {
                                    //Adds first name in file
                                    String[] strSplit = str.split("[ _]");
                                    Platform.runLater(()->this.mainTextArea.appendText(strSplit[0] + "\n"));
                                } else {
                                    //Adds entire name in file
                                    String string = str;
                                    Platform.runLater(()->this.mainTextArea.appendText(string + "\n"));
                                }
                            }
                        }
                        br.close();
                    } catch (IOException e) {
                        Platform.runLater(()->this.alert.unknownError());
                    }
                }
                Platform.runLater(this::startSession);
            });
            new Thread(task).start();
        }
    }

    /**
     * Checks if any of the files from the file list is not empty
     * @param allFiles the list of files to search for non-empty
     * @return true if not empty, false if all empty
     */
    private boolean textFilesNotEmpty(List<File> allFiles) {
        for(File file : allFiles) {
            if(file.length() != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * If the text area is empty and files are inputted, prepares and constructs playlist and associated fields
     */
    private void startSession() {
        try {
            //Creates playlist
            player.formPlaylist(this.mainTextArea.getText());
        } catch (IOException e) {
            alert.unknownError();
        }
        //Sets the now playing label to show current name from playlist
        setNowPlaying();
    }

    /**
     * Sets the now playing label to show current name from playlist
     */
    private void setNowPlaying() {
        nowPlayingText.setText(player.getNowPlaying());
        changePracticeButton(player.currentNameNotValid());
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
            ((Stage) rootPane.getScene().getWindow()).setResizable(true);
        }catch(IOException e){
            this.alert.unknownError();
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
            changePracticeButton(player.currentNameNotValid());
        }
        else {
            //Goes back to original oder of playlist
            player.orderPlayList();
            nowPlayingText.setText(player.getNowPlaying());
            changePracticeButton(player.currentNameNotValid());
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
        changePracticeButton(player.currentNameNotValid());
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
            this.alert.unknownError();
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
        changePracticeButton(player.currentNameNotValid());
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
            //Passes player and volume as parameters to control playback of the current name
            controller.setPlayer(this.player, this.volumeSlider.getValue()/100);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 800, 500));
            stage.setTitle("Practicing Name");
            stage.setResizable(false);
            stage.showAndWait();
            //On close of practice module, checks if new skin color has been unlocked
            this.topPane.setStyle("-fx-background-color: " + this.score.getColor());
            this.sessionPlaybackControl.setStyle("-fx-background-color: " + this.score.getColor());
            ((Stage) rootPane.getScene().getWindow()).setResizable(true);
        }catch(IOException e){
            this.alert.unknownError();
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
                this.alert.unknownError();
            }
        });
        new Thread(task).start();
    }
}
