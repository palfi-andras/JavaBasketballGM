package core;

import javafx.collections.ObservableMap;

import java.sql.SQLException;
import java.util.List;
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

    Map<String, Integer> getIDS();

    String createEntityIDString();


    ObservableMap<String, Object> getEntityAttributes();

    boolean entityAttributeExists(String attribute);

    void setEntityAttribute(String attribute, Object value);

    void updateEntityAttribute(String attribute, Object value);

    Object getEntityAttribute(String attribute);

    void createEntityInDatabase();

    void initializeAttributes();

    void reloadEntityAttributes();

    List<String> getAttributeNames();

    boolean entityExistsInDatabase() throws SQLException;

    boolean entityCanHaveStats();

    double getAvgValueOfStatForEntity(String stat);


}
