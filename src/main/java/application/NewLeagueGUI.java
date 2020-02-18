package application;


import core.League;
import core.LeagueFunctions;
import core.Team;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Optional;

class NewLeagueGUI extends AbstractGUI {

    NewLeagueGUI(Stage primaryStage) {
        super();
        Label label1 = new Label("Select Your Team");
        label1.setFont(Font.font("Arial", 36));
        label1.setPadding(new Insets(10, 0, 10, 0));
        getRootPane().setTop(label1);
        FlowPane pane = new FlowPane();
        pane.setPadding(new Insets(25, 25, 25, 25));
        pane.setVgap(4);
        pane.setHgap(4);
        pane.setPrefWrapLength(170);

        for (Team t : LeagueFunctions.getAllTeams()) {
            Button button = new Button(t.getName());
            button.setOnAction(e -> {
                ButtonType manualDraft = new ButtonType("Manual Draft", ButtonBar.ButtonData.YES);
                ButtonType automatedDraft = new ButtonType("Automated Draft", ButtonBar.ButtonData.NO);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        String.format("You have selected %s as your team. Would you like to start a live draft of" +
                                " players now or perform an automated draft?", t.getName()), manualDraft, automatedDraft);
                alert.setTitle("Draft?");
                alert.setHeaderText("Would you like to draft the team?");
                Optional<ButtonType> results = alert.showAndWait();
                if (results.get() == automatedDraft) {
                    League.getInstance().automatedDraft();
                    MainMenuGUI mainMenuGUI = new MainMenuGUI(primaryStage, t);
                    primaryStage.getScene().setRoot(mainMenuGUI.getRootPane());
                } else {
                    DraftGUI draftGUI = new DraftGUI(primaryStage, t);
                    primaryStage.getScene().setRoot(draftGUI.getRootPane());
                }
            });
            pane.getChildren().add(button);
        }
        getRootPane().setCenter(pane);
        BorderPane.setAlignment(getRootPane().getTop(), Pos.TOP_CENTER);
        BorderPane.setAlignment(getRootPane().getCenter(), Pos.TOP_CENTER);

    }

}
