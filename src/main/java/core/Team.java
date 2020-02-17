package core;

import gameplay.StatContainer;
import gameplay.TeamStat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * CS-622
 * Team.java
 * <p>
 * The Team object represents a team of players in the League.
 *
 * @author Andras Palfi apalfi@bu.edu
 * @version 1.0
 */
public class Team extends AbstractEntity {
    private static final String pathToCitiesCSV = "./resources/us-cities.csv";
    // Store a list of players on this team
    private List<Player> roster = new LinkedList<>();

    Team(int id) {
        super(id);
    }

    private Team(AbstractEntity previous) {
        super(previous.getID(), previous.getName());
        setEntityAttributes(previous.getEntityAttributes());
        setStatContainer(new StatContainer<TeamStat, Integer>());
    }

    static String getPathToCitiesCSV() {
        return pathToCitiesCSV;
    }

    /**
     * Loads a team object from a JSON object
     */
    static Team loadTeamFromJSON(JSONObject json) throws Utils.LeagueLoadException {
        Team entity = new Team(AbstractEntity.loadEntityFromJSON(json));
        for (TeamAttributes attr : TeamAttributes.values()) {
            if (!json.containsKey(attr.toString())) {
                throw new Utils.LeagueLoadException(attr.toString(), json);
            } else {
                entity.setEntityAttribute(attr.toString(), (double) json.get(attr.toString()));
            }
        }
        if (!json.containsKey("players"))
            throw new Utils.LeagueLoadException("players", json);
        JSONArray players = (JSONArray) json.get("players");
        for (Object player : players) {
            entity.addPlayerToRoster(Player.loadPlayerFromJSON((JSONObject) player));
        }
        return entity;
    }

    public List<Player> getRoster() {
        return roster;
    }

    void setRoster(List<Player> roster) {
        this.roster = roster;
    }

    public List<Player> getRankedRoster() {
        List<Player> rankedRoster = new LinkedList<>(getRoster());
        rankedRoster.sort(Comparator.comparingInt(Player::getOverallPlayerRating));
        Collections.reverse(rankedRoster);
        return rankedRoster;
    }

    /**
     * Returns a sorted list of this teams roster, sorted based off which player has the highest attribute
     *
     * @return List<Player>
     */
    public List<Player> getSortedRosterBasedOffPlayerAttributes(PlayerAttributes attr) {

        List<Map.Entry<Player, Double>> sortedRoster = new LinkedList<>();
        for (Player p : getRoster()) {
            sortedRoster.add(new AbstractMap.SimpleEntry<>(p, p.getPlayerAttribute(attr)));
        }
        Collections.sort(sortedRoster, new Comparator<Map.Entry<Player, Double>>() {
            public int compare(Map.Entry<Player, Double> o1, Map.Entry<Player, Double> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        List<Player> sorted = new LinkedList<>();
        for (Map.Entry<Player, Double> entry : sortedRoster) {
            sorted.add(entry.getKey());
        }
        Collections.reverse(sorted);
        return sorted;
    }

    /**
     * Adds a new player to this team roster
     *
     * @param p Player
     */
    void addPlayerToRoster(Player p) {
        assert !getRoster().contains(p);
        getRoster().add(p);
    }

    int getRosterSize() {
        return getRoster().size();
    }

    /**
     * Returns the overall rating of this team which is the average of all of its players overall ratings
     *
     * @return
     */
    public double getOverallTeamRating() {
        double sum = 0.0;
        for (double attrVal : getEntityAttributes().values())
            sum += attrVal;
        return (int) ((sum / getEntityAttributes().size()) * 100);
    }

    /**
     * Resets each players energy level to 1.0
     */
    public void resetEnergyLevels() {
        for (Player p : getRoster())
            p.setPlayerEnergy(1.0);
    }

    public int getSumOfTeamStat(TeamStat stat) {
        return (Integer) getStatContainer().getSumOfStatContainer(stat);
    }

    @Override
    public JSONObject getJSONObject() {
        JSONObject json = super.getJSONObject();
        JSONArray players = new JSONArray();
        for (Player p : getRoster())
            players.add(p.getJSONObject());
        json.put("players", players);
        return json;
    }

    @Override
    public String getJSONString() {
        return getJSONObject().toString();
    }

    @Override
    public String toString() {
        return "Team Location: " + getName() + "\n" + super.toString();
    }
}
