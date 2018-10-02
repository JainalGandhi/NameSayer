package sample;

import javafx.scene.control.Alert;
import javafx.scene.layout.Region;

/**
 * Multipurpose popup window with configurable content
 */
public class PopupAlert {

    //Creates alert to inform user the microphone is not working
    public void micNotWorking() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Microphone Not Working");
        alert.setHeaderText(null);
        alert.setContentText("Your microphone audio is not working. Please connect another microphone.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    //Creates alert to inform user the microphone is working
    public void micWorking() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Microphone Working");
        alert.setHeaderText(null);
        alert.setContentText("Your microphone audio works.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    //Creates alert to inform user that no names have been selected when play button pressed
    public void noNameSelected() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Name Selection Error");
        alert.setHeaderText(null);
        alert.setContentText("No names were selected to practice. Please select a name(s) before pressing start.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    //Creates alert to inform user that the recording was saved
    public void redordingSaved() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Recording Saved");
        alert.setHeaderText(null);
        alert.setContentText("Your recording was successfully saved.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    //Creates alert to inform user that an unknown error has occurred
    public void unkownError() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Unknown error");
        alert.setHeaderText(null);
        alert.setContentText("Something went wrong. You may continue to use the GUI or restart it.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
    
  //Creates alert to inform user that no varient has been selected when play button pressed
    public void noVarientSelected() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Variant Selection Error");
        alert.setHeaderText(null);
        alert.setContentText("No variant was selected to practice. Please select a variant before pressing play.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    public void badName() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bad File Name");
        alert.setHeaderText(null);
        alert.setContentText("One or more names you tried to upload had a bad name. It must follow the structure: \"se206_xx-xx-xxxx_xx-xx-xx_name.wav\"");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    public void badNameInDatabase() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bad File in Database");
        alert.setHeaderText(null);
        alert.setContentText("There is an incorrectly named file in the database. Please delete it and then relaunch the application");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

}
