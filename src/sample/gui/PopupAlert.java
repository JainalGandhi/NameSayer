package sample.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

import java.util.Optional;

/**
 * Multipurpose popup window with configurable content
 */
public class PopupAlert {

    /**
     * Creates alert to inform user that no names have been selected when play button pressed
     */
    public void noNameSelected() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Name Selection Error");
        alert.setHeaderText(null);
        alert.setContentText("No names were selected to practice. Please select a name(s) before pressing start.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    /**
     * Creates alert to inform user that the recording was saved
     */
    public void recordingSaved() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Recording Saved");
        alert.setHeaderText(null);
        alert.setContentText("Your recording was successfully saved.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    /**
     * Creates alert to inform user that an unknown error has occurred
     */
    public void unknownError() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Unknown error");
        alert.setHeaderText(null);
        alert.setContentText("Something went wrong. You may continue to use the GUI or restart it.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    /**
     * Alerts user that wav file added to database is of bad quality
     */
    public void badName() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bad File Name");
        alert.setHeaderText(null);
        alert.setContentText("One or more names you tried to upload had a bad name. It must follow the structure: \"se206_xx-xx-xxxx_xx-xx-xx_name.wav\"");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    /**
     * Asks user if they wish to equip the new skin they have unlocked
     * @return user choice to equip skin (true = yes, false = no)
     */
    public boolean equipNewLevelRequest() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("You Have Unlocked a New Skin Color");
        alert.setHeaderText(null);
        alert.setContentText("Do you wish to equip your new skin color?");

        ButtonType buttonTypeNo = new ButtonType("No");
        ButtonType buttonTypeYes = new ButtonType("Yes");
        alert.getButtonTypes().setAll(buttonTypeNo, buttonTypeYes);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonTypeYes;
    }

    /**
     * Alerts user that no past recordings of the current name exists
     */
    public void noPastRecording() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No Past Recording");
        alert.setHeaderText(null);
        alert.setContentText("There is no past user recording for that name. Please record one first.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    /**
     * Asks user if they wish to import only first name or full name from the text file
     * @return  user choice to only import first name (true = first name, false = full name)
     */
    public boolean importFirstNameStyle() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("How do you wish to import the text file");
        alert.setHeaderText(null);
        alert.setContentText("Do you wish to import only first names or full names");

        ButtonType buttonTypeFirst = new ButtonType("First Name");
        ButtonType buttonTypeFull = new ButtonType("Full Name");
        alert.getButtonTypes().setAll(buttonTypeFirst, buttonTypeFull);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonTypeFirst;
    }
}
