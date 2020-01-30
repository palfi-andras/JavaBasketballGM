package core;


import org.json.simple.JSONObject;

public class Player extends AbstractEntity {

    private static final String pathToFirstNameCSV = "./resources/first-names.csv";
    private static final String pathToLastNameCSV = "./resources/last-names.csv";

    Player(int id) {
        super(id);
    }

    Player(AbstractEntity previous) {
        super(previous.getID(), previous.getName());
        setEntityAttributes(previous.getEntityAttributes());
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

    static Player loadPlayerFromJSON(JSONObject json) throws Utils.LeagueLoadException {
        Player entity = new Player(AbstractEntity.loadEntityFromJSON(json));
        for (PlayerAttributes attr : PlayerAttributes.values())
            if (!json.containsKey(attr.toString())) {
                throw new Utils.LeagueLoadException(attr.toString(), json);
            } else {
                entity.setEntityAttribute(attr.toString(), (double) json.get(attr.toString()));
            }
        return entity;
    }

    @Override
    public String toString() {
        return "Player Name: " + getName() + "\nPlayer Overall Rating: " + getOverallPlayerRating() + "\n" + super.toString();
    }
}
