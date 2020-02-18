package application;

import core.League;
import core.LeagueFunctions;
import core.Utils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

class RootGUI extends AbstractGUI {


    RootGUI(Stage primaryStage) {
        super();
        Label label = new Label("Welcome to JavaBasketballGM! ");
        label.setFont(Font.font("Arial", 24));
        label.setPadding(new Insets(10, 0, 10, 0));
        label.setAlignment(Pos.TOP_CENTER);
        Button newL = new Button("New League");
        newL.setOnAction(e -> {
            NewLeagueGUI newLeagueGUI = new NewLeagueGUI(primaryStage);
            primaryStage.getScene().setRoot(newLeagueGUI.getRootPane());
        });
        Button loadL = new Button("Load League");
        loadL.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("DATA files (*.data)", "*.data");
            chooser.getExtensionFilters().add(extFilter);
            File file = chooser.showOpenDialog(primaryStage);
            if (file != null && Utils.deserializeLeague(file.getAbsolutePath())) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Successfully Loaded League!");
                alert.showAndWait();
                MainMenuGUI mainMenuGUI = new MainMenuGUI(primaryStage,
                        LeagueFunctions.getTeam(League.getInstance().getUserTeam().getID()));
                primaryStage.getScene().setRoot(mainMenuGUI.getRootPane());
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error Loading League!");
                alert.showAndWait();
            }
        });
        HBox hBox = new HBox(50, newL, loadL);
        hBox.setAlignment(Pos.BASELINE_CENTER);
        hBox.setPadding(new Insets(10, 10, 10, 10));
        getRootPane().setTop(label);
        getRootPane().setBottom(hBox);
        Scene scene = new Scene(getRootPane(), 1050, 850);
        primaryStage.setTitle("Java Basketball GM");
        BorderPane.setAlignment(getRootPane().getTop(), Pos.TOP_CENTER);
        BorderPane.setAlignment(getRootPane().getBottom(), Pos.BASELINE_CENTER);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


}
