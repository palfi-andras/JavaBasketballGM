package core;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Draft {

    public int pickNum = 0;
    private Iterator<Team> draftOrder = LeagueFunctions.getAllTeams().iterator();
    private Map<Team, Map<Player, Integer>> draftRecap = new LinkedHashMap<>();

    public Draft() {
        assert !League.getInstance().draftIsOver();

        for (Team t : LeagueFunctions.getAllTeams()) {
            assert 0 >= LeagueFunctions.getRosterSize(t);
            draftRecap.put(t, new LinkedHashMap<>());
        }
    }

    public Team getCurrentTeam() {
        if (!draftOrder.hasNext())
            resetDraftOrder();
        return draftOrder.next();
    }

    private void resetDraftOrder() {
        List<Team> teams = new LinkedList<>();
        for (Team t : LeagueFunctions.getAllTeams())
            if (t.getRoster().size() < League.PLAYERS_PER_TEAM)
                teams.add(t);
        draftOrder = teams.iterator();
    }

    public boolean draftIsDone() {
        for (Team t : LeagueFunctions.getAllTeams())
            if (t.getRoster().size() < League.PLAYERS_PER_TEAM)
                return false;
        return true;
    }

    public void draftPlayer(Player p, Team t) {
        if (p == null)
            return;
        assert LeagueFunctions.getFreeAgents().contains(p);
        assert LeagueFunctions.getRosterSize(t) < League.PLAYERS_PER_TEAM;
        LeagueFunctions.getTeam(t).addPlayerToRoster(p);
        League.getInstance().initializeAttributes(LeagueFunctions.getTeam(t));
        draftRecap.get(t).put(p, pickNum + 1);
        pickNum++;
    }


    public TableView<Entity> createDraftRecapTable() {
        List<Entity> draftedPlayers = new LinkedList<>(getAllDraftedPlayers());
        TableView<Entity> recap = Utils.createEntityAttributeTable(draftedPlayers, EntityType.PLAYER);
        recap.getColumns().remove(1);
        TableColumn<Entity, Integer> pickNum = new TableColumn<>("Pick Number");
        TableColumn<Entity, Integer> ovr = new TableColumn<>("Overall Rating");
        TableColumn<Entity, Team> team = new TableColumn<>("Team");
        pickNum.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(draftRecap.get(
                LeagueFunctions.getPlayerTeam(
                        (Player) e.getValue())).
                get((Player) e.getValue())));
        team.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(
                LeagueFunctions.getPlayerTeam((Player) e.getValue())));
        ovr.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(((Player) e.getValue()).getOverallPlayerRating()));
        recap.getColumns().add(0, ovr);
        recap.getColumns().add(0, team);
        recap.getColumns().add(0, pickNum);
        return recap;
    }

    public TableView<Entity> createDraftOrderTable() {
        List<Entity> teams = new LinkedList<>(LeagueFunctions.getAllTeams());
        TableView<Entity> order = Utils.createEntityTable();
        order.getColumns().remove(1);
        order.getItems().addAll(teams);
        return order;
    }


    private List<Player> getAllDraftedPlayers() {
        List<Player> drafted = new LinkedList<>();
        for (Team t : draftRecap.keySet())
            drafted.addAll(draftRecap.get(t).keySet());
        return drafted;
    }

}
