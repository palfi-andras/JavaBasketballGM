package core;

import utilities.DatabaseConnection;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MET CS 622
 *
 * @author apalfi
 * @version 1.0
 * <p>
 * The TeamStat class is an AbstractEntity that stores in its Observable Map the stats that a particular player
 * achieved in some game in the past. This class aligns itself with the player_stats table in the DB
 */
public class TeamStat extends AbstractEntity {
    // Team ID
    private int tid;
    // Game ID
    private int gid;

    TeamStat(int tid, int gid) throws SQLException {
        super(League.getInstance().getNextUniqueKey(),
                String.format("Team %d Stats for Game %d", tid, gid),
                "tid,gid", "team_stats");
        this.tid = tid;
        this.gid = gid;
    }

    public int getTid() {
        return tid;
    }

    public int getGid() {
        return gid;
    }

    @Override
    public void createEntityInDatabase(int id, String name) {
        DatabaseConnection.getInstance().executeSQL(
                "INSERT INTO " + tableName + "(" + idName + ") VALUES (" + tid + "," + gid + ")"
        );
    }


    /**
     * Initialize all attributes (in this case they are PlayerStats) with zeroes. These will be changed
     * once the game is played.
     */
    @Override
    public void initializeAttributes() {
        for (String attribute : getAttributeNames())
            setEntityAttribute(attribute, 0);
    }

    @Override
    public boolean entityExistsInDatabase() throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM " + tableName + " WHERE tid=" + tid + ",gid=" + gid + ");";
        ResultSet rs = DatabaseConnection.getInstance().executeQuery(sql);
        if (rs == null)
            return false;
        return rs.getInt(1) == 1;
    }

}
