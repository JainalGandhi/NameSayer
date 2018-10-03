package sample.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.gui.customcomponent.DynamicAutocompleteTextBox;
import sample.model.NamesCollection;
import sample.model.Player;

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

    @FXML private AnchorPane rootPane;
    @FXML private AnchorPane prepareSession;
    @FXML private AnchorPane sessionPlaybackControl;
    @FXML private DynamicAutocompleteTextBox searchTextBox;
    @FXML private TextArea mainTextArea;
    @FXML private Button startStopSessionButton;
    @FXML private TextField nowPlayingText;
    @FXML private Slider volumeSlider;



    private NamesCollection namesCollection = new NamesCollection();
    private PopupAlert alert = new PopupAlert();

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

        this.searchTextBox.setAutocompleteList(this.namesCollection.getAllNamesFirstCap());

        this.volumeSlider.setValue(75);

    }

    /**
     * Add search bar contents to text area
     */
    public void addButtonPressed() {
        //Checking if on new line
        if(!(this.mainTextArea==null || this.mainTextArea.getText().trim().isEmpty() || (this.mainTextArea.getText().length()-1 >=0
                && this.mainTextArea.getText().charAt(this.mainTextArea.getText().length()-1)=='\n'))) {
            this.mainTextArea.appendText("\n");
        }
        if(this.searchTextBox.getText()!=null && !this.searchTextBox.getText().trim().isEmpty()) {
            this.searchTextBox.resetAutocompleteTextBox();
            this.mainTextArea.appendText(this.searchTextBox.getText() + "\n");
            this.searchTextBox.clear();
        }
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
    }

    public void startStopSessionButtonPressed() throws IOException {
    	Player player = new Player(mainTextArea.getText());
    	player.formPlaylist();
    	setNowPlaying(player);
    }
    
    public void setNowPlaying(Player player) {
    	nowPlayingText.setText(player.getNowPlaying());
    }

    public void badQualityButtonPressed() {
        try {
            List<String> tempList = new ArrayList<>();
            tempList.add("Peter");
            tempList.add("Paul");
            tempList.add("Logan");

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("QualityRater.fxml"));
            Parent root = fxmlLoader.load();
            QualityRaterController controller = fxmlLoader.getController();

            //TODO Replace tempList with type variant (so it should include the file location and shortform name
            controller.setNames(tempList);

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

    }

    public void previousNameButtonPressed(){

    }

    public void playButtonPressed(){

    }

    public void playPastRecordingButtonPressed() {

    }

    public void nextNameButtonPressed(){

    }

    public void practiceNameButtonPressed(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PracticeName.fxml"));
            Parent root = fxmlLoader.load();
            PracticeNameController controller = fxmlLoader.getController();
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
