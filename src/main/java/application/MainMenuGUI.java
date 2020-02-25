package application;

import core.Entity;
import core.EntityType;
import core.League;
import core.LeagueFunctions;
import core.Player;
import core.Team;
import core.Utils;
import gameplay.GameSimulation;
import gameplay.PlayerStat;
import gameplay.TeamStat;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


/**
 * CS622
 * MainMenuGUI.java
 * This class implements the "MainMenu" or Main View of this program. This is the view presented to the user once their
 * team is fully drafted and ready to start playing games.
 *
 * @author apalfi
 * @version 1.0
 */
class MainMenuGUI extends AbstractGUI {

    // The users team
    private final Team userTeam;
    private final Stage primaryStage;

    MainMenuGUI(Stage primaryStage, Team userTeam) {
        super();
        this.userTeam = userTeam;
        this.primaryStage = primaryStage;
        setupTopBox();
        setupLeftBox();
        setupLowBox();
        setupRightBox();

    }

    private void refresh() {
        setupTopBox();
        setupLeftBox();
        setupRightBox();
        setupLowBox();
    }

    /**
     * Setup the right portion of the main menu which shows the user teams schedule
     */
    private void setupRightBox() {
        ScrollPane schedule = new ScrollPane();
        schedule.setPrefSize(350, 350);
        VBox scheduleBox = new VBox(3);
        scheduleBox.getChildren().add(Utils.getBoldLabel("Team Schedule"));
        scheduleBox.getChildren().addAll(Utils.getStandardLabel("Click Game Row to view more info and simulate game"));
        TableView<GameSimulation> teamSchedule = Utils.createScheduleTable(getUserTeam());
        // Set an event that brings up the Game View if a user clicks on one of the games in this table. From here
        // they will be able to start the game.
        teamSchedule.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                getRootPane().setCenter(createGameVBox(teamSchedule.getSelectionModel().getSelectedItem()));
            }
        });
        // Add team schedule
        scheduleBox.getChildren().add(teamSchedule);
        // Create a button that allows the user to set up more games
        Button scheduleMoreGames = new Button("Schedule More Games");
        scheduleMoreGames.setOnAction(e -> {
            League.getInstance().setupRoundRobinTournament();
            setupRightBox();
        });
        scheduleBox.getChildren().add(scheduleMoreGames);
        //  Button simulateGame = new Button("Simulate Next Game");
        //  simulateNextGameButton(simulateGame);
        //  scheduleBox.getChildren().add(simulateGame);
        schedule.setContent(scheduleBox);
        getRootPane().setRight(schedule);
    }


    /**
     * This method creates a view that should be used in the center pane of this screen. It is the view presented
     * to a user when simulating a game.
     *
     * @param gs GameSimulation: The game that the user wants to see info about, or simulate
     * @return VBox: The GUI elements compromising this game
     */
    private VBox createGameVBox(GameSimulation gs) {
        VBox game = new VBox(3);
        // First add labels for team names
        game.getChildren().add(new HBox(180,
                Utils.getTitleLabel(String.format("Home: %s", gs.getHomeTeam().getName())),
                Utils.getTitleLabel(String.format("Away: %s", gs.getAwayTeam().getName()))));
        game.getChildren().add(Utils.getStandardLabel("\n\n"));
        // Add lables for team ovr ratings
        game.getChildren().add(new HBox(180,
                Utils.getTitleLabel(String.valueOf(Utils.round(gs.getHomeTeam().getOverallTeamRating(), 3))),
                Utils.getBoldLabel("Overall Rating"),
                Utils.getTitleLabel(String.valueOf(Utils.round(gs.getAwayTeam().getOverallTeamRating(), 3)))));
        game.getChildren().add(Utils.getStandardLabel("\n\n"));
        // Add labels for the score
        game.getChildren().add(new HBox(180,
                Utils.getTitleLabel(String.valueOf(gs.getHomeTeamStat(TeamStat.TEAM_PTS))), Utils.getBoldLabel("Score"),
                Utils.getTitleLabel(String.valueOf(gs.getAwayTeamStat(TeamStat.TEAM_PTS)))));
        // Add a button to play the game represented by this view
        Button playGame = new Button("Play Game");
        playButtonAction(gs, playGame);
        game.getChildren().add(playGame);

        // Display game log
        ScrollPane gameLog = new ScrollPane();
        gameLog.setPrefSize(100, 280);
        VBox logs = new VBox(2);
        for (String log : gs.getGameLog()) {
            logs.getChildren().add(Utils.getStandardLabel(log));
        }
        gameLog.setContent(logs);
        game.getChildren().add(Utils.getTitleLabel("Game Log"));
        game.getChildren().add(gameLog);
        // Display stats
        ScrollPane stats = new ScrollPane();
        stats.setPrefSize(100, 315);
        VBox statBox = new VBox(4);
        statBox.getChildren().add(Utils.getTitleLabel("Team Stats"));
        TableView<Entity> teamStats = Utils.createGameSimulationTeamStatTable(gs);
        teamStats.setPrefHeight(180);
        statBox.getChildren().add(teamStats);

        TableView<Entity> homePlayers = Utils.createGameSimulationPlayerStatTable(gs, gs.getHomeTeam().getRoster());
        homePlayers.setPrefHeight(300);
        TableView<Entity> awayPlayers = Utils.createGameSimulationPlayerStatTable(gs, gs.getAwayTeam().getRoster());
        awayPlayers.setPrefHeight(300);
        statBox.getChildren().add(Utils.getTitleLabel("Home Player Stats"));
        statBox.getChildren().add(homePlayers);
        statBox.getChildren().add(Utils.getTitleLabel("Away Player Stats"));
        statBox.getChildren().add(awayPlayers);
        stats.setContent(statBox);
        game.getChildren().add(stats);
        return game;
    }

    /**
     * Configures the action for the Play Game button. Will do some checking to see if the user is currently scheduled
     * to play this game or not.
     *
     * @param gs GameSimulation: the game to be played
     * @param b  Button: the button to set the action for
     */
    private void playButtonAction(GameSimulation gs, Button b) {
        b.setOnAction(e -> {
            // If the game is already over, then show an error
            if (gs.gameIsOver()) {
                Alert over = new Alert(Alert.AlertType.ERROR);
                over.setTitle("Error!");
                over.setHeaderText("ERROR: This game is already over");
                over.showAndWait();
                return;
            }
            // If the user has a game scheduled before this one, show an error
            if (gs != LeagueFunctions.getNextGameForTeam(getUserTeam())) {
                Alert notNextGame = new Alert(Alert.AlertType.ERROR);
                notNextGame.setTitle("Error!");
                notNextGame.setHeaderText("ERROR: Not the current game");
                notNextGame.setContentText("There is another game in the queue that is scheduled earlier");
                notNextGame.showAndWait();
            } else {
                // Else, simulate this game (along with any other games that do not belong to the user and happen in the
                // same time period.
                for (GameSimulation game : LeagueFunctions.getAllGames()) {
                    if (game.gameIsOver())
                        continue;
                    if (game != gs)
                        LeagueFunctions.simulateGame(game);
                    else {
                        // Sim game
                        LeagueFunctions.simulateGame(gs);
                        // Update team record
                        updateRecord();
                        getRootPane().setCenter(createGameVBox(gs));
                        break;
                    }
                }
            }
        });
    }


    /**
     * Setup the top portion of the main menu which contains information about the current view. This also adds buttons
     * which allows the user to save and quit from this application.
     */
    private void setupTopBox() {
        Label label = Utils.getTitleLabel("Main Menu");
        Insets format = new Insets(10, 0, 10, 0);
        label.setPadding(format);
        label.setAlignment(Pos.TOP_CENTER);
        HBox box = new HBox(20, label);
        /*
        Add a button to save the league, using a FileChooser. We write out the save as a serialized object (.data)
         */
        Button save = new Button("Save League");
        save.setPadding(format);
        save.setOnAction(e -> {
            if (Utils.saveLeagueToDatabase(League.getInstance(), getUserTeam())) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Success!");
                alert.setHeaderText("League has been saved.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Failure!");
                alert.setHeaderText("Error Saving League");
                alert.showAndWait();
            }
        });
        box.getChildren().add(save);
        Button quit = new Button("Quit");
        quit.setPadding(format);
        quit.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Select yes to save the league", ButtonType.YES, ButtonType.NO);
            alert.setTitle("Save?");
            alert.setHeaderText("Would you like to save before quitting?");
            Optional<ButtonType> results = alert.showAndWait();
            if (results.get() == ButtonType.YES) {
                save.fire();
            }
            if (results.get() == ButtonType.NO)
                System.exit(0);
            System.exit(0);
        });
        box.getChildren().add(quit);
        getRootPane().setTop(box);
    }

    /**
     * Setup the left portion of the main menu which contains buttons that bring up information about the users roster
     */
    private void setupLeftBox() {
        VBox vbox = new VBox(10);
        vbox.getChildren().add(Utils.getBoldLabel(String.format("Your Team: %s", getUserTeam().getName())));
        int[] winLoss = LeagueFunctions.getTeamRecord(getUserTeam());
        vbox.getChildren().add(Utils.getBoldLabel(String.format("Record: %d - %d", winLoss[0], winLoss[1])));
        vbox.getChildren().add(Utils.getBoldLabel("\nRoster:"));
        for (Player p : LeagueFunctions.getTeam(userTeam.getID()).getRankedRoster()) {
            Button button = new Button(p.getName());
            setupPlayerButton(p, button);
            vbox.getChildren().add(button);
        }
        vbox.getChildren().add(new Separator());
        Button teamInfo = new Button("Team Info");
        teamInfoButton(teamInfo, getUserTeam());
        vbox.getChildren().add(teamInfo);
        Button signFreeAgent = new Button("Free Agents");
        signFreeAgent.setOnAction(e -> {
            VBox freeAgentPane = new VBox(10);
            freeAgentPane.getChildren().add(Utils.getTitleLabel("Free Agents"));
            freeAgentPane.getChildren().add(Utils.getStandardLabel("Tip: Double-click a players name to sign the free agent"));
            TableView<Entity> freeAgents = Utils.createEntityAvgStatsTable(new LinkedList<>(
                    LeagueFunctions.getFreeAgents()));
            freeAgents.setOnMouseClicked((MouseEvent event) -> {
                if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                    if (LeagueFunctions.getTeam(userTeam).getRoster().size() < League.PLAYERS_PER_TEAM) {
                        Player freeAgent = (Player)
                                freeAgents.getSelectionModel().getSelectedItem();
                        // Allowed
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                                String.format("Are you sure you want to add free agent %s to your team?", freeAgent.getName()),
                                ButtonType.YES, ButtonType.NO);
                        confirm.setHeaderText("Confirm your selection.");
                        Optional<ButtonType> response = confirm.showAndWait();
                        if (response.get() == ButtonType.YES) {
                            LeagueFunctions.getTeam(userTeam).addPlayerToRoster(freeAgent);
                            refresh();
                        }
                    } else {
                        // Unallowed
                        Alert cannot = new Alert(Alert.AlertType.ERROR, "Your team already has the maximum " +
                                "amount of players. You must drop someone first before adding a free agent");
                        cannot.setHeaderText("Error: Team Already Filled");
                        cannot.showAndWait();
                    }
                }
            });
            freeAgentPane.getChildren().add(freeAgents);
            freeAgentPane.setAlignment(Pos.TOP_CENTER);
            getRootPane().setCenter(freeAgentPane);
        });
        vbox.getChildren().addAll(signFreeAgent);
        getRootPane().setLeft(vbox);
    }

    /**
     * This brings up a view which provides information about a team. We show things such as average stats for their roster,
     * and who their best players are
     *
     * @param b Button: the button to append this action to
     * @param t Team: the team to view info about
     */
    private void teamInfoButton(Button b, Team t) {
        b.setOnAction(e -> {
            // First create a view of this teams average stats
            List<Entity> players = new LinkedList<>(t.getRoster());
            VBox box = new VBox(10);
            box.getChildren().add(Utils.getTitleLabel("Average Team Stats"));
            TableView<Entity> avgStatsTable = Utils.createEntityAvgStatsTable(players);
            avgStatsTable.setOnMouseClicked((MouseEvent event) -> {
                if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                    getRootPane().setCenter(createPlayerBox(
                            (Player) avgStatsTable.getSelectionModel().getSelectedItem()));
                }
            });
            box.getChildren().add(avgStatsTable);
            /*
            Display this teams overall rating and current record
             */
            box.getChildren().add(new HBox(10, Utils.getBoldLabel("Overall Team Rating"),
                    Utils.getBoldLabel(String.valueOf(getUserTeam().getOverallTeamRating()))));
            int[] winLoss = LeagueFunctions.getTeamRecord(getUserTeam());
            box.getChildren().add(new HBox(10, Utils.getBoldLabel("Team Record"),
                    Utils.getBoldLabel(String.format(" %d - %d", winLoss[0], winLoss[1]))));
            /*
            Finally, display some of the best players in a few different categories from this users team
             */
            box.getChildren().add(new HBox(10, Utils.getBoldLabel("Best Overall Player"),
                    Utils.getBoldLabel(t.getRankedRoster().get(0).getName())));
            Player topScorer = t.getSortedRosterBasedOffPlayerAvgStats(PlayerStat.PTS).get(0);
            Player topAssist = t.getSortedRosterBasedOffPlayerAvgStats(PlayerStat.ASSIST).get(0);
            Player topOffReb = t.getSortedRosterBasedOffPlayerAvgStats(PlayerStat.ORB).get(0);
            Player topDefReb = t.getSortedRosterBasedOffPlayerAvgStats(PlayerStat.DRB).get(0);
            Player topStl = t.getSortedRosterBasedOffPlayerAvgStats(PlayerStat.STL).get(0);
            Player topBlock = t.getSortedRosterBasedOffPlayerAvgStats(PlayerStat.BLK).get(0);
            box.getChildren().add(new HBox(10, Utils.getBoldLabel("Most Points"),
                    Utils.getBoldLabel(topScorer.getName()), Utils.getBoldLabel("Points per Game:"),
                    Utils.getBoldLabel(String.valueOf(topScorer.getStatContainer().getAvgValueOfStat(PlayerStat.PTS)))));
            box.getChildren().add(new HBox(10, Utils.getBoldLabel("Most Assists"),
                    Utils.getBoldLabel(topAssist.getName()), Utils.getBoldLabel("Assists per Game:"),
                    Utils.getBoldLabel(String.valueOf(topAssist.getStatContainer().getAvgValueOfStat(PlayerStat.ASSIST)))));
            box.getChildren().add(new HBox(10, Utils.getBoldLabel("Most Offensive Rebounds"),
                    Utils.getBoldLabel(topOffReb.getName()), Utils.getBoldLabel("Off. Rebounds per Game:"),
                    Utils.getBoldLabel(String.valueOf(topOffReb.getStatContainer().getAvgValueOfStat(PlayerStat.ORB)))));
            box.getChildren().add(new HBox(10, Utils.getBoldLabel("Most Defensive Rebounds"),
                    Utils.getBoldLabel(topDefReb.getName()), Utils.getBoldLabel("Def. Rebounds per Game:"),
                    Utils.getBoldLabel(String.valueOf(topDefReb.getStatContainer().getAvgValueOfStat(PlayerStat.DRB)))));
            box.getChildren().add(new HBox(10, Utils.getBoldLabel("Most Steals"),
                    Utils.getBoldLabel(topStl.getName()), Utils.getBoldLabel("Steals per Game:"),
                    Utils.getBoldLabel(String.valueOf(topStl.getStatContainer().getAvgValueOfStat(PlayerStat.STL)))));
            box.getChildren().add(new HBox(10, Utils.getBoldLabel("Most Blocks"),
                    Utils.getBoldLabel(topBlock.getName()), Utils.getBoldLabel("Blocks per Game :"),
                    Utils.getBoldLabel(String.valueOf(topBlock.getStatContainer().getAvgValueOfStat(PlayerStat.BLK)))));
            getRootPane().setCenter(box);
        });
    }

    /**
     * Configures an action handler for the player buttons. Displays information about the player in the
     * center view
     */
    private void setupPlayerButton(Player p, Button b) {
        b.setOnAction(e -> {
            getRootPane().setCenter(createPlayerBox(p));
        });
    }

    /**
     * Creates a view that displays some information about a player
     *
     * @param p Player: the player to get info about
     * @return VBox
     */
    private VBox createPlayerBox(Player p) {
        VBox playerBox = new VBox(5);
        playerBox.getChildren().add(Utils.getTitleLabel(p.getName()));
        playerBox.getChildren().add(new HBox(10, Utils.getBoldLabel("Overall Rating: "),
                Utils.getStandardLabel(String.valueOf(p.getOverallPlayerRating()))));
        playerBox.getChildren().add(Utils.getTitleLabel("Player Attributes"));
        TableView<Entity> attrTable = Utils.createEntityAttributeTable(p, EntityType.PLAYER);
        attrTable.setPrefHeight(150);
        playerBox.getChildren().add(attrTable);
        playerBox.getChildren().add(Utils.getTitleLabel("Average Statistics"));
        if (LeagueFunctions.teamHasNotPlayedGames(getUserTeam())) {
            playerBox.getChildren().add(Utils.getBoldLabel("Team has not yet played any games"));
        } else {
            TableView<Entity> statTable = Utils.createEntityAvgStatsTable(p);
            statTable.setPrefHeight(150);
            playerBox.getChildren().add(statTable);
        }
        if (LeagueFunctions.getPlayerTeam(p) == userTeam) {
            Button dropPlayer = new Button("Release Player");
            dropPlayer.setOnAction(e -> {
                Alert areYouSure = new Alert(Alert.AlertType.CONFIRMATION,
                        String.format("%s will be released and become a free agent", p.getName()),
                        ButtonType.YES, ButtonType.NO);
                areYouSure.setTitle("Are you sure?");
                areYouSure.setHeaderText("Are you sure you want to drop this player?");
                Optional<ButtonType> response = areYouSure.showAndWait();
                if (response.get() == ButtonType.YES) {
                    LeagueFunctions.releasePlayerIntoFreeAgency(p);
                    refresh();
                }
            });
            playerBox.getChildren().addAll(dropPlayer);
        }
        return playerBox;
    }

    /**
     * The low portion of the screen is used to display information about other teams.
     */
    private void setupLowBox() {
        HBox hBox = new HBox(5);
        hBox.getChildren().add(Utils.getBoldLabel("Other Teams"));
        for (Team t : LeagueFunctions.getAllTeams()) {
            if (t == getUserTeam())
                continue;
            Button b = new Button(t.getName());
            teamInfoButton(b, t);
            hBox.getChildren().add(b);
        }
        getRootPane().setBottom(hBox);
    }

    /**
     * Updates the record of the user team in the GUI.
     */
    private void updateRecord() {
        int[] winLoss = LeagueFunctions.getTeamRecord(getUserTeam());
        ((VBox) getRootPane().getLeft())
                .getChildren().set(1, Utils.getBoldLabel(String.format("Record: %d - %d", winLoss[0], winLoss[1])));
    }


    Team getUserTeam() {
        return userTeam;
    }


}
