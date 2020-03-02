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
 * The PlayerStat class is an AbstractEntity that stores in its Observable Map the stats that a particular player
 * achieved in some game in the past. This class aligns itself with the player_stats table in the DB
 */
public class PlayerStat extends AbstractEntity {
    // The player id
    private int pid;
    // The team that the player played for during this game
    private int tid;
    // The specific game id
    private int gid;

    PlayerStat(int pid, int tid, int gid) throws SQLException {
        super(League.getInstance().getNextUniqueKey(),
                String.format("Player %d Stats playing for Team %d in Game %d", pid, tid, gid),
                "pid,tid,gid", "player_stats");
        this.pid = pid;
        this.gid = gid;
        this.tid = tid;
    }

    public int getGid() {
        return gid;
    }

    public int getPid() {
        return pid;
    }

    public int getTid() {
        return tid;
    }

    @Override
    public void createEntityInDatabase(int id, String name) {
        DatabaseConnection.getInstance().executeSQL(
                "INSERT INTO " + tableName + "(" + idName + ") VALUES (" + pid + "," + tid + "," + gid + ")"
        );
    }

    @Override
    public void initializeAttributes() {
        for (String attribute : getAttributeNames())
            setEntityAttribute(attribute, 0);
    }

    @Override
    public boolean entityExistsInDatabase() throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM " + tableName + " WHERE " + pid + "=" + pid +
                ",tid=" + tid + ",gid=" + gid + ");";
        ResultSet rs = DatabaseConnection.getInstance().executeQuery(sql);
        if (rs == null)
            return false;
        return rs.getInt(1) == 1;
    }

}
