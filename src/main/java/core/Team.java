package core;

import attributes.PlayerAttributes;
import attributes.PlayerStatTypes;
import attributes.TeamAttributes;
import attributes.TeamStatTypes;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import utilities.DatabaseConnection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
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

    public static final List<TeamAttributes> NON_GAME_RELATED_ATTRS = Arrays.asList(TeamAttributes.ROSTER);
    private List<TeamStat> teamStats = new LinkedList<>();

    public Team(int id, String name) throws SQLException {
        super(createIDMap(EntityType.TEAM, id), name, "teams");
        ResultSet statEntries = DatabaseConnection.getInstance().getStatEntriesForTeam(id);
        while (statEntries.next())
            teamStats.add(new TeamStat(statEntries.getInt("tid"), statEntries.getInt("gid")));
    }

    @Override
    public void initializeAttributes() {
        // First initialize the Team roster attribute
        ObservableList<Player>
                roster = FXCollections.observableArrayList();
        // The roster attribute stores an observable list. Whenever a player
        // is added to this team, the Team Attributes are re calculated with the
        // new players individual attributes taken into consideration
        roster.addListener((ListChangeListener<Player>) change -> {
            while (change.next()) {
                if (change.wasAdded())
                    for (Player p : change.getAddedSubList())
                        p.setEntityAttribute("TEAM_ID", getID());

                if (change.wasRemoved())
                    for (Player p : change.getRemoved())
                        p.setEntityAttribute("TEAM_ID", null);

                for (String attr : getAttributeNames()) {
                    try {
                        PlayerAttributes a = PlayerAttributes.valueOf(attr);
                        double avg = 0.0;
                        for (Player p : getRoster())
                            avg += (Double) p.getEntityAttribute(a.toString());
                        setEntityAttribute(a.toString(), avg / getRosterSize());
                    } catch (IllegalArgumentException ex) {
                        continue;
                    }
                }
                setEntityAttribute(TeamAttributes.ROSTER.toString(), roster);
            }
        });
        setEntityAttribute(TeamAttributes.ROSTER.toString(), roster);
        // Now initialize all other attributes, which are basically averages of player attributes for this team
        for (String attribute : getAttributeNames()) {
            if (!NON_GAME_RELATED_ATTRS.contains(TeamAttributes.valueOf(attribute))) {
                PlayerAttributes a = PlayerAttributes.valueOf(attribute);
                double avg = 0.0;
                if (getRosterSize() > 0) {
                    for (Player p : getRoster())
                        avg += (Double) p.getEntityAttribute(attribute);
                    setEntityAttribute(attribute, avg / getRoster().size());
                } else {
                    setEntityAttribute(attribute, avg);
                }
            }
        }
    }

    @Override
    public void updateEntityAttribute(String attribute, Object value) {
        if (!NON_GAME_RELATED_ATTRS.contains(TeamAttributes.valueOf(attribute)))
            super.updateEntityAttribute(attribute, value);
        else {
            assert attribute.equals("ROSTER");
            try {
                ByteOutputStream bos = new ByteOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(new LinkedList<Player>((ObservableList<Player>) value));
                byte[] byteArray = bos.getBytes();
                String sql = "UPDATE teams set ROSTER=? where tid=?";
                PreparedStatement statement = DatabaseConnection.getInstance().getBlankPreparedStatement(sql);
                try {
                    statement.setBytes(1, byteArray);
                    statement.setInt(2, getID());
                    statement.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void reloadEntityAttributes() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        for (String attr : getAttributeNames()) {
            ResultSet resultSet = DatabaseConnection.getInstance().executeQuery("SELECT " +
                    attr + " from " + tableName + " WHERE " + createEntityIDString());
            try {
                if (!Team.NON_GAME_RELATED_ATTRS.contains(TeamAttributes.valueOf(attr))) {
                    attributes.put(attr, resultSet.getObject(attr));
                } else {
                    ObjectInputStream ois = new ObjectInputStream(resultSet.getBinaryStream(1));
                    ObservableList<Player> roster = FXCollections.observableArrayList((List<Player>) ois.readObject());
                    attributes.put(attr, roster);
                }
            } catch (SQLException | IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }


    public ObservableList<Player> getRoster() {
        return (ObservableList<Player>) getEntityAttribute(TeamAttributes.ROSTER.toString());
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

        assert !Player.NON_GAME_RELATED_ATTRS.contains(attr);
        List<Map.Entry<Player, Double>> sortedRoster = new LinkedList<>();
        for (Player p : getRoster()) {
            sortedRoster.add(new AbstractMap.SimpleEntry<Player, Double>(p, (Double) p.getEntityAttribute(attr.toString())));
        }
        Collections.sort(sortedRoster, Comparator.comparing(Map.Entry::getValue));
        List<Player> sorted = new LinkedList<>();
        for (Map.Entry<Player, Double> entry : sortedRoster) {
            sorted.add(entry.getKey());
        }
        Collections.reverse(sorted);
        return sorted;
    }

    public List<Player> getSortedRosterBasedOffPlayerAvgStats(PlayerStatTypes stat) {
        List<Map.Entry<Player, Double>> sortedRoster = new LinkedList<>();
        for (Player p : getRoster())
            sortedRoster.add(new AbstractMap.SimpleEntry<>
                    (p, (Double) p.getAvgValueOfPlayerStat(stat)));
        Collections.sort(sortedRoster, Comparator.comparingDouble(Map.Entry::getValue));
        List<Player> sorted = new LinkedList<>();
        for (Map.Entry<Player, Double> entry : sortedRoster)
            sorted.add(entry.getKey());
        Collections.reverse(sorted);
        return sorted;
    }

    /**
     * Adds a new player to this team roster
     *
     * @param p Player
     */
    public void addPlayerToRoster(Player p) {
        assert !getRoster().contains(p);
        getRoster().add(p);
    }

    public void removePlayerFromRoster(Player p) {
        assert getRoster().contains(p);
        getRoster().remove(p);
    }

    public int getRosterSize() {
        return getRoster().size();
    }


    public double getOverallTeamRating() {
        double sum = 0.0;
        for (Map.Entry<String, Object> a : getEntityAttributes().entrySet())
            if (!NON_GAME_RELATED_ATTRS.contains(TeamAttributes.valueOf(a.getKey())))
                sum += (Double) a.getValue();
        return (int) ((sum / getEntityAttributes().size()) * 100);
    }

    /**
     * Resets each players energy level to 1.0
     */
    public void resetEnergyLevels() {
        for (Player p : getRoster())
            p.setPlayerEnergy(1.0);
    }


    public List<TeamStat> getTeamStats() {
        return teamStats;
    }

    public TeamStat getTeamStat(int gid) {
        for (TeamStat stat : getTeamStats())
            if (stat.getIDS().get("gid") == gid)
                return stat;
        return null;
    }

    public void addTeamStat(TeamStat stat) {
        teamStats.add(stat);
    }

    public double getAvgValueOfTeamStat(TeamStatTypes statType) {
        return getSumOfTeamStat(statType) / getTeamStats().size();
    }

    public int getSumOfTeamStat(TeamStatTypes statType) {
        int sum = 0;
        for (TeamStat stat : getTeamStats())
            sum += (int) stat.getEntityAttribute(statType.toString());
        return sum;
    }
}
