package application;

import attributes.TeamAttributes;
import core.League;
import core.Player;
import core.Team;
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
import utilities.Draft;
import utilities.Utils;

import java.util.Optional;

/**
 * CS 622
 * DraftGUI.java
 * THe DraftGUI class implements the Draft GUI of the JavaBasketballGM. This class is responsible for facilitating the
 * draft process for the user
 *
 * @author apalfi
 * @version 1.0
 */
class DraftGUI extends AbstractGUI {

    // The users team
    private Team userTeam;
    // Initialize a draft process
    private Draft draft = new Draft();
    // The team currently picking at this point in the draft
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

    /**
     * Refresh the entire view
     */
    private void refresh() {
        setRight();
        setLeft();
        setBottom();
        setCenter();
    }

    /**
     * Sets the left portion of the DraftGUI which shows a table of the users current roster, some buttons to simulate
     * the process, and also a view of the draft order
     */
    private void setLeft() {
        // set the roster at the left
        VBox box = new VBox(8, Utils.getBoldLabel(String.format("%s Roster", userTeam.getName())));
        // Add the roster table
        box.getChildren().add(Utils.createRosterTableForTeam(userTeam));
        box.getChildren().add(new Separator(Orientation.HORIZONTAL));
        box.getChildren().add(Utils.getBoldLabel(String.format("Now Picking: %s", nowPicking.getName())));
        // Add a button to simulate pick
        Button simulatePick = new Button("Simulate This Pick");
        simulatePick.setOnAction(e -> {
            if (userTeam == nowPicking) {
                // If this is the users team, make sure they really want to sim
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
            // Adds a button that alows the user to simulate to their next pick
            Button simulateToNextUserPick = new Button("Simulate to next User Pick");
            simulateToNextUserPick.setOnAction(e -> {
                while (nowPicking != userTeam)
                    performDraftAction(LeagueFunctions.getBestAvailableFreeAgent(), nowPicking, true);
                refresh();
            });
            box.getChildren().add(simulateToNextUserPick);
        }
        box.getChildren().add(new Separator(Orientation.HORIZONTAL));
        /*
        Now add a Table that shows the draft order. We also set a cell value factory to highlight the current teams pick
         */
        box.getChildren().add(Utils.getBoldLabel("Draft Order"));
        TableView<Entity> order = draft.createDraftOrderTable();
        TableColumn<Entity, String> nameCol = (TableColumn<Entity, String>) order.getColumns().get(0);
        nameCol.setCellFactory(col -> {
            TableCell<Entity, String> cell = new TableCell<Entity, String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    boolean highlighted = League.getInstance().getTeams().indexOf(League.getInstance().getTeam(item))
                            % League.getInstance().getTeams().size() == draft.pickNum;
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

    /**
     * Performs a draft action in the context of this GUI. If the user has already finished drafting their team, it
     * will just sim the rest of the draft. Also increments the currentTeam drafting to the next picl.
     *
     * @param p       Player: THe player being drafted
     * @param t       Team: The team drafting the player
     * @param refresh boolean: If set to true, will reload the DraftGUI.
     */
    private void performDraftAction(Player p, Team t, boolean refresh) {
        if (userTeam.getRosterSize() == League.PLAYERS_PER_TEAM) {
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

    /**
     * Sets up the next Scene, which is the DraftRecap view.
     */
    private void setupNextScene() {
        DraftRecap draftRecap = new DraftRecap(primaryStage, userTeam, draft);
        primaryStage.getScene().setRoot(draftRecap.getRootPane());
    }


    /**
     * Set the bottom portion of the view, which is a label of how many players the team has drafted thus far in the draft
     */
    private void setBottom() {
        getRootPane().setBottom(Utils.getTitleLabel(String.format("Team Size: %d/%d",
                userTeam.getRosterSize(), League.PLAYERS_PER_TEAM)));
        BorderPane.setAlignment(getRootPane().getBottom(), Pos.CENTER);
    }

    /**
     * Sets the right portion of the view, which shows information that the user may find helpful while drafting.
     * Adds a table of  a teams average attributes, along with average team overall ratings versus the league average.
     */
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
                            Utils.round((Double) userTeam.getEntityAttribute(a.toString()), 2)
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
                Utils.getStandardLabel(String.valueOf(userTeam.getOverallTeamRating()))));
        box.getChildren().add(new HBox(10, Utils.getStandardLabel("League Avg. Team Ovr: "),
                Utils.getStandardLabel(String.valueOf(LeagueFunctions.getLeagueAvgTeamOvrRating()))));

        getRootPane().setRight(box);
        BorderPane.setAlignment(getRootPane().getRight(), Pos.CENTER_RIGHT);
    }


    /**
     * The center view is the main content area of this screen. It shows a table of free agents that are avaiable to draft.
     * It also shows a table of players that have already been drafted
     */
    private void setCenter() {
        VBox box = new VBox(5, Utils.getBoldLabel("Draft Board"), Utils.getStandardLabel("Double click a " +
                "player to draft them to your team"),
                Utils.getStandardLabel("Tip: Double-clicking a column header allows you to sort based off any attribute"));
        box.setPadding(new Insets(0, 10, 0, 10));
        // Create the table of free agents
        TableView<Entity> draftBoard = Utils.createDraftTable();
        draftBoard.setPrefHeight(550);
        // Set a mouse event when a user double clicks on a free agent, to have the option to draft them. Will ask for
        // confirmation before actually executing the draft. Will also show an error if the user is not currently drafting
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
        // Add a table of already drafted players in this draft
        box.getChildren().addAll(Utils.getBoldLabel("Draft Recap"));
        ScrollPane pane = new ScrollPane();
        pane.setContent(draft.createDraftRecapTable());
        box.getChildren().addAll(pane);
        getRootPane().setCenter(box);
        BorderPane.setAlignment(getRootPane().getCenter(), Pos.TOP_CENTER);
    }
}
