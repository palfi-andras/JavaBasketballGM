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

/**
 * CS 622
 * RootGUI.java
 * The RootGUI shows a basic screen where the user can either start a new league or load a previous one
 *
 * @author apalfi
 * @version 1.0
 */
class RootGUI extends AbstractGUI {


    RootGUI(Stage primaryStage) {
        super();
        Label label = new Label("Welcome to JavaBasketballGM! ");
        label.setFont(Font.font("Arial", 24));
        label.setPadding(new Insets(10, 0, 10, 0));
        label.setAlignment(Pos.TOP_CENTER);
        Button newL = new Button("New League");
        newL.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("DB files (*.db)", "*.db");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file == null) {
                throw new RuntimeException("Error Saving File!");
            } else {
                League.getInstance(file.getName().substring(0, file.getName().length() - 3), file, true);
                Stage runtimeStage = new Stage();
                runtimeStage.setTitle("JavBasketballGM");
                NewLeagueGUI newLeagueGUI;
                if (file.exists())
                    newLeagueGUI = new NewLeagueGUI(runtimeStage, true);
                else
                    newLeagueGUI = new NewLeagueGUI(runtimeStage, false);
                runtimeStage.setScene(new Scene(newLeagueGUI.getRootPane(), 1050, 850));
                primaryStage.close();
                runtimeStage.show();
            }
        });
        Button loadL = new Button("Load League");
        loadL.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("DB files (*.db)", "*.db");
            chooser.getExtensionFilters().add(extFilter);
            File file = chooser.showOpenDialog(primaryStage);
            if (file != null && Utils.loadLeagueFromDatabase(file)) {
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
