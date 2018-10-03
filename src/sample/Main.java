package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private int STARTUP_GUI_HEIGHT = 700;
    private int STARTUP_GUI_WIDTH = 950;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("gui/StartupScreen.fxml"));
        primaryStage.setTitle("Name Sayer Introduction");
        primaryStage.setScene(new Scene(root, STARTUP_GUI_WIDTH, STARTUP_GUI_HEIGHT));
        primaryStage.setResizable(false);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
