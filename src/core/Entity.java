package core;

import java.util.Map;

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

}
