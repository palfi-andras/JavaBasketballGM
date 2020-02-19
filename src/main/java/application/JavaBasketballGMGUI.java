package application;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * CS 622
 * JavaBasketballGMGUI.java
 * The JavaBasketballGMGUI class is the main starting point for this program. It starts a new instance of RootGUI which
 * will lead to other GUIs.
 *
 * @author apalfi
 * @version 1.0
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
