package application;

import core.League;
import core.LeagueFunctions;
import core.Player;
import core.PlayerAttributes;
import core.Team;
import core.Utils;
import gameplay.GameSimulation;
import gameplay.PlayerStat;
import gameplay.TeamStat;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;


class MainMenuGUI extends AbstractGUI {

    private final Team userTeam;

    MainMenuGUI(Stage primaryStage, Team userTeam) {
        super();
        this.userTeam = userTeam;
        setupTopBox();
        setupLeftBox();
        setupLowBox();
        setupRightBox();

    }

    /**
     * Setup the right portion of the main menu which shows the user teams schedule
     */
    private void setupRightBox() {
        VBox vbox = new VBox(5);
        Button setupRRButton = new Button("Setup Round Robin Tournament");
        setupRRButton.setOnAction(e -> {
            League.getInstance().setupRoundRobinTournament();
            vbox.getChildren().remove(0);
            vbox.getChildren().add(new Label("Team Schedule: "));
            int i = 1;
            for (GameSimulation gs : LeagueFunctions.getGamesForTeam(getUserTeam())) {
                vbox.getChildren().add(new Label(String.format("Game %d: %s vs %s", i,
                        gs.getHomeTeam().getName(), gs.getAwayTeam().getName())));
                i++;
            }
            Button simulateNextGameButton = new Button("Simulate Next Game");
            simulateNextGameButton(simulateNextGameButton);
            vbox.getChildren().add(simulateNextGameButton);
        });
        vbox.getChildren().add(setupRRButton);
        getRootPane().setRight(vbox);
    }

    /**
     * Highlights the current game being played
     **/
    private void updateTeamSchedule(int gameIndex) {
        int i = 0;
        for (Node n : ((VBox) getRootPane().getRight()).getChildren()) {
            if (n instanceof Label) {
                if (i - 1 == gameIndex) {
                    ((Label) n).setFont(Font.font("Arial", FontWeight.BOLD, 14));
                } else {
                    ((Label) n).setFont(Font.font("Arial", FontWeight.NORMAL, 14));
                }
            }
            i++;
        }
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
                    updateTeamSchedule(LeagueFunctions.getNumOfGamesPlayedForTeam(getUserTeam()));
                    VBox game = new VBox(3);
                    game.getChildren().add(new HBox(180,
                            getTitleLabel(String.format("Home: %s", gs.getHomeTeam().getName())),
                            getTitleLabel(String.format("Away: %s", gs.getAwayTeam().getName()))));
                    game.getChildren().add(getStandardLabel("\n\n"));
                    game.getChildren().add(new HBox(180,
                            getTitleLabel(String.valueOf(Utils.round(gs.getHomeTeam().getOverallTeamRating(), 3))),
                            getBoldLabel("Overall Rating"),
                            getTitleLabel(String.valueOf(Utils.round(gs.getAwayTeam().getOverallTeamRating(), 3)))));
                    game.getChildren().add(getStandardLabel("\n\n"));
                    game.getChildren().add(new HBox(180,
                            getTitleLabel(String.valueOf(gs.getHomeTeamStat(TeamStat.TEAM_PTS))), getBoldLabel("Score"),
                            getTitleLabel(String.valueOf(gs.getAwayTeamStat(TeamStat.TEAM_PTS)))));
                    Button simulateGame = new Button("Simulate Game");
                    simulateGame.setOnAction(e2 -> {
                        // Sim game
                        LeagueFunctions.simulateGame(gs);
                        // Update final score
                        game.getChildren().set(4, new HBox(180,
                                getTitleLabel(String.valueOf(gs.getHomeTeamStat(TeamStat.TEAM_PTS))),
                                getBoldLabel("Score"),
                                getTitleLabel(String.valueOf(gs.getAwayTeamStat(TeamStat.TEAM_PTS)))));
                        // Display game log
                        ScrollPane gameLog = new ScrollPane();
                        gameLog.setVmax(200);
                        gameLog.setPrefSize(100, 250);
                        VBox logs = new VBox(2);
                        for (String log : gs.getGameLog()) {
                            logs.getChildren().add(getStandardLabel(log));
                        }
                        gameLog.setContent(logs);
                        game.getChildren().add(getTitleLabel("Game Log"));
                        game.getChildren().add(gameLog);
                        // Display stats
                        game.getChildren().add(getTitleLabel("Stats"));
                        ScrollPane stats = new ScrollPane();
                        stats.setPrefSize(100, 250);
                        stats.setVmax(200);
                        VBox statBox = new VBox(4);
                        statBox.getChildren().add(getTitleLabel("Team Stats"));
                        HBox teamLabels = new HBox(5, getStandardLabel("            "));
                        HBox homeTeam = new HBox(5, getBoldLabel(gs.getHomeTeam().getName()));
                        HBox awayTeam = new HBox(5, getBoldLabel(gs.getAwayTeam().getName()));
                        for (TeamStat stat : TeamStat.values()) {
                            teamLabels.getChildren().add(getBoldLabel(stat.toString()));
                            homeTeam.getChildren().add(getStandardLabel(String.valueOf(gs.getHomeTeamStat(stat))));
                            awayTeam.getChildren().add(getStandardLabel(String.valueOf(gs.getAwayTeamStat(stat))));
                        }
                        statBox.getChildren().add(teamLabels);
                        statBox.getChildren().add(homeTeam);
                        statBox.getChildren().add(awayTeam);
                        statBox.getChildren().add(getTitleLabel("Player Stats"));
                        HBox homeLabels = new HBox(5, getBoldLabel(gs.getHomeTeam().getName()));
                        HBox awayLabels = new HBox(5, getBoldLabel(gs.getAwayTeam().getName()));
                        for (PlayerStat stat : PlayerStat.values()) {
                            homeLabels.getChildren().add(getBoldLabel(stat.toString()));
                        }
                        statBox.getChildren().add(homeLabels);
                        for (Player p : gs.getHomeTeam().getRankedRoster()) {
                            HBox playerBox = new HBox(5, getBoldLabel(p.getName()));
                            for (int val : gs.getPlayerStats(p).values()) {
                                playerBox.getChildren().add(getStandardLabel(String.valueOf(val)));
                            }
                            statBox.getChildren().add(playerBox);
                        }
                        statBox.getChildren().add(awayLabels);
                        for (Player p : gs.getHomeTeam().getRankedRoster()) {
                            HBox playerBox = new HBox(5, getBoldLabel(p.getName()));
                            for (int val : gs.getPlayerStats(p).values())
                                playerBox.getChildren().add(getStandardLabel(String.valueOf(val)));
                            statBox.getChildren().add(playerBox);
                        }
                        stats.setContent(statBox);
                        game.getChildren().add(stats);
                        // Update team record
                        updateRecord();
                        // Remove sim game button
                        game.getChildren().remove(simulateGame);
                    });
                    game.getChildren().add(simulateGame);
                    getRootPane().setCenter(game);
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
        Label label = getTitleLabel("Main Menu");
        label.setPadding(new Insets(10, 0, 10, 0));
        label.setAlignment(Pos.TOP_CENTER);
        getRootPane().setTop(label);
    }

    /**
     * Setup the left portion of the main menu which contains buttons that bring up information about the users roster
     */
    private void setupLeftBox() {
        VBox vbox = new VBox(2);
        vbox.getChildren().add(getBoldLabel(String.format("Your Team: %s", getUserTeam().getName())));
        int[] winLoss = LeagueFunctions.getTeamRecord(getUserTeam());
        vbox.getChildren().add(getBoldLabel(String.format("Record: %d - %d", winLoss[0], winLoss[1])));
        vbox.getChildren().add(getBoldLabel("\nRoster:"));
        for (Player p : LeagueFunctions.getTeam(userTeam.getID()).getRankedRoster()) {
            Button button = new Button(p.getName());
            setupPlayerButton(p, button);
            vbox.getChildren().add(button);
        }
        getRootPane().setLeft(vbox);
    }

    /**
     * Configures an action handler for the player buttons. Displays information about the player in the
     * center view
     */
    private void setupPlayerButton(Player p, Button b) {
        b.setOnAction(e -> {
            VBox playerBox = new VBox(3);
            playerBox.getChildren().add(getTitleLabel(p.getName()));
            playerBox.getChildren().add(new HBox(10, getBoldLabel("Overall Rating: "),
                    getStandardLabel(String.valueOf(p.getOverallPlayerRating()))));
            playerBox.getChildren().add(getTitleLabel("\nPlayer Attributes"));
            for (PlayerAttributes attr : PlayerAttributes.values()) {
                playerBox.getChildren().add(new HBox(10,
                        getBoldLabel(attr.toString()),
                        getStandardLabel(String.valueOf(p.getPlayerAttribute(attr)))));
            }
            playerBox.getChildren().add(getTitleLabel("Average Statistics"));
            if (LeagueFunctions.teamHasNotPlayedGames(getUserTeam())) {
                playerBox.getChildren().add(getBoldLabel("Team has not yet played any games"));
            } else {
                ScrollPane pane = new ScrollPane();
                pane.setVmax(350);
                VBox stats = new VBox(5);
                for (PlayerStat stat : PlayerStat.values()) {
                    stats.getChildren().add(new HBox(10,
                            getBoldLabel(stat.toString()),
                            getStandardLabel(String.valueOf(p.getStatContainer().getAvgValueOfStat(stat)))));
                }
                pane.setContent(stats);
                playerBox.getChildren().add(pane);
            }
            getRootPane().setCenter(playerBox);
        });
    }

    /**
     * The low portion of the screen is used to display information about other teams.
     */
    private void setupLowBox() {
        HBox hBox = new HBox(5);
        hBox.getChildren().add(getBoldLabel("Other Teams"));
        for (Team t : LeagueFunctions.getAllTeams()) {
            if (t == getUserTeam())
                continue;
            Button b = new Button(t.getName());
            setupOtherTeamButton(t, b);
            hBox.getChildren().add(b);
        }
        getRootPane().setBottom(hBox);
    }

    private void updateRecord() {
        int[] winLoss = LeagueFunctions.getTeamRecord(getUserTeam());
        ((VBox) getRootPane().getLeft())
                .getChildren().set(1, getBoldLabel(String.format("Record: %d - %d", winLoss[0], winLoss[1])));
    }

    private void setupOtherTeamButton(Team t, Button b) {
        b.setOnAction(e -> {
            VBox teamBox = new VBox(3);
            teamBox.getChildren().add(getTitleLabel(t.getName()));
            int[] winLoss = LeagueFunctions.getTeamRecord(t);
            teamBox.getChildren().add(getBoldLabel(String.format("Record: %d - %d", winLoss[0], winLoss[1])));
            teamBox.getChildren().add(getBoldLabel("Roster: "));
            for (Player p : t.getRankedRoster()) {
                HBox playerBox = new HBox(10, getBoldLabel(p.getName()),
                        getBoldLabel("Overall Rating: "),
                        getStandardLabel(String.valueOf(p.getOverallPlayerRating())));
                Button moreInfo = new Button("More Info");
                moreInfo.setOnAction(e2 -> {
                    setupPlayerButton(p, moreInfo);
                });
                playerBox.getChildren().add(moreInfo);
                teamBox.getChildren().add(playerBox);
            }
            getRootPane().setCenter(teamBox);
        });
    }


    public Team getUserTeam() {
        return userTeam;
    }

    private Label getTitleLabel(String label) {
        Label l = getLabel(label);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        return l;
    }

    private Label getBoldLabel(String label) {
        Label l = getLabel(label);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        return l;
    }

    private Label getStandardLabel(String label) {
        Label l = getLabel(label);
        l.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        return l;
    }

    private Label getLabel(String label) {
        return new Label(label);
    }
}
