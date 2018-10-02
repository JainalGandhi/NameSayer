package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import sample.customcomponent.DynamicAutocompleteTextBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainGuiController implements Initializable {

    @FXML private DynamicAutocompleteTextBox pee = new DynamicAutocompleteTextBox();

    private NamesCollection namesCollection = new NamesCollection();
    private PopupAlert alert = new PopupAlert();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.namesCollection.solveAllNames();
        }catch (IOException e){
            this.alert.unkownError();
        }

        pee.setAutocompleteList(this.namesCollection.getAllNamesFirstCap());

    }
}
