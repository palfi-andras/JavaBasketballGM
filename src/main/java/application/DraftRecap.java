package application;

import core.Draft;
import core.Team;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utilities.Utils;

class DraftRecap extends AbstractGUI {

    DraftRecap(Stage primaryStage, Team userTeam, Draft draft) {
        super();
        VBox box = new VBox(10, Utils.getTitleLabel("Draft Recap"), draft.createDraftRecapTable());
        Button b = new Button("Proceed to main menu");
        b.setOnAction(e2 -> {
            MainMenuGUI mainMenuGUI = new MainMenuGUI(primaryStage, userTeam);
            primaryStage.getScene().setRoot(mainMenuGUI.getRootPane());
        });
        box.setAlignment(Pos.TOP_CENTER);
        box.getChildren().add(b);
        getRootPane().setCenter(box);

    }
}
