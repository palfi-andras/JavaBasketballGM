package core;


import gameplay.PlayerStat;
import gameplay.StatContainer;
import org.json.simple.JSONObject;

/**
 * CS-622
 * Player.java
 * <p>
 * The Player subclass of AbstractEntity represents a basketball player in the League
 *
 * @author Andras Palfi apalfi@bu.edu
 * @version 1.0
 */
public class Player extends AbstractEntity {

    private static final String pathToFirstNameCSV = "./resources/first-names.csv";
    private static final String pathToLastNameCSV = "./resources/last-names.csv";

    Player(int id) {
        super(id);
    }

    private Player(AbstractEntity previous) {
        super(previous.getID(), previous.getName());
        setEntityAttributes(previous.getEntityAttributes());
        setStatContainer(new StatContainer<PlayerStat, Integer>());
    }

    static String getPathToFirstNameCSV() {
        return pathToFirstNameCSV;
    }

    static String getPathToLastNameCSV() {
        return pathToLastNameCSV;
    }

    /**
     * Loads the player from a JSON Object
     */
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

    void setEntityName(String firstName, String lastName) {
        this.setEntityName(String.format("%s %s", firstName, lastName));
    }

    /**
     * Returns the over rating of this players, which is the avg of all of its attributes
     */
    public int getOverallPlayerRating() {
        double avg = 0.0;
        for (double attrVal : getEntityAttributes().values())
            avg += attrVal;
        return (int) ((avg / getEntityAttributes().size()) * 100);
    }

    /**
     * Returns this players average points per game
     */
    double getPlayerPointsPerGame() {
        return (double) getStatContainer().getAvgValueOfStat(PlayerStat.PTS);
    }

    public double getPlayerEnergy() {
        return getEntityAttribute("ENERGY");
    }

    public void setPlayerEnergy(double val) {
        setEntityAttribute("ENERGY", Math.min(val, 1.0));
    }

    public double getPlayerAttribute(PlayerAttributes attr) {
        return getEntityAttribute(attr.toString());
    }

    public int getSumOfPlayerStat(PlayerStat stat) {
        return (Integer) getStatContainer().getSumOfStatContainer(stat);
    }

    @Override
    public JSONObject getJSONObject() {
        return super.getJSONObject();
    }

    @Override
    public String getJSONString() {
        return getJSONObject().toString();
    }

    @Override
    public String toString() {
        return "Player Name: " + getName() + "\nPlayer Overall Rating: " + getOverallPlayerRating() + "\n" + super.toString();
    }
}
