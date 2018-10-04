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
import sample.model.NamesCollection;
import sample.model.Player;
import sample.model.Score;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainGuiController implements Initializable {

    private int MIN_WINDOW_WIDTH = 1150;
    private int MIN_WINDOW_HEIGHT = 800;
    
    private Player player = new Player();
    private Score score = Score.getInstance();
    private NamesCollection namesCollection = new NamesCollection();
    private PopupAlert alert = new PopupAlert();

    @FXML private AnchorPane rootPane;
    @FXML private AnchorPane prepareSession;
    @FXML private AnchorPane sessionPlaybackControl;
    @FXML private GridPane mediaControl;
    @FXML private Label currentScore;
    @FXML private DynamicAutocompleteTextBox searchTextBox;
    @FXML private TextArea mainTextArea;
    @FXML private Button startStopSessionButton;
    @FXML private TextField nowPlayingText;
    @FXML private Slider volumeSlider;
    @FXML private ToggleButton shuffleToggle;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater( ()-> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setMinWidth(this.MIN_WINDOW_WIDTH);
            stage.setMinHeight(this.MIN_WINDOW_HEIGHT);
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

        this.volumeSlider.setValue(75);

    }

    /**
     * Add search bar contents to text area
     */
    public void addButtonPressed() {
        if(this.searchTextBox.getText()!=null && !this.searchTextBox.getText().trim().isEmpty()) {
            this.searchTextBox.resetAutocompleteTextBox();
            this.mainTextArea.appendText(this.searchTextBox.getText() + "\n");
            this.searchTextBox.clear();
        }

        //TODO add code to add single name to the playlist
    }

    public void clearPlaylistButton() {
        this.mainTextArea.clear();
    }

    /**
     * Add contents of one or more text files to the text area
     */
    public void importTextFileButtonPressed() {
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
				} catch (FileNotFoundException e) {
					this.alert.unkownError();
				} catch (IOException e) {
					this.alert.unkownError();
				}
            }
        }
        if(!this.mainTextArea.getText().isEmpty()) {
            startStopSessionButtonPressed();
        }
    }

    public void startStopSessionButtonPressed() {
        player.setText(mainTextArea.getText());
        try {
            player.formPlaylist();
        } catch (IOException e) {
            alert.unkownError();
        }
        setNowPlaying();
    }
    
    public void setNowPlaying() {
    	nowPlayingText.setText(player.getNowPlaying());
    }

    public void badQualityButtonPressed() {
        try {
            List<String> tempList1 = new ArrayList<>();
            tempList1.add("Peter");
            tempList1.add("Paul");
            tempList1.add("Logan");
            List<String> tempList2 = new ArrayList<>();
            tempList2.add("se206_2-5-2018_15-23-50_Peter.wav");
            tempList2.add("se206_2-5-2018_15-31-49_Paul.wav");
            tempList2.add("se206_2-5-2018_15-47-27_Logan.wav");



            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("QualityRater.fxml"));
            Parent root = fxmlLoader.load();
            QualityRaterController controller = fxmlLoader.getController();

            //TODO Replace (tempList1) and (tempList2) with (List of names in current name) and (List of files for names in current name) respectively
            controller.setNames(tempList1, tempList2);

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
        this.currentScore.setText("Current Score: " + score.differentNameRequested());
        player.prevName();
        nowPlayingText.setText(player.getNowPlaying());
    }

    public void playButtonPressed(){
        player.playCurrentName(this.volumeSlider.getValue()/100);
    }

    public void playPastRecordingButtonPressed() {

    }

    public void nextNameButtonPressed(){
        this.currentScore.setText("Current Score: " + score.differentNameRequested());
        player.nextName();
        nowPlayingText.setText(player.getNowPlaying());
    }

    public void practiceNameButtonPressed(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PracticeName.fxml"));
            Parent root = fxmlLoader.load();
            PracticeNameController controller = fxmlLoader.getController();
            controller.setScoreLabel(this.currentScore);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 680, 500));
            stage.setTitle("Practicing Name");
            stage.setResizable(false);
            stage.showAndWait();
        }catch(IOException e){
            this.alert.unkownError();
        }
    }


}
