package core;

import gameplay.StatContainer;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.Math.toIntExact;

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
public class AbstractEntity implements Entity, Serializable {
    // Each abstract Entity is required to have a name, id, and a map of attributes
    private String entityName;
    private int id;
    private Map<String, Double> entityAttributes = new HashMap<>();
    private StatContainer statContainer = new StatContainer();

    /**
     * Constructors
     */
    AbstractEntity(int id) {
        setID(id);
    }

    AbstractEntity(int id, String name) {
        setID(id);
        setEntityName(name);
    }

    /**
     * Creates an AbstractEntity from a JSONObject, throwing a LeagueLoadException if the name and id field are not found
     * in the json file
     *
     * @param json json object to load entity from
     * @return new AbstractEntity with id and name
     * @throws Utils.LeagueLoadException if id and name not found
     */
    static AbstractEntity loadEntityFromJSON(JSONObject json) throws Utils.LeagueLoadException {
        if (!json.containsKey("name"))
            throw new Utils.LeagueLoadException("name", json);
        if (!json.containsKey("id"))
            throw new Utils.LeagueLoadException("id", json);
        return new AbstractEntity(toIntExact((Long) json.get("id")), (String) json.get("name"));
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
    public Map<String, Double> getEntityAttributes() {
        return entityAttributes;
    }

    @Override
    public void setEntityAttributes(Map<String, Double> attributes) {
        this.entityAttributes = attributes;
    }

    /**
     * Checks whether an attribute exists for this entity
     *
     * @param attribute the attribute to find
     * @return true if the attribute is in the map, false otherwise
     */
    @Override
    public boolean entityAttributeExists(String attribute) {
        return entityAttributes.containsKey(attribute);
    }

    /**
     * Set an entity attribute
     *
     * @param attribute the attribute to set
     * @param value     the value to set the attribute to
     */
    @Override
    public void setEntityAttribute(String attribute, double value) {
        getEntityAttributes().put(attribute, value);
    }

    /**
     * @param attribute the entity attribute to find
     * @return value of the attribute
     */
    @Override
    public double getEntityAttribute(String attribute) {
        assert entityAttributeExists(attribute);
        return getEntityAttributes().get(attribute);
    }

    /**
     * Create a JSON Object representation of this Abstract Entity
     * so that it can be preserved
     *
     * @return JSONObject
     */
    @Override
    public JSONObject getJSONObject() {
        JSONObject json = new JSONObject();
        json.put("id", this.getID());
        json.put("name", this.getName());
        Set<String> attrs = this.getEntityAttributes().keySet();
        for (String attr : attrs)
            json.put(attr, this.getEntityAttribute(attr));
        return json;
    }

    @Override
    public String getJSONString() {
        return getJSONObject().toJSONString();
    }

    @Override
    public StatContainer getStatContainer() {
        return statContainer;
    }

    @Override
    public void setStatContainer(StatContainer statContainer) {
        this.statContainer = statContainer;
    }


    @Override
    public String toString() {
        return "ID: " + getID();
    }


}
