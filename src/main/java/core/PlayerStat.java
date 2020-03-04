package core;

import utilities.DatabaseConnection;

import java.sql.PreparedStatement;
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

    PlayerStat(int pid, int tid, int gid) throws SQLException {
        super(createIDMap(EntityType.PLAYER_STAT, pid, tid, gid),
                String.format("Player %d Stats playing for Team %d in Game %d", pid, tid, gid), "player_stats");

    }


    @Override
    public void createEntityInDatabase() {
        String sql = "INSERT INTO " + tableName + "(pid,tid,gid,name) VALUES(?,?,?,?)";
        PreparedStatement statement = DatabaseConnection.getInstance().getBlankPreparedStatement(sql);
        try {
            statement.setInt(1, getIDS().get("pid"));
            statement.setInt(2, getIDS().get("tid"));
            statement.setInt(3, getIDS().get("gid"));
            statement.setString(4, getName());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void updateEntityAttribute(String attribute, Object value) {
        DatabaseConnection.getInstance().
                executeSQL("UPDATE " + tableName + " SET " + attribute + "=" + value + " WHERE tid=" + getIDS().get("tid")
                        + " AND gid=" + getIDS().get("gid") + " AND pid=" + getIDS().get("pid"));
    }

    @Override
    public void initializeAttributes() {
        for (String attribute : getAttributeNames())
            setEntityAttribute(attribute, 0);
    }

    @Override
    public boolean entityExistsInDatabase() throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM " + tableName + " WHERE pid=" + getIDS().get("pid") +
                " AND tid=" + getIDS().get("tid") + " AND gid=" + getIDS().get("gid") + ");";
        ResultSet rs = DatabaseConnection.getInstance().executeQuery(sql);
        if (rs == null)
            return false;
        return rs.getInt(1) == 1;
    }

}
