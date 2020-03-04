package core;

import attributes.GameAttributes;
import attributes.LeagueAttributes;
import attributes.PlayerAttributes;
import attributes.PlayerStatTypes;
import attributes.TeamAttributes;
import attributes.TeamStatTypes;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import utilities.DatabaseConnection;
import utilities.Utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * CS -622
 * AbstractEntity.java
 * <p>
 * AbstractEntity is the class that implements the base of Entity. Each entity in the league is expected to expand
 * upon the AbstractEntity.
 *
 * @author Andras Palfi apalfi@bu.edu
 * @version 1.0
 */
public class AbstractEntity implements Entity {

    String tableName;
    private String entityName;
    private Map<String, Integer> ids;
    private ObservableMap<String, Object> entityAttributes;

    AbstractEntity(Map<String, Integer> ids, String name, String tableName) throws SQLException {
        this.ids = ids;
        this.entityName = name;
        this.tableName = tableName;
        if (!entityExistsInDatabase()) {
            createEntityInDatabase();
            entityAttributes = FXCollections.synchronizedObservableMap(FXCollections.observableHashMap());
            entityAttributes.addListener((MapChangeListener<String, Object>) change -> {
                if (change.wasAdded())
                    updateEntityAttribute(change.getKey(), change.getValueAdded());
                else if (change.wasRemoved())
                    updateEntityAttribute(change.getKey(), null);
            });
            initializeAttributes();
        } else {
            reloadEntityAttributes();
        }
    }

    public static Map<String, Integer> createIDMap(EntityType type, Integer... args) {
        Map<String, Integer> ids = new LinkedHashMap<>();
        if (type == EntityType.PLAYER) {
            assert args.length == 1;
            ids.put("pid", args[0]);
        } else if (type == EntityType.TEAM) {
            assert args.length == 1;
            ids.put("tid", args[0]);
        } else if (type == EntityType.LEAGUE) {
            assert args.length == 1;
            ids.put("lid", args[0]);
        } else if (type == EntityType.GAME_SIMULATION) {
            assert args.length == 1;
            ids.put("gid", args[0]);
        } else if (type == EntityType.PLAYER_STAT) {
            assert args.length == 3;
            ids.put("pid", args[0]);
            ids.put("tid", args[1]);
            ids.put("gid", args[2]);
        } else if (type == EntityType.TEAM_STAT) {
            assert args.length == 2;
            ids.put("tid", args[0]);
            ids.put("gid", args[1]);
        }
        return ids;
    }

    @Override
    public boolean entityExistsInDatabase() throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM " + tableName + " WHERE " + createEntityIDString() + ");";
        ResultSet rs = DatabaseConnection.getInstance().executeQuery(sql);
        if (rs == null)
            return false;
        return rs.getInt(1) == 1;
    }

    @Override
    public boolean entityCanHaveStats() {
        return this instanceof Team || this instanceof Player;
    }

    @Override
    public double getAvgValueOfStatForEntity(String stat) {
        assert entityCanHaveStats();
        if (this instanceof Team)
            return ((Team) this).getAvgValueOfTeamStat(TeamStatTypes.valueOf(stat));
        else
            return ((Player) this).getAvgValueOfPlayerStat(PlayerStatTypes.valueOf(stat));
    }

    /**
     * Getters and Setters for all member variables
     */

    @Override
    public String getName() {
        return entityName;
    }

    @Override
    public void setEntityName(String name) {
        this.entityName = name;

    }

    @Override
    public int getID() {
        Iterator<Map.Entry<String, Integer>> iterator = getIDS().entrySet().iterator();
        int id = iterator.next().getValue();
        assert !iterator.hasNext();
        return id;
    }

    @Override
    public Map<String, Integer> getIDS() {
        return this.ids;
    }

    @Override
    public String createEntityIDString() {
        Iterator<Map.Entry<String, Integer>> iterator = getIDS().entrySet().iterator();
        StringBuilder ids = new StringBuilder();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> idSet = iterator.next();
            ids.append(idSet.getKey()).append("=").append(idSet.getValue());
            if (iterator.hasNext())
                ids.append(",");
        }
        return ids.toString();
    }

    @Override
    public ObservableMap<String, Object> getEntityAttributes() {
        return entityAttributes;
    }

    @Override
    public boolean entityAttributeExists(String attribute) {
        return entityAttributes.containsKey(attribute);
    }

    @Override
    public void setEntityAttribute(String attribute, Object value) {
        getEntityAttributes().put(attribute, value);
    }

    @Override
    public void updateEntityAttribute(String attribute, Object value) {
        DatabaseConnection.getInstance().
                executeSQL("UPDATE " + tableName + " SET " + attribute + "=" + value + " WHERE " + createEntityIDString());
    }

    @Override
    public Object getEntityAttribute(String attribute) {
        assert entityAttributeExists(attribute);
        return getEntityAttributes().get(attribute);
    }

    @Override
    public void createEntityInDatabase() {
        Map<String, Integer> ids = getIDS();
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + "(");
        int count = 0;
        for (String s : ids.keySet()) {
            sql.append(s).append(",");
            count++;
        }
        sql.append("name) VALUES(?,");
        for (int i = 0; i < count; i++)
            sql.append("?,");
        sql.replace(sql.length() - 1, sql.length(), ")");
        PreparedStatement statement = DatabaseConnection.getInstance().getBlankPreparedStatement(sql.toString());
        try {
            int i = 1;
            for (int id : getIDS().values()) {
                statement.setInt(i, id);
                i++;
            }
            statement.setString(i, getName());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initializeAttributes() {
        for (String attribute : getAttributeNames()) {
            setEntityAttribute(attribute, Utils.getRandomDouble());
        }
    }

    @Override
    public void reloadEntityAttributes() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        for (String attr : getAttributeNames()) {
            ResultSet resultSet = DatabaseConnection.getInstance().executeQuery("SELECT " +
                    attr + " from " + tableName + " WHERE " + createEntityIDString());
            try {
                attributes.put(attr, resultSet.getObject(attr));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            entityAttributes = FXCollections.synchronizedObservableMap(FXCollections.observableMap(attributes));
        }
    }

    @Override
    public List<String> getAttributeNames() {
        List<String> names = new LinkedList<>();
        if (this instanceof Player) {
            for (PlayerAttributes a : PlayerAttributes.values())
                names.add(a.toString());
            return names;
        }

        if (this instanceof Team) {
            for (TeamAttributes a : TeamAttributes.values())
                names.add(a.toString());
            return names;
        }


        if (this instanceof League) {
            for (LeagueAttributes a : LeagueAttributes.values())
                names.add(a.toString());
            return names;
        }

        if (this instanceof GameSimulation) {
            for (GameAttributes a : GameAttributes.values())
                names.add(a.toString());
            return names;
        }

        if (this instanceof PlayerStat) {
            for (PlayerStatTypes statType : PlayerStatTypes.values())
                names.add(statType.toString());
            return names;
        }

        if (this instanceof TeamStat) {
            for (TeamStatTypes statType : TeamStatTypes.values())
                names.add(statType.toString());
            return names;
        }
        throw new RuntimeException("Unknown or Abstract Entity Type");
    }

    @Override
    public String toString() {
        return getName();
    }

}
