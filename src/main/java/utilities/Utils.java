package utilities;

import attributes.PlayerAttributes;
import attributes.PlayerStatTypes;
import attributes.TeamAttributes;
import attributes.TeamStatTypes;
import core.Entity;
import core.EntityType;
import core.GameSimulation;
import core.LeagueFunctions;
import core.Player;
import core.Team;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    private static Random random = new Random(System.currentTimeMillis());

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Map<TeamStatTypes, Integer> createTeamStatIntMap() {
        Map<TeamStatTypes, Integer> map = new LinkedHashMap<>();
        for (TeamStatTypes stat : TeamStatTypes.values())
            map.put(stat, 0);
        return map;
    }

    public static Map<PlayerStatTypes, Integer> createPlayerStatIntMap() {
        Map<PlayerStatTypes, Integer> map = new LinkedHashMap<>();
        for (PlayerStatTypes stat : PlayerStatTypes.values())
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
        idCol.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(e.getValue().getID()));
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

    private static TableColumn<Entity, Number> createEntityAttrTableColumn(String attr) {
        TableColumn<Entity, Number> col = new TableColumn<>(attr);
        col.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>((Number) e.getValue().getEntityAttribute(attr)));
        return col;
    }

    private static TableColumn<Entity, Double> createEntityAvgStatTableColumn(String stat) {
        TableColumn<Entity, Double> col = new TableColumn<>(stat);
        col.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(e.getValue().getAvgValueOfStatForEntity(stat)));
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
            for (PlayerStatTypes stat : PlayerStatTypes.values())
                entityTableView.getColumns().add(createEntityAvgStatTableColumn(stat.toString()));
        } else if (e instanceof Team)
            for (TeamStatTypes stat : TeamStatTypes.values())
                entityTableView.getColumns().add(createEntityAvgStatTableColumn(stat.toString()));
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
        for (TeamStatTypes stat : TeamStatTypes.values())
            teamStats.getColumns().add(createEntityGameStatTableColumn(stat, gs));
        teamStats.getItems().addAll(gs.getHomeTeam(), gs.getAwayTeam());
        return teamStats;
    }

    public static TableView<Entity> createGameSimulationPlayerStatTable(GameSimulation gs, List<Player> players) {
        TableView<Entity> playerStats = createEntityTable();
        for (PlayerStatTypes stat : PlayerStatTypes.values())
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

    public static double getRandomDouble() {
        return Utils.round(getRandomDouble(0.4, 1), 2);
    }

    public static double getRandomDouble(int low, int high) {
        return Utils.round(low + (high - low) * random.nextDouble(), 2);
    }

    public static double getRandomDouble(double low, double high) {
        return Utils.round(low + (high - low) * random.nextDouble(), 2);
    }

    public static int getRandomInteger(int bound) {
        return random.nextInt(bound);
    }

    public static int getRandomInteger(int low, int high) {
        return (int) getRandomDouble(low, high);
    }

}
