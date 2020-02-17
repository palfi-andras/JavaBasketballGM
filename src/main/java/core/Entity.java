package core;

import gameplay.StatContainer;
import org.json.simple.JSONObject;

import java.util.Map;

/**
 * CS-622
 * Entity.java
 * <p>
 * A JavaBasketballGM entity is an object that has a name, and an ID, along with a map of attributes which are stored as
 * String:Double entries.These things define that particular entity. Each entity must also have the ability to save
 * itself to a JSON string, and load itself from a JSON string
 *
 * @author Andras Palfi apalfi@bu.edu
 * @version 1.0
 */
public interface Entity {

    String getName();

    void setEntityName(String name);

    int getID();

    void setID(int id);

    Map<String, Double> getEntityAttributes();

    void setEntityAttributes(Map<String, Double> attributes);

    boolean entityAttributeExists(String attribute);

    void setEntityAttribute(String attribute, double value);

    double getEntityAttribute(String attribute);

    JSONObject getJSONObject();

    String getJSONString();

    StatContainer getStatContainer();

    void setStatContainer(StatContainer statContainer);

}
