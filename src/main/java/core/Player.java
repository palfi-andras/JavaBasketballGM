package core;


import org.json.simple.JSONObject;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Player extends AbstractEntity {

    private static final String pathToFirstNameCSV = "./resources/first-names.csv";
    private static final String pathToLastNameCSV = "./resources/last-names.csv";

    Player(int id) {
        super(id);
    }

    static String getPathToFirstNameCSV() {
        return pathToFirstNameCSV;
    }

    static String getPathToLastNameCSV() {
        return pathToLastNameCSV;
    }

    void setEntityName(String firstName, String lastName) {
        this.setEntityName(String.format("%s %s", firstName, lastName));
    }

    int getOverallPlayerRating() {
        double avg = 0.0;
        for (double attrVal : getEntityAttributes().values())
            avg += attrVal;
        return (int) ((avg / getEntityAttributes().size()) * 100);
    }

    JSONObject getJSONObject() {
        return super.getJSONObject();
    }

    String getJSONString() {
        return getJSONObject().toString();
    }

    @Override
    public String toString() {
        return "Player Name: " + getName() + "\nPlayer Overall Rating: " + getOverallPlayerRating() + "\n" +  super.toString();
    }
}
