package sample.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import sample.gui.customcomponent.DynamicAutocompleteTextBox;
import sample.model.NamesCollection;

import java.io.IOException;
import java.net.URL;
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

    public void importTextFileButtonPressed() {

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
