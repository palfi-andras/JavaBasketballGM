package core;

import gameplay.PlayerStat;
import gameplay.TeamStat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * CS-622
 * Utils.java
 * <p>
 * THe utils class offers some wrapper functions that are useful throughout the program. The entire class is static
 * therefore it has no context about the current league other than information that is passed to it in function parameters
 *
 * @author Andras Palfi apalfi@bu.edu
 * @version 1.0
 */
public class Utils {
    /**
     * Save the current league instance to a JSON file
     *
     * @param league League
     */
    public static void saveLeague(League league) {
        String fileName = "./resources/" + league.getName() + ".json";
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(league.getJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a league from a JSON file
     *
     * @param fileName String
     * @return League
     */
    public static League loadLeague(String fileName) {
        try (FileReader reader = new FileReader(fileName)) {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(reader);
            return League.loadLeagueFromJSON(json);
        } catch (IOException | ParseException | LeagueLoadException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Map<TeamStat, Integer> createTeamStatIntMap() {
        Map<TeamStat, Integer> map = new HashMap<>();
        for (TeamStat stat : TeamStat.values())
            map.put(stat, 0);
        return map;
    }

    public static Map<PlayerStat, Integer> createPlayerStatIntMap() {
        Map<PlayerStat, Integer> map = new HashMap<>();
        for (PlayerStat stat : PlayerStat.values())
            map.put(stat, 0);
        return map;
    }

    // A custom exception to be used when trying to load a previous League from a json file. The most common usages of
    // this exception should be when a league is loaded and the entity that is being loaded does not have all of its
    // required attributes
    static class LeagueLoadException extends Exception {

        LeagueLoadException(String message) {
            super(message);
        }

        LeagueLoadException(String fieldName, JSONObject json) {
            this("ERROR! Cannot find field " + fieldName + " when trying to load entity from JSON: " + json.toJSONString());
        }
    }
}
