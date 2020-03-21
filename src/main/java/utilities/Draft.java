package utilities;

import attributes.LeagueAttributes;
import attributes.PlayerAttributes;
import core.League;
import core.Player;
import core.Team;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * CS 622
 * Draft.java
 * The Draft class was added to help aide a manual (user event driven) draft process to the League singleton.
 * It iteratively goes through each pick until all teams have filled their rosters
 *
 * @author apalfi
 * @version 1.0
 */
public class Draft {

    // The current pick number
    public int pickNum = 0;
    // To find out the order of teams drafting, we store an iterator of the teams
    private Iterator<Team> draftOrder = new LinkedList<>(League.getInstance().getTeams()).iterator();
    // A map of teams and the players they drafted with each pick number
    private Map<Team, Map<Player, Integer>> draftRecap = new LinkedHashMap<>();

    public Draft() {
        // Check to make sure the draft is not yet over
        assert !draftIsDone();
        for (Team t : League.getInstance().getTeams()) {
            assert 0 >= t.getRosterSize();
            draftRecap.put(t, new LinkedHashMap<>());
        }
    }

    /**
     * Performs an automated draft of the league. Each team takes turns drafting players until their roster is filled up.
     */
    public void automatedDraft() {
        List<Player> bestPlayers = LeagueFunctions.getBestPlayers();
        while (!draftIsDone()) {
            for (Team t : League.getInstance().getTeams())
                t.addPlayerToRoster(bestPlayers.remove(0));
        }
    }

    /**
     * Calls .next() on the draftOrder iterator, effectively getting the next team that is drafting. If we have reached
     * the end of the iterator, we reset it with the teams that still need to draft players
     *
     * @return
     */
    public Team getCurrentTeam() {
        if (!draftOrder.hasNext())
            resetDraftOrder();
        return draftOrder.next();
    }

    /**
     * Called when the class iterator has expired. Reset the iterator with teams that have yet to finish drafting.
     */
    private void resetDraftOrder() {
        List<Team> teams = new LinkedList<>();
        for (Team t : League.getInstance().getTeams())
            if (t.getRoster().size() < League.PLAYERS_PER_TEAM)
                teams.add(t);
        draftOrder = teams.iterator();
    }

    /**
     * Returns true if this draft is over. All teams must have filled their rosters
     *
     * @return boolean
     */
    public boolean draftIsDone() {
        for (Team t : League.getInstance().getTeams())
            if (t.getRoster().size() < League.PLAYERS_PER_TEAM)
                return false;
        setPlayerSalaries();
        return true;
    }

    /**
     * Randomly set all player salaries for each team that just drafted players. Out of the 15 players on each team:
     * - Best player takes 15 % of the total salary cap for the team
     * - Next 4 players take  10% each
     * - Next 5 take divide the remaining salary cap
     * <p>
     * Each players contract runs for 3 years by default
     */
    public void setPlayerSalaries() {
        for (Team t : League.getInstance().getTeams()) {
            List<Player> roster = t.getRankedRoster();
            int salaryCap = (int) League.getInstance().getEntityAttribute(LeagueAttributes.SALARY_CAP.toString());
            int amountToSubtract = 0;
            for (int i = 0; i < roster.size(); i++) {
                if (i == 0) {
                    roster.get(0).setEntityAttribute(PlayerAttributes.SALARY_AMOUNT.toString(), salaryCap * 0.15);
                    amountToSubtract += salaryCap * 0.15;
                } else if ((i >= 1) && (i <= 4)) {
                    roster.get(i).setEntityAttribute(PlayerAttributes.SALARY_AMOUNT.toString(), salaryCap * 0.10);
                    amountToSubtract += salaryCap * 0.10;
                } else {
                    salaryCap -= amountToSubtract;
                    amountToSubtract = 0;
                    roster.get(i).setEntityAttribute(PlayerAttributes.SALARY_AMOUNT.toString(), salaryCap / 5);
                }
                roster.get(0).setEntityAttribute(PlayerAttributes.SALARY_LENGTH.toString(), 3);
            }
        }
    }

    /**
     * Draft the player. Add to teams roster, update the teams attributes with this new player and increment the pick number
     *
     * @param p Player
     * @param t Team
     */
    public void draftPlayer(Player p, Team t) {
        if (p == null)
            return;
        assert LeagueFunctions.getFreeAgents().contains(p);
        assert t.getRosterSize() < League.PLAYERS_PER_TEAM;
        t.addPlayerToRoster(p);
        draftRecap.get(t).put(p, pickNum + 1);
        pickNum++;
    }

    /**
     * Creates a Table of players that have been drafted so far
     *
     * @return TableVIew<Entity>
     */
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
        List<Entity> teams = new LinkedList<>(League.getInstance().getTeams());
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
