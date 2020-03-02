package application;

import core.League;
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
import utilities.DatabaseConnection;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

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
            new Alert(Alert.AlertType.INFORMATION, "This program features an auto-save system " +
                    "that will write changes to a local database whenever they are made. All data is cached locally to" +
                    " cut back on database activity, and the only times the database is accessed is when an update is made. Please choose a " +
                    "file where the local database will be saved at.").showAndWait();
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("DB files (*.db)", "*.db");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file == null) {
                throw new RuntimeException("Error Saving File!");
            } else {
                Stage runtimeStage = new Stage();
                runtimeStage.setTitle("JavBasketballGM");
                NewLeagueGUI newLeagueGUI;
                if (file.exists())
                    newLeagueGUI = new NewLeagueGUI(runtimeStage, true, file.getAbsolutePath());
                else
                    newLeagueGUI = new NewLeagueGUI(runtimeStage, false, file.getAbsolutePath());
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
            if (file != null) {
                DatabaseConnection.getInstance(file.getAbsolutePath(), false);
                ResultSet leagueDbEntry = DatabaseConnection.getInstance().getLeagueEntry();
                try {
                    League.getInstance(leagueDbEntry.getInt("lid"), leagueDbEntry.getString("name"));
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                new Alert(Alert.AlertType.CONFIRMATION, "Successfully Loaded League!").showAndWait();
                MainMenuGUI mainMenuGUI = new MainMenuGUI(primaryStage, League.getInstance().getUserTeam());
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
