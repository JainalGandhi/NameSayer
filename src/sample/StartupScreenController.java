package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class StartupScreenController implements Initializable {

    @FXML private AnchorPane rootPane;

    private DirectoryCreationFactory directoryCreationFactory = new DirectoryCreationFactory();
    private PopupAlert popupAlert = new PopupAlert();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Creates required directory for user to insert into database
        try {
            this.directoryCreationFactory.create();
        } catch (IOException | InterruptedException e) {
            this.popupAlert.unkownError();
        }
    }

    public void uploadFilesButtonClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add Wav Files");
        FileChooser.ExtensionFilter fileExtensions = new FileChooser.ExtensionFilter("Wav Files", "*.wav");
        fileChooser.getExtensionFilters().setAll(fileExtensions);
        List<File> allFiles = fileChooser.showOpenMultipleDialog(this.rootPane.getScene().getWindow());
        try {
            this.directoryCreationFactory.copyFileList(allFiles);
        }catch(IOException e){
            this.popupAlert.unkownError();
        }
    }

    public void beginButtonClicked() {

    }
}
