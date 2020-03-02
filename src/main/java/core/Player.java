package core;


import attributes.PlayerAttributes;
import attributes.PlayerStatTypes;
import utilities.DatabaseConnection;
import utilities.Utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * CS-622
 * Player.java
 * <p>
 * The Player subclass of AbstractEntity represents a basketball player in the League
 *
 * @author Andras Palfi apalfi@bu.edu
 * @version 1.0
 */
public class Player extends AbstractEntity {

    static final List<PlayerAttributes> NON_GAME_RELATED_ATTRS = Arrays.asList(PlayerAttributes.AGE,
            PlayerAttributes.SALARY_AMOUNT, PlayerAttributes.SALARY_LENGTH, PlayerAttributes.TEAM_ID);
    // List of stats achieved by this player
    private List<PlayerStat> playerStats = new LinkedList<>();


    public Player(int id, String name) throws SQLException {
        super(id, name, "pid", "players");
        // Load any previous stats for this player
        ResultSet statEntries = DatabaseConnection.getInstance().getStatEntriesForPlayer(id);
        while (statEntries.next()) {
            playerStats.add(new PlayerStat(statEntries.getInt("pid"),
                    statEntries.getInt("tid"), statEntries.getInt("gid")));
        }
    }

    /**
     * Returns the over rating of this players, which is the avg of all of its attributes
     */
    public int getOverallPlayerRating() {
        double avg = 0.0;
        for (Map.Entry<String, Object> attributes : getEntityAttributes().entrySet())
            if (!NON_GAME_RELATED_ATTRS.contains(PlayerAttributes.valueOf(attributes.getKey())))
                avg += (Double) attributes.getValue();
        return (int) ((avg / getEntityAttributes().size()) * 100);
    }


    public double getPlayerEnergy() {
        return (double) getEntityAttribute("ENERGY");
    }

    public void setPlayerEnergy(double val) {
        setEntityAttribute("ENERGY", Math.min(val, 1.0));
    }


    public List<PlayerStat> getPlayerStats() {
        return playerStats;
    }

    public PlayerStat getPlayerStat(int gid) {
        for (PlayerStat stat : getPlayerStats())
            if (stat.getID() == gid)
                return stat;
        return null;
    }

    public void addPlayerStat(PlayerStat stat) {
        playerStats.add(stat);
    }

    public double getAvgValueOfPlayerStat(PlayerStatTypes statType) {
        return getSumOfPlayerStat(statType) / getPlayerStats().size();
    }

    public int getSumOfPlayerStat(PlayerStatTypes statType) {
        int sum = 0;
        for (PlayerStat stat : getPlayerStats())
            sum += (int) stat.getEntityAttribute(statType.toString());
        return sum;
    }

    @Override
    public void initializeAttributes() {
        for (String attribute : getAttributeNames()) {
            if (attribute.equals("ENERGY"))
                setEntityAttribute(attribute, 1.0);
            else if (attribute.equals("AGE"))
                setEntityAttribute(attribute, Utils.getRandomDouble(18, 40));
            else {
                if (!attribute.contains("SALARY") || attribute.equals("TEAM_ID"))
                    setEntityAttribute(attribute, Utils.getRandomDouble());
            }
        }
    }

    @Override
    public String toString() {
        return "Player Name: " + getName() + "\nPlayer Overall Rating: " + getOverallPlayerRating() + "\n" + super.toString();
    }
}
