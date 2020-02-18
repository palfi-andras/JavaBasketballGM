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
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


class MainMenuGUI extends AbstractGUI {

    private final Team userTeam;
    private final Stage primaryStage;
    private Map<GameSimulation, VBox> gameBoxScores = new LinkedHashMap<>();

    MainMenuGUI(Stage primaryStage, Team userTeam) {
        super();
        this.userTeam = userTeam;
        this.primaryStage = primaryStage;
        setupTopBox();
        setupLeftBox();
        setupLowBox();
        setupRightBox();

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
        teamSchedule.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                getRootPane().setCenter(createGameVBox(teamSchedule.getSelectionModel().getSelectedItem()));
            }
        });
        scheduleBox.getChildren().add(teamSchedule);
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


    private VBox createGameVBox(GameSimulation gs) {
        VBox game = new VBox(3);
        game.getChildren().add(new HBox(180,
                Utils.getTitleLabel(String.format("Home: %s", gs.getHomeTeam().getName())),
                Utils.getTitleLabel(String.format("Away: %s", gs.getAwayTeam().getName()))));
        game.getChildren().add(Utils.getStandardLabel("\n\n"));
        game.getChildren().add(new HBox(180,
                Utils.getTitleLabel(String.valueOf(Utils.round(gs.getHomeTeam().getOverallTeamRating(), 3))),
                Utils.getBoldLabel("Overall Rating"),
                Utils.getTitleLabel(String.valueOf(Utils.round(gs.getAwayTeam().getOverallTeamRating(), 3)))));
        game.getChildren().add(Utils.getStandardLabel("\n\n"));
        game.getChildren().add(new HBox(180,
                Utils.getTitleLabel(String.valueOf(gs.getHomeTeamStat(TeamStat.TEAM_PTS))), Utils.getBoldLabel("Score"),
                Utils.getTitleLabel(String.valueOf(gs.getAwayTeamStat(TeamStat.TEAM_PTS)))));
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

    private void playButtonAction(GameSimulation gs, Button b) {
        b.setOnAction(e -> {
            if (gs.gameIsOver()) {
                Alert over = new Alert(Alert.AlertType.ERROR);
                over.setTitle("Error!");
                over.setHeaderText("ERROR: This game is already over");
                over.showAndWait();
                return;
            }
            if (gs != LeagueFunctions.getNextGameForTeam(getUserTeam())) {
                Alert notNextGame = new Alert(Alert.AlertType.ERROR);
                notNextGame.setTitle("Error!");
                notNextGame.setHeaderText("ERROR: Not the current game");
                notNextGame.setContentText("There is another game in the queue that is scheduled earlier");
                notNextGame.showAndWait();
            } else {
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
     * Opens the game simulation screen
     */
    private void simulateNextGameButton(Button b) {
        b.setOnAction(e -> {
            for (GameSimulation gs : LeagueFunctions.getAllGames()) {
                if (gs.gameIsOver())
                    continue;
                if (LeagueFunctions.getGamesForTeam(getUserTeam()).contains(gs)) {
                    // Sim game
                    LeagueFunctions.simulateGame(gs);
                    // Update team record
                    updateRecord();
                    getRootPane().setCenter(createGameVBox(gs));
                    setupRightBox();
                    break;
                } else {
                    LeagueFunctions.simulateGame(gs);
                }
            }
        });
    }

    /**
     * Setup the top portion of the main menu which contains information about the current view
     */
    private void setupTopBox() {
        Label label = Utils.getTitleLabel("Main Menu");
        Insets format = new Insets(10, 0, 10, 0);
        label.setPadding(format);
        label.setAlignment(Pos.TOP_CENTER);
        HBox box = new HBox(20, label);
        Button save = new Button("Save League");
        save.setPadding(format);
        save.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("DATA files (*.data)", "*.data");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                if (Utils.serializeLeague(League.getInstance(), file.getAbsolutePath(), getUserTeam())) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Success!");
                    alert.setHeaderText("League has been saved.");
                    alert.setContentText(String.format("File Path: %s", file.getAbsolutePath()));
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Failure!");
                    alert.setHeaderText("Error Saving League");
                    alert.showAndWait();
                }
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
        VBox vbox = new VBox(2);
        vbox.getChildren().add(Utils.getBoldLabel(String.format("Your Team: %s", getUserTeam().getName())));
        int[] winLoss = LeagueFunctions.getTeamRecord(getUserTeam());
        vbox.getChildren().add(Utils.getBoldLabel(String.format("Record: %d - %d", winLoss[0], winLoss[1])));
        vbox.getChildren().add(Utils.getBoldLabel("\nRoster:"));
        for (Player p : LeagueFunctions.getTeam(userTeam.getID()).getRankedRoster()) {
            Button button = new Button(p.getName());
            setupPlayerButton(p, button);
            vbox.getChildren().add(button);
        }
        vbox.getChildren().add(Utils.getBoldLabel("\n\n"));
        Button teamInfo = new Button("Team Info");
        teamInfoButton(teamInfo, getUserTeam());
        vbox.getChildren().add(teamInfo);
        getRootPane().setLeft(vbox);
    }

    private void teamInfoButton(Button b, Team t) {
        b.setOnAction(e -> {
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
            box.getChildren().add(new HBox(10, Utils.getBoldLabel("Overall Team Rating"),
                    Utils.getBoldLabel(String.valueOf(getUserTeam().getOverallTeamRating()))));
            int[] winLoss = LeagueFunctions.getTeamRecord(getUserTeam());
            box.getChildren().add(new HBox(10, Utils.getBoldLabel("Team Record"),
                    Utils.getBoldLabel(String.format(" %d - %d", winLoss[0], winLoss[1]))));
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

    private void updateRecord() {
        int[] winLoss = LeagueFunctions.getTeamRecord(getUserTeam());
        ((VBox) getRootPane().getLeft())
                .getChildren().set(1, Utils.getBoldLabel(String.format("Record: %d - %d", winLoss[0], winLoss[1])));
    }


    public Team getUserTeam() {
        return userTeam;
    }


}
