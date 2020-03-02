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
    String idName;
    private String entityName;
    private int id;
    private ObservableMap<String, Object> entityAttributes;

    AbstractEntity(int id, String name, String idName, String tableName) throws SQLException {
        this.id = id;
        this.entityName = name;
        this.idName = idName;
        this.tableName = tableName;
        if (!entityExistsInDatabase()) {
            createEntityInDatabase(id, name);
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

    @Override
    public boolean entityExistsInDatabase() throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM " + tableName + " WHERE " + idName + "=" + id + ");";
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
        return id;
    }

    @Override
    public void setID(int id) {
        this.id = id;
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
                executeSQL("UPDATE " + tableName + " SET " + attribute + "=" + value + " WHERE " + idName + "=" + getID());
    }


    @Override
    public Object getEntityAttribute(String attribute) {
        assert entityAttributeExists(attribute);
        return getEntityAttributes().get(attribute);
    }


    @Override
    public void createEntityInDatabase(int id, String name) {
        String sql = "INSERT INTO " + tableName + "(" + idName + ", name) VALUES(?, ?);";
        PreparedStatement statement = DatabaseConnection.getInstance().getBlankPreparedStatement(sql);
        try {
            statement.setInt(1, id);
            statement.setString(2, name);
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
                    attr + " from " + tableName + " WHERE " + idName + "=" + getID());
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
