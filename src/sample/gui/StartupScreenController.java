package sample.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import sample.model.DirectoryMaintainer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class StartupScreenController implements Initializable {


    @FXML private AnchorPane rootPane;

    private DirectoryMaintainer directoryMaintainer = new DirectoryMaintainer();
    private PopupAlert alert = new PopupAlert();

    /**
     * On loading, will create required files/directories for later function and clears unnecessary files leftover
     * from previous session.
     * @param location unused
     * @param resources unused
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            //Creates required directory for user to insert into database
            this.directoryMaintainer.create();
            this.directoryMaintainer.clearTempDirectory();
        } catch (IOException | InterruptedException e) {
            this.alert.unknownError();
        }
    }

    /**
     * Opens directory chooser for user to select directory containing wav files. The wav files in this folder will be
     * included into the database. This is to provide a more user-friendly way of expanding the database
     */
    public void uploadFilesButtonClicked() {
        //Directory chooser displayed to user to select directory
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Add Files to Database");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File dir = directoryChooser.showDialog(this.rootPane.getScene().getWindow());
        if(dir != null) {
            try {
                //Adds wav files of selected directory into database
                if(this.directoryMaintainer.copyWavFiles(dir)) {
                    this.alert.badName();
                }
            } catch (IOException e) {
                this.alert.unknownError();
            }
        }
    }

    /**
     * Loads the main gui in a new stage
     */
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
