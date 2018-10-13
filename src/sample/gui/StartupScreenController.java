package sample.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.model.DirectoryMaintainer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class StartupScreenController implements Initializable {


    @FXML private AnchorPane rootPane;

    private DirectoryMaintainer directoryMaintainer = new DirectoryMaintainer();
    private PopupAlert alert = new PopupAlert();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //Creates required directory for user to insert into database
        try {
            this.directoryMaintainer.create();
            this.directoryMaintainer.clearTempDirectory();
        } catch (IOException | InterruptedException e) {
            this.alert.unknownError();
        }
    }

    public void uploadFilesButtonClicked() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Add Files to Database");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File dir = directoryChooser.showDialog(this.rootPane.getScene().getWindow());
        if(dir != null) {
            try {
                if(this.directoryMaintainer.copyWavFiles(dir)) {
                    this.alert.badName();
                }
            } catch (IOException e) {
                this.alert.unknownError();
            }
        }
    }

    public void beginButtonClicked() {
        try {
            Stage currentStage = (Stage) rootPane.getScene().getWindow();

            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("MainGui.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), currentStage.getWidth(), currentStage.getHeight());
            Stage stage = new Stage();
            stage.setTitle("Name Sayer");
            stage.setScene(scene);
            stage.show();
            currentStage.close();
        }catch (IOException e){
            this.alert.unknownError();
        }

    }
}
