package application;

import core.Draft;
import core.Entity;
import core.League;
import core.LeagueFunctions;
import core.Player;
import core.Team;
import core.TeamAttributes;
import core.Utils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

class DraftGUI extends AbstractGUI {

    private Team userTeam;
    private Draft draft = new Draft();
    private Team nowPicking = draft.getCurrentTeam();
    private Stage primaryStage;

    DraftGUI(Stage primaryStage, Team userTeam) {
        super();
        this.userTeam = userTeam;
        this.primaryStage = primaryStage;
        getRootPane().setTop(Utils.getTitleLabel("League Draft"));
        BorderPane.setAlignment(getRootPane().getTop(), Pos.CENTER);
        refresh();
    }

    private void refresh() {
        setRight();
        setLeft();
        setBottom();
        setCenter();
    }

    private void setLeft() {
        // set the roster at the left
        VBox box = new VBox(8, Utils.getBoldLabel(String.format("%s Roster", userTeam.getName())));
        box.getChildren().add(Utils.createRosterTableForTeam(userTeam));
        box.getChildren().add(new Separator(Orientation.HORIZONTAL));
        box.getChildren().add(Utils.getBoldLabel(String.format("Now Picking: %s", nowPicking.getName())));
        Button simulatePick = new Button("Simulate This Pick");
        simulatePick.setOnAction(e -> {
            if (userTeam == nowPicking) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Are you sure you want to simulate your " +
                        "teams own pick? The best overall player will be drafted", ButtonType.NO, ButtonType.YES);
                alert.setHeaderText("Simulating your pick...");
                Optional<ButtonType> results = alert.showAndWait();
                if (results.get() == ButtonType.YES)
                    performDraftAction(LeagueFunctions.getBestAvailableFreeAgent(), nowPicking, true);
            } else {
                performDraftAction(LeagueFunctions.getBestAvailableFreeAgent(), nowPicking, true);
            }
        });
        box.getChildren().add(simulatePick);
        if (userTeam != nowPicking) {
            Button simulateToNextUserPick = new Button("Simulate to next User Pick");
            simulateToNextUserPick.setOnAction(e -> {
                while (nowPicking != userTeam)
                    performDraftAction(LeagueFunctions.getBestAvailableFreeAgent(), nowPicking, true);
                refresh();
            });
            box.getChildren().add(simulateToNextUserPick);
        }
        box.getChildren().add(new Separator(Orientation.HORIZONTAL));
        box.getChildren().add(Utils.getBoldLabel("Draft Order"));
        TableView<Entity> order = draft.createDraftOrderTable();
        TableColumn<Entity, String> nameCol = (TableColumn<Entity, String>) order.getColumns().get(0);
        nameCol.setCellFactory(col -> {
            TableCell<Entity, String> cell = new TableCell<Entity, String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    boolean highlighted = LeagueFunctions.getAllTeams().indexOf(LeagueFunctions.getTeam(item))
                            % LeagueFunctions.getAllTeams().size() == draft.pickNum;
                    if (highlighted)
                        setStyle("-fx-background-color: #b8e9ff;");
                    setText(item);
                }
            };
            return cell;
        });
        order.getColumns().remove(0);
        order.getColumns().add(nameCol);
        order.setPrefWidth(240);
        box.getChildren().add(order);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(0, 10, 0, 10));
        getRootPane().setLeft(box);
        BorderPane.setAlignment(getRootPane().getLeft(), Pos.TOP_CENTER);

    }

    private void performDraftAction(Player p, Team t, boolean refresh) {
        if (LeagueFunctions.getRosterSize(userTeam) == League.PLAYERS_PER_TEAM) {
            while (!draft.draftIsDone()) {
                draft.draftPlayer(LeagueFunctions.getBestAvailableFreeAgent(), nowPicking);
                if (!draft.draftIsDone())
                    nowPicking = draft.getCurrentTeam();
            }
            setupNextScene();
        }
        draft.draftPlayer(p, t);
        if (!draft.draftIsDone()) {
            nowPicking = draft.getCurrentTeam();
            if (refresh)
                refresh();
        } else {
            setupNextScene();
        }
    }

    private void setupNextScene() {
        DraftRecap draftRecap = new DraftRecap(primaryStage, userTeam, draft);
        primaryStage.getScene().setRoot(draftRecap.getRootPane());
    }


    private void setBottom() {
        getRootPane().setBottom(Utils.getTitleLabel(String.format("Team Size: %d/%d",
                LeagueFunctions.getRosterSize(userTeam), League.PLAYERS_PER_TEAM)));
        BorderPane.setAlignment(getRootPane().getBottom(), Pos.CENTER);
    }

    private void setRight() {
        VBox box = new VBox(8,
                Utils.getTitleLabel("Team Needs"));
        box.setPadding(new Insets(0, 10, 0, 10));
        Label l = Utils.getStandardLabel("This section displays your teams average attributes. Use this as a guide for " +
                "deciding which players to draft. If you are lower in one attribute, you may want to draft" +
                " a player with a high value in that attribute area");
        l.setPrefSize(275, 120);
        l.setWrapText(true);
        box.getChildren().add(l);
        for (TeamAttributes a : TeamAttributes.values())
            box.getChildren().add(new HBox(8,
                    Utils.getBoldLabel(a.toString()),
                    Utils.getStandardLabel(String.valueOf(
                            Utils.round(LeagueFunctions.getTeam(userTeam).getEntityAttribute(a.toString()), 2)
                    ))));
        box.setPrefWidth(275);
        box.getChildren().add(new Separator(Orientation.HORIZONTAL));
        box.getChildren().add(Utils.getBoldLabel("Your Team vs The Average"));
        Label l2 = Utils.getStandardLabel("This section shows your current team overall rating versus the overall rating" +
                " average for all the other teams in the league.");
        l2.setPrefSize(275, 80);
        l2.setWrapText(true);
        box.getChildren().add(l2);
        box.getChildren().add(new HBox(10, Utils.getStandardLabel("Your Team Ovr: "),
                Utils.getStandardLabel(String.valueOf(LeagueFunctions.getTeam(userTeam).getOverallTeamRating()))));
        box.getChildren().add(new HBox(10, Utils.getStandardLabel("League Avg. Team Ovr: "),
                Utils.getStandardLabel(String.valueOf(LeagueFunctions.getLeagueAvgTeamOvrRating()))));

        getRootPane().setRight(box);
        BorderPane.setAlignment(getRootPane().getRight(), Pos.CENTER_RIGHT);
    }


    private void setCenter() {
        VBox box = new VBox(5, Utils.getBoldLabel("Draft Board"), Utils.getStandardLabel("Double click a " +
                "player to draft them to your team"),
                Utils.getStandardLabel("Tip: Double-clicking a column header allows you to sort based off any attribute"));
        box.setPadding(new Insets(0, 10, 0, 10));
        TableView<Entity> draftBoard = Utils.createDraftTable();
        draftBoard.setPrefHeight(550);
        draftBoard.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                Player selectedPlayer = (Player) draftBoard.getSelectionModel().getSelectedItem();
                if (userTeam != nowPicking) {
                    Alert error = new Alert(Alert.AlertType.ERROR, "Your team is not currently picking. " +
                            "Would you like to simulate this turn?", ButtonType.NO, ButtonType.YES);
                    error.setHeaderText(String.format("Not currently %s's turn!", userTeam.getName()));
                    Optional<ButtonType> results = error.showAndWait();
                    if (results.get() == ButtonType.YES) {
                        ((Button) ((VBox) getRootPane().getLeft()).getChildren().get(3)).fire();
                    }
                } else {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, String.format(
                            "Are you sure you want to draft %s to your team?", selectedPlayer.getName()
                    ), ButtonType.NO, ButtonType.YES);
                    confirm.setHeaderText("Confirm your selection");
                    Optional<ButtonType> results = confirm.showAndWait();
                    if (results.get() == ButtonType.YES) {
                        performDraftAction(selectedPlayer, userTeam, true);
                    }
                }
            }
        });
        box.getChildren().add(draftBoard);
        box.getChildren().addAll(Utils.getBoldLabel("Draft Recap"));
        ScrollPane pane = new ScrollPane();
        pane.setContent(draft.createDraftRecapTable());
        box.getChildren().addAll(pane);
        getRootPane().setCenter(box);
        BorderPane.setAlignment(getRootPane().getCenter(), Pos.TOP_CENTER);
    }
}
