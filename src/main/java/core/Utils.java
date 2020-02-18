package core;

import gameplay.GameSimulation;
import gameplay.PlayerStat;
import gameplay.TeamStat;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * CS-622
 * Utils.java
 * <p>
 * THe utils class offers some wrapper functions that are useful throughout the program. The entire class is static
 * therefore it has no context about the current league other than information that is passed to it in function parameters
 *
 * @author Andras Palfi apalfi@bu.edu
 * @version 1.0
 */
public class Utils {
    /**
     * Save the current league instance to a JSON file
     *
     * @param league League
     */
    public static void saveLeague(League league) {
        saveLeague(league, league.getName());
    }

    public static void saveLeague(League league, String fileName) {
        String path = "./resources/" + fileName + ".json";
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(league.getJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean saveLeague(League league, File file, Team userTeam) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            JSONObject json = league.getJSONObject();
            json.put("userTeam", userTeam.getID());
            fileWriter.write(json.toString());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean serializeLeague(League league, String filename, Team userTeam) {
        try {
            FileOutputStream file = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(file);
            league.setUserTeam(userTeam);
            out.writeObject(league);
            out.close();
            file.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads a league from a JSON file
     *
     * @param fileName String
     * @return League
     */
    public static boolean loadLeague(String fileName) {
        try (FileReader reader = new FileReader(fileName)) {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(reader);
            League.loadLeagueFromJSON(json);
            return true;
        } catch (IOException | ParseException | LeagueLoadException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static Integer loadLeagueWithUserTeam(String fileName) {
        try (FileReader reader = new FileReader(fileName)) {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(reader);
            if (!json.containsKey("userTeam"))
                return null;
            Integer team = Integer.valueOf(json.get("userTeam").toString());
            json.remove("userTeam");
            League.loadLeagueFromJSON(json);
            return team;
        } catch (IOException | ParseException | LeagueLoadException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static boolean deserializeLeague(String filename) {
        League league;

        // Deserialization
        try {
            FileInputStream file = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(file);
            league = (League) in.readObject();
            League.getInstance(league);
            in.close();
            file.close();
            return true;
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return false;
    }


    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Map<TeamStat, Integer> createTeamStatIntMap() {
        Map<TeamStat, Integer> map = new LinkedHashMap<>();
        for (TeamStat stat : TeamStat.values())
            map.put(stat, 0);
        return map;
    }

    public static Map<PlayerStat, Integer> createPlayerStatIntMap() {
        Map<PlayerStat, Integer> map = new LinkedHashMap<>();
        for (PlayerStat stat : PlayerStat.values())
            map.put(stat, 0);
        return map;
    }

    public static TableView<Entity> createEntityTable() {
        TableView<Entity> entityTableView = new TableView<>();
        entityTableView.setEditable(false);
        entityTableView.getColumns().add(createEntityNameTableColumn());
        entityTableView.getColumns().add(createEntityIDTableColumn());
        return entityTableView;
    }

    public static TableView<GameSimulation> createScheduleTable(Team team) {
        TableView<GameSimulation> scheduleTable = new TableView<>();
        scheduleTable.setEditable(false);
        scheduleTable.setPrefHeight(600);
        TableColumn<GameSimulation, Integer> idCol = new TableColumn<>("ID");
        idCol.prefWidthProperty().bind(scheduleTable.widthProperty().multiply(0.2));
        idCol.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(e.getValue().getId()));
        TableColumn<GameSimulation, String> homeTeamCol = new TableColumn<>("Home Team");
        homeTeamCol.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(e.getValue().getHomeTeam().getName()));
        homeTeamCol.prefWidthProperty().bind(scheduleTable.widthProperty().multiply(0.4));
        TableColumn<GameSimulation, String> awayTeamCol = new TableColumn<>("Away Team");
        awayTeamCol.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(e.getValue().getAwayTeam().getName()));
        awayTeamCol.prefWidthProperty().bind(scheduleTable.widthProperty().multiply(0.4));
        scheduleTable.getColumns().addAll(idCol, homeTeamCol, awayTeamCol);
        scheduleTable.getItems().addAll(LeagueFunctions.getGamesForTeam(team));
        return scheduleTable;
    }

    private static TableColumn<Entity, String> createEntityNameTableColumn() {
        TableColumn<Entity, String> col = new TableColumn<>("Name");
        col.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(e.getValue().getName()));
        return col;
    }

    private static TableColumn<Entity, Integer> createEntityIDTableColumn() {
        TableColumn<Entity, Integer> col = new TableColumn<>("ID");
        col.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(e.getValue().getID()));
        return col;
    }

    private static TableColumn<Entity, Double> createEntityAttrTableColumn(String attr) {
        TableColumn<Entity, Double> col = new TableColumn<>(attr);
        col.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(e.getValue().getEntityAttribute(attr)));
        return col;
    }

    private static TableColumn<Entity, Integer> createEntityAvgStatTableColumn(Object stat) {
        TableColumn<Entity, Integer> col = new TableColumn<>(stat.toString());
        col.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>((Integer) e.getValue().getStatContainer().getAvgValueOfStat(stat)));
        return col;
    }

    private static TableColumn<Entity, Integer> createEntityGameStatTableColumn(Object stat, GameSimulation gs) {
        TableColumn<Entity, Integer> col = new TableColumn<>(stat.toString());
        col.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(gs.getGameStat(e.getValue(), stat)));
        return col;
    }

    public static TableView<Entity> createEntityAttributeTable(List<Entity> entities, EntityType type) {
        assert entities.size() > 0;
        TableView<Entity> entityTableView = createEntityTable();
        if (type == EntityType.PLAYER) {
            for (PlayerAttributes attr : PlayerAttributes.values())
                entityTableView.getColumns().add(createEntityAttrTableColumn(attr.toString()));
        } else if (type == EntityType.TEAM)
            for (TeamAttributes attr : TeamAttributes.values())
                entityTableView.getColumns().add(createEntityAttrTableColumn(attr.toString()));
        else
            throw new RuntimeException();
        entityTableView.getItems().addAll(entities);
        return entityTableView;
    }

    public static TableView<Entity> createEntityAttributeTable(Entity entity, EntityType type) {
        List<Entity> entities = new ArrayList<>();
        entities.add(entity);
        return createEntityAttributeTable(entities, type);
    }

    public static TableView<Entity> createEntityAvgStatsTable(List<Entity> entities) {
        assert entities.size() > 0;
        TableView<Entity> entityTableView = createEntityTable();
        Entity e = entities.get(0);
        if (e instanceof Player) {
            for (PlayerStat stat : PlayerStat.values())
                entityTableView.getColumns().add(createEntityAvgStatTableColumn(stat));
        } else if (e instanceof Team)
            for (TeamStat stat : TeamStat.values())
                entityTableView.getColumns().add(createEntityAvgStatTableColumn(stat));
        else
            throw new RuntimeException();
        entityTableView.getItems().addAll(entities);
        return entityTableView;
    }

    public static TableView<Entity> createEntityAvgStatsTable(Entity entity) {
        List<Entity> entities = new ArrayList<>();
        entities.add(entity);
        return createEntityAvgStatsTable(entities);
    }

    public static TableView<Entity> createGameSimulationTeamStatTable(GameSimulation gs) {
        TableView<Entity> teamStats = createEntityTable();
        for (TeamStat stat : TeamStat.values())
            teamStats.getColumns().add(createEntityGameStatTableColumn(stat, gs));
        teamStats.getItems().addAll(gs.getHomeTeam(), gs.getAwayTeam());
        return teamStats;
    }

    public static TableView<Entity> createGameSimulationPlayerStatTable(GameSimulation gs, List<Player> players) {
        TableView<Entity> playerStats = createEntityTable();
        for (PlayerStat stat : PlayerStat.values())
            playerStats.getColumns().add(createEntityGameStatTableColumn(stat, gs));
        playerStats.getItems().addAll(players);
        return playerStats;
    }

    public static TableView<Entity> createRosterTableForTeam(Team team) {
        TableView<Entity> rosterTable = createEntityTable();
        rosterTable.getColumns().remove(1);
        TableColumn<Entity, Integer> overallColl = new TableColumn<>("Overall Rating");
        overallColl.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(
                ((Player) e.getValue()).getOverallPlayerRating()
        ));
        rosterTable.getColumns().add(overallColl);
        rosterTable.getItems().addAll(team.getRankedRoster());
        return rosterTable;
    }

    public static TableView<Entity> createDraftTable() {
        List<Entity> freeAgents = new LinkedList<>(LeagueFunctions.getFreeAgents());
        TableView<Entity> playersTable = createEntityAttributeTable(freeAgents, EntityType.PLAYER);
        TableColumn<Entity, Integer> ovr = new TableColumn<>("Overall Rating");
        ovr.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(((Player) e.getValue()).getOverallPlayerRating()));
        playersTable.getColumns().add(2, ovr);
        return playersTable;
    }

    public static Label getTitleLabel(String label) {
        Label l = getLabel(label);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        return l;
    }

    public static Label getBoldLabel(String label) {
        Label l = getLabel(label);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        return l;
    }

    public static Label getStandardLabel(String label) {
        Label l = getLabel(label);
        l.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        return l;
    }

    public static Label getLabel(String label) {
        return new Label(label);
    }

    // A custom exception to be used when trying to load a previous League from a json file. The most common usages of
    // this exception should be when a league is loaded and the entity that is being loaded does not have all of its
    // required attributes
    static class LeagueLoadException extends Exception {

        LeagueLoadException(String message) {
            super(message);
        }

        LeagueLoadException(String fieldName, JSONObject json) {
            this("ERROR! Cannot find field " + fieldName + " when trying to load entity from JSON: " + json.toJSONString());
        }
    }
}
