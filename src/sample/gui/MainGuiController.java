package sample.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.gui.customcomponent.DynamicAutocompleteTextBox;
import sample.model.NamesCollection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

public class MainGuiController implements Initializable {

    private int MIN_WINDOW_WIDTH = 950;
    private int MIN_WINDOW_HEIGHT = 700;

    @FXML private AnchorPane rootPane;
    @FXML private AnchorPane prepareSession;
    @FXML private AnchorPane sessionPlaybackControl;
    @FXML private DynamicAutocompleteTextBox searchTextBox;
    @FXML private TextArea mainTextArea;
    @FXML private Button startStopSessionButton;
    @FXML private TextField NowPlayingText;
    @FXML private Slider volumeSlider;



    private NamesCollection namesCollection = new NamesCollection();
    private PopupAlert alert = new PopupAlert();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater( ()-> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setMinWidth(this.MIN_WINDOW_WIDTH);
            stage.setMinHeight(this.MIN_WINDOW_HEIGHT);
        });

        try {
            this.namesCollection.solveAllNames();
        }catch (IOException e){
            this.alert.unkownError();
        }

        this.searchTextBox.setAutocompleteList(this.namesCollection.getAllNamesFirstCap());

    }

    public void addButtonPressed() {

    }

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
						mainTextArea.appendText(str + "\n");
					}
				} catch (FileNotFoundException e) {
					this.alert.unkownError();
				} catch (IOException e) {
					this.alert.unkownError();
				}
            }
        }
    }

    public void startStopSessionButtonPressed() {

    }

    public void badQualityButtonPressed() {

    }

    public void shuffleButtonPressed(){

    }

    public void previousNameButtonPressed(){

    }

    public void playButtonPressed(){

    }

    public void nextNameButtonPressed(){

    }

    public void practiceNameButtonPressed(){

    }


}
