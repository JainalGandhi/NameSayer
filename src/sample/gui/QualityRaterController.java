package sample.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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

    private ObservableList<String> names;
    private List<String> paths;
    private DirectoryMaintainer directoryMaintainer = new DirectoryMaintainer();

    @FXML private CheckListView<String> listView;
    @FXML private Button submitButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater( ()-> this.listView.getItems().setAll(names));
        this.listView.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) c -> {
            if(this.listView.getCheckModel().getCheckedItems().isEmpty()){
                this.submitButton.setDisable(true);
            }else {
                this.submitButton.setDisable(false);
            }
        });
    }

    public void setNames(List<String> names, List<String> paths) {
        this.names = FXCollections.observableArrayList(names);
        this.paths = paths;
    }

    public void submitButtonPressed() {
        for(int i : this.listView.getCheckModel().getCheckedIndices()) {
            directoryMaintainer.writeBadQuality(paths.get(i));
        }
        cancelButtonPressed();
    }

    public void cancelButtonPressed() {
        ((Stage) listView.getScene().getWindow()).close();
    }
}
