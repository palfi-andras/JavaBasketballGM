package core;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {
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

    public static void saveLeague(League league) {
        String fileName = "./resources/" + league.getName() + ".json";
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(league.getJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static League loadLeague(String fileName) {
        try (FileReader reader = new FileReader(fileName)) {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(reader);
            return League.loadLeagueFromJSON(json);
        } catch (IOException | ParseException | LeagueLoadException e) {
            e.printStackTrace();
            return null;
        }
    }
}
