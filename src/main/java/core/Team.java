package core;

import com.oracle.javafx.jmx.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.LinkedList;
import java.util.List;

public class Team extends AbstractEntity {
    private static final String pathToCitiesCSV = "./resources/us-cities.csv";
    private List<Player> roster = new LinkedList<>();

    Team(int id) {
        super(id);
    }

    static String getPathToCitiesCSV() {
        return pathToCitiesCSV;
    }

    List<Player> getRoster() {
        return roster;
    }

    void setRoster(List<Player> roster) {
        this.roster = roster;
    }

    void addPlayerToRoster(Player p) {
        assert !getRoster().contains(p);
        getRoster().add(p);
    }

    int getRosterSize() {
        return getRoster().size();
    }

    JSONObject getJSONObject() {
        JSONObject json = super.getJSONObject();
        JSONArray players = new JSONArray();
        for (Player p : getRoster())
            players.add(p.getJSONObject());
        json.put("players", players);
        return json;
    }

    String getJSONString() {
        return getJSONObject().toString();
    }

    @Override
    public String toString() {
        return "Team Location: " + getName() + "\n" + super.toString();
    }
}
