package core;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class Team extends AbstractEntity {
    private static final String pathToCitiesCSV = "./resources/us-cities.csv";
    private List<Player> roster = new LinkedList<>();

    Team(int id) {
        super(id);
    }

    Team(AbstractEntity previous) {
        super(previous.getID(), previous.getName());
        setEntityAttributes(previous.getEntityAttributes());
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

    static Team loadTeamFromJSON(JSONObject json) throws Utils.LeagueLoadException {
        Team entity = new Team(AbstractEntity.loadEntityFromJSON(json));
        for (TeamAttributes attr : TeamAttributes.values()) {
            if (!json.containsKey(attr.toString())) {
                throw new Utils.LeagueLoadException(attr.toString(), json);
            } else {
                entity.setEntityAttribute(attr.toString(), (double) json.get(attr.toString()));
            }
        }
        if (!json.containsKey("players"))
            throw new Utils.LeagueLoadException("players", json);
        JSONArray players = (JSONArray) json.get("players");
        for (Object player : players) {
            entity.addPlayerToRoster(Player.loadPlayerFromJSON((JSONObject) player));
        }
        return entity;
    }

    @Override
    public String toString() {
        return "Team Location: " + getName() + "\n" + super.toString();
    }
}
