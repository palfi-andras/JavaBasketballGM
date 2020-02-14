package core;

import gameplay.PlayerStat;
import gameplay.TeamStat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
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
        saveLeague(league, league.getName());
    }

    public static void saveLeague(League league, String fileName) {
        String path = "./resources/" + fileName + ".json";
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(league.getJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean saveLeague(League league, File file, Team userTeam) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            JSONObject json = league.getJSONObject();
            json.put("userTeam", userTeam.getID());
            fileWriter.write(json.toString());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean serializeLeague(League league, String filename, Team userTeam) {
        try {
            FileOutputStream file = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(file);
            league.setUserTeam(userTeam);
            out.writeObject(league);
            out.close();
            file.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads a league from a JSON file
     *
     * @param fileName String
     * @return League
     */
    public static boolean loadLeague(String fileName) {
        try (FileReader reader = new FileReader(fileName)) {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(reader);
            League.loadLeagueFromJSON(json);
            return true;
        } catch (IOException | ParseException | LeagueLoadException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static Integer loadLeagueWithUserTeam(String fileName) {
        try (FileReader reader = new FileReader(fileName)) {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(reader);
            if (!json.containsKey("userTeam"))
                return null;
            Integer team = Integer.valueOf(json.get("userTeam").toString());
            json.remove("userTeam");
            League.loadLeagueFromJSON(json);
            return team;
        } catch (IOException | ParseException | LeagueLoadException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static boolean deserializeLeague(String filename) {
        League league;

        // Deserialization
        try {
            FileInputStream file = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(file);
            league = (League) in.readObject();
            League.getInstance(league);
            in.close();
            file.close();
            return true;
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return false;
    }


    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Map<TeamStat, Integer> createTeamStatIntMap() {
        Map<TeamStat, Integer> map = new LinkedHashMap<>();
        for (TeamStat stat : TeamStat.values())
            map.put(stat, 0);
        return map;
    }

    public static Map<PlayerStat, Integer> createPlayerStatIntMap() {
        Map<PlayerStat, Integer> map = new LinkedHashMap<>();
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
