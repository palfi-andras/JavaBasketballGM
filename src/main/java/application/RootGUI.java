package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

class RootGUI extends AbstractGUI {


    RootGUI(Stage primaryStage) {
        super();
        Label label = new Label("Welcome to JavaBasketballGM! ");
        label.setFont(Font.font("Arial", 24));
        label.setPadding(new Insets(10, 220, 10, 220));
        Button newL = new Button("New League");
        newL.setOnAction(e -> {
            NewLeagueGUI newLeagueGUI = new NewLeagueGUI(primaryStage);
            primaryStage.getScene().setRoot(newLeagueGUI.getRootPane());
        });
        Button loadL = new Button("Load League");
        HBox hBox = new HBox(50, newL, loadL);
        hBox.setAlignment(Pos.BASELINE_CENTER);
        hBox.setPadding(new Insets(10, 10, 10, 10));
        getRootPane().setTop(label);
        getRootPane().setBottom(hBox);
        Scene scene = new Scene(getRootPane(), 950, 850);
        primaryStage.setTitle("Java Basketball GM");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


}
