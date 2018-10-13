package sample.gui;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.controlsfx.control.CheckListView;
import sample.model.DirectoryMaintainer;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class QualityRaterController implements Initializable {

    private List<String> paths;

    @FXML private CheckListView<String> listView;
    @FXML private Button submitButton;

    /**
     * Adds listener to listview to enable/disable submit button depending on if items are selected
     * @param location unused
     * @param resources unused
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Adds listener to listview
        this.listView.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) c -> {
            if(this.listView.getCheckModel().getCheckedItems().isEmpty()){
                //listview selection empty so submit button disabled
                this.submitButton.setDisable(true);
            }else {
                //listview selection not empty so submit button enabled
                this.submitButton.setDisable(false);
            }
        });
    }

    /**
     * Gets the list of names and the audio path, in order, before stage is shown. Sets the items of the listview to
     * the names
     * @param names names to be rated and displayed in the listview
     * @param paths paths associated with the names to be written into the text file
     */
    public void setNames(List<String> names, List<String> paths) {
        this.listView.getItems().setAll(FXCollections.observableArrayList(names));
        this.paths = paths;
    }

    /**
     * On submit button pressed, will take checked elements from listview and mark them as bad quality in text file.
     * Then closes the stage
     */
    public void submitButtonPressed() {
        //Marks selected files as bad quality
        DirectoryMaintainer directoryMaintainer = new DirectoryMaintainer();
        for(int i : this.listView.getCheckModel().getCheckedIndices()) {
            directoryMaintainer.writeBadQuality(paths.get(i));
        }
        //Closes stage
        cancelButtonPressed();
    }

    /**
     * On cancel button pressed, closes the stage
     */
    public void cancelButtonPressed() {
        ((Stage) listView.getScene().getWindow()).close();
    }
}
