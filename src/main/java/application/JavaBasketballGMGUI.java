package application;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Barebones start to a GUI for the Basketball Simulator.
 */
public class JavaBasketballGMGUI extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        new RootGUI(primaryStage);
    }
}
