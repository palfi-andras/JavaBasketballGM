package core;

import java.util.HashMap;
import java.util.Map;

public class AbstractEntity implements Entity {
    private String entityName;
    private int id;
    private Map<String, Double> entityAttributes = new HashMap<>();

    public AbstractEntity(int id) {
        setID(id);
    }

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

    @Override
    public boolean entityAttributeExists(String attribute) {
        return entityAttributes.containsKey(attribute);
    }

    @Override
    public void setEntityAttribute(String attribute, double value) {
        getEntityAttributes().put(attribute, value);
    }

    @Override
    public double getEntityAttribute(String attribute) {
        assert entityAttributeExists(attribute);
        return getEntityAttributes().get(attribute);
    }

    @Override
    public String toString() {
        return "ID: " + getID();
    }


}
