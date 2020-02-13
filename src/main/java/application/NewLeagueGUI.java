package application;


import core.League;
import core.LeagueFunctions;
import core.Team;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

class NewLeagueGUI extends AbstractGUI {


    NewLeagueGUI(Stage primaryStage) {
        super();
        League.getInstance().automatedDraft();
        Label label1 = new Label("Select Your Team");
        label1.setFont(Font.font("Arial", 36));
        label1.setPadding(new Insets(10, 220, 10, 220));
        getRootPane().setTop(label1);
        FlowPane pane = new FlowPane();
        pane.setPadding(new Insets(25, 25, 25, 25));
        pane.setVgap(4);
        pane.setHgap(4);
        pane.setPrefWrapLength(170);

        for (Team t : LeagueFunctions.getAllTeams()) {
            Button button = new Button(t.getName());
            button.setOnAction(e -> {
                MainMenuGUI mainMenuGUI = new MainMenuGUI(primaryStage, t);
                primaryStage.getScene().setRoot(mainMenuGUI.getRootPane());
            });
            pane.getChildren().add(button);
        }
        getRootPane().setCenter(pane);
    }

}
