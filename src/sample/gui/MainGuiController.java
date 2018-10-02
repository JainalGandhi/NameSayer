package sample.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import sample.model.NamesCollection;
import sample.gui.customcomponent.DynamicAutocompleteTextBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainGuiController implements Initializable {

    @FXML private AnchorPane rootPane;
//    @FXML private DynamicAutocompleteTextBox pee = new DynamicAutocompleteTextBox();

    private NamesCollection namesCollection = new NamesCollection();
    private PopupAlert alert = new PopupAlert();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater( ()-> ((Stage) rootPane.getScene().getWindow()).setResizable(true));
        try {
            this.namesCollection.solveAllNames();
        }catch (IOException e){
            this.alert.unkownError();
        }

//        pee.setAutocompleteList(this.namesCollection.getAllNamesFirstCap());

    }
}
