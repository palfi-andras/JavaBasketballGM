package application;

import core.EntityType;
import core.League;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * CS 622
 * RootGUI.java
 * The RootGUI shows a basic screen where the user can either start a new league or load a previous one
 *
 * @author apalfi
 * @version 1.0
 */
class RootGUI extends AbstractGUI {

    private List<League> previousLeagues = new LinkedList<>();
    private Stage primaryStage;


    RootGUI(Stage primaryStage) {
        super();
        this.primaryStage = primaryStage;
        bootstrap();
        if (previousLeagues.size() == 0) {
            setupNewLeague();
        } else {
            Label label = new Label("Welcome to JavaBasketballGM! ");
            label.setFont(Font.font("Arial", 24));
            label.setPadding(new Insets(10, 0, 10, 0));
            label.setAlignment(Pos.TOP_CENTER);
            Button newL = new Button("New League");
            newL.setOnAction(e -> {
                setupNewLeague();
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
            HBox previousLeagueButtons = new HBox(10);
            previousLeagueButtons.setAlignment(Pos.CENTER);
            for (League league : previousLeagues) {

            }
            HBox functionalButtons = new HBox(50, newL, loadL);
            functionalButtons.setAlignment(Pos.BASELINE_CENTER);
            functionalButtons.setPadding(new Insets(10, 10, 10, 10));
            getRootPane().setTop(label);
            getRootPane().setBottom(functionalButtons);
            Scene scene = new Scene(getRootPane(), 1050, 850);
            primaryStage.setTitle("Java Basketball GM");
            BorderPane.setAlignment(getRootPane().getTop(), Pos.TOP_CENTER);
            BorderPane.setAlignment(getRootPane().getBottom(), Pos.BASELINE_CENTER);
            primaryStage.setScene(scene);
            primaryStage.show();
        }


    }

    private void bootstrap() {
        List<Object> objects = DatabaseConnection.getConnection().getEntitiesOfType(EntityType.League);
        for (Object o : objects)
            previousLeagues.add((League) o);
    }

    private void setupNewLeague() {
        TextInputDialog td = new TextInputDialog("league1");
        td.setHeaderText("Enter name for League");
        Optional<String> result = td.showAndWait();
        if (result.isPresent()) {
            League newLeague = new League(result.get());
            NewLeagueGUI newLeagueGUI = new NewLeagueGUI(primaryStage, new);
            primaryStage.setScene(new Scene(newLeagueGUI.getRootPane(), 1050, 850));
        }

    }


}
