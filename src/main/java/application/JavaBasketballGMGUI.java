package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class JavaBasketballGMGUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        StackPane root = new StackPane();
        Button newL = new Button("New League");
        Button loadL = new Button("Load League");
        root.getChildren().add(newL);
        root.getChildren().add(loadL);
        Scene scene = new Scene(root, 500, 400);
        primaryStage.setTitle("Java Basketball GM");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
