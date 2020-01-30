package core;

import com.oracle.javafx.jmx.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class League extends AbstractEntity {

    private static final int NUM_TEAMS = 10;
    private static final int PLAYERS_PER_TEAM = 15;
    private static final int NUM_PLAYERS = (NUM_TEAMS * PLAYERS_PER_TEAM) + 100;


    private static League instance = null;
    private static AtomicInteger idCreator = new AtomicInteger();
    private List<String> firstNames;
    private List<String> lastNames;
    private List<String> cities;
    private Random randomInstance = new Random(System.currentTimeMillis());
    private LinkedList<AbstractEntity> entities = new LinkedList<>();


    private League(int id) {
        super(id);
        initialize();
    }

    private static AtomicInteger getIdCreator() {
        return idCreator;
    }


    public static League getInstance() {
        if (instance == null)
            instance = new League(0);
        return instance;
    }

    public void save() {
        String fileName = "./resources/" + getName() + ".json";
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(getJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(String fileName) {
        try (FileReader reader = new FileReader(fileName)) {
            JSONParser parser = new JSONParser();
            JSONObject league = (JSONObject) parser.parse(reader);
            //this.setID();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void setFirstNames(List<String> firstNames) {
        this.firstNames = firstNames;
    }

    List<String> getFirstNames() {
        return firstNames;
    }

    private String getRandomFirstName() {
        return getFirstNames().get(getRandomInteger(getFirstNames().size() - 1));
    }

    private void setLastNames(List<String> lastNames) {
        this.lastNames = lastNames;
    }

    private List<String> getLastNames() {
        return lastNames;
    }

    private String getRandomLastName() {
        return getLastNames().get(getRandomInteger(getLastNames().size() - 1));
    }

    private void setCities(List<String> cities) {
        this.cities = cities;
    }

    private List<String> getCities() {
        return cities;
    }

    private String getRandomCity() {
        int i = getRandomInteger(getCities().size() - 1);
        String city = getCities().get(i);
        getCities().remove(i);
        return city;
    }

    private Random getRandomInstance() {
        return randomInstance;
    }

    public LinkedList<AbstractEntity> getEntities() {
        return entities;
    }

    private boolean entityExists(AbstractEntity entity) { return getEntities().contains(entity); }

    private void addEntity(AbstractEntity entity) {
        assert !entityExists(entity);
        getEntities().add(entity);
    }

    private void initialize() {
        this.setEntityName("league1");
        // Load the list of names for each entity type from CSVs
        setFirstNames(getFirstRowFromCSVFile(Player.getPathToFirstNameCSV()));
        setLastNames(getFirstRowFromCSVFile(Player.getPathToLastNameCSV()));
        setCities(getFirstRowFromCSVFile(Team.getPathToCitiesCSV()));
        // Create the required amount of teams and players
        for (int i = 0; i <= NUM_TEAMS; i++)
            addEntity(new Team(getNextUniqueKey()));
        for (int i = 0; i <= NUM_PLAYERS; i++)
            addEntity(new Player(getNextUniqueKey()));
        // Set the names of all of the Entity types
        for (AbstractEntity entity : getEntities()) {
            if (entity instanceof Player) {
                ((Player) entity).setEntityName(getRandomFirstName(), getRandomLastName());
            } else if (entity instanceof  Team) {
                entity.setEntityName(getRandomCity());
            } else {
                throw new IllegalStateException("Unrecognized entity type");
            }
        }
        for (int p : getPlayerEntityIndexes())
            initializeAttributes(getEntities().get(p));
        automatedDraft();
        for (int t : getTeamEntityIndexes())
            initializeAttributes(getEntities().get(t));

    }

    private void initializeAttributes(AbstractEntity entity) {
        if (entity instanceof Player)
            for (PlayerAttributes attr : PlayerAttributes.values())
                entity.setEntityAttribute(attr.toString(), getRandomDouble());

        if (entity instanceof Team)
            for (TeamAttributes attr : TeamAttributes.values()) {
                double val = 0.0;
                for (Player p : ((Team) entity).getRoster())
                    val += p.getEntityAttribute(attr.toString());
                entity.setEntityAttribute(attr.toString(), val / ((Team) entity).getRosterSize());
            }
    }

    private void automatedDraft() {
        assert getNumPlayers() == NUM_PLAYERS && getNumTeams() == NUM_TEAMS;
        List<Integer> sortedPlayerIndexes = getRankedPlayerEntityIndexes();
        List<Integer> teamIndexes = getTeamEntityIndexes();
        while (!draftIsOver()) {
            for (int t : teamIndexes) {
                Player p = (Player) getEntities().get(sortedPlayerIndexes.remove(0));
                Team team = (Team) getEntities().get(t);
                System.out.println(team.toString() + "\n has drafted: " + p.toString());
                ((Team) getEntities().get(t)).addPlayerToRoster(p);
            }
        }
    }

    private boolean draftIsOver() {
        for (int t : getTeamEntityIndexes()) {
            Team team = (Team) getEntities().get(t);
            if (team.getRosterSize() < PLAYERS_PER_TEAM)
                return false;
        }
        return true;
    }

    private int getNumTeams() {
        return getTeamEntityIndexes().size();
    }

    private int getNumPlayers() {
        return getPlayerEntityIndexes().size();
    }

    private AbstractEntity getEntity(int id) {
        for (AbstractEntity entity : getEntities()) {
            if (entity.getID() == id)
                return entity;
        }
        return null;
    }

    private List<Integer> getTeamEntityIndexes() {
        List<Integer> indexes = new LinkedList<>();
        for (int i = 0; i <= getEntities().size() - 1; i++)
            if (getEntities().get(i) instanceof  Team)
                indexes.add(i);
        return indexes;
    }

    private List<Integer> getPlayerEntityIndexes() {
        List<Integer> indexes = new LinkedList<>();
        for (int i = 0; i <= getEntities().size() - 1; i++)
            if (getEntities().get(i) instanceof  Player)
                indexes.add(i);
        return indexes;
    }

    private Integer getEntityIndex(AbstractEntity entity) {
        if (entity instanceof Team)
            for (int t : getTeamEntityIndexes())
                if (Objects.equals(getEntities().get(t), entity))
                    return t;
        if (entity instanceof Player)
            for (int p : getPlayerEntityIndexes())
                if (Objects.equals(getEntities().get(p), entity))
                    return p;
        return null;
    }

    private List<Integer> getRankedPlayerEntityIndexes() {
        List<Player> players = new LinkedList<>();
        for (int i : getPlayerEntityIndexes())
            players.add((Player) getEntities().get(i));
        players.sort(Comparator.comparingInt(Player::getOverallPlayerRating));
        List<Integer> sortedIndexes = new LinkedList<>();
        for (Player p : players)
            sortedIndexes.add(getEntityIndex(p));
        Collections.reverse(sortedIndexes);
        return sortedIndexes;
    }

    private List<String> getFirstRowFromCSVFile(String path) {
        List<String> values = new LinkedList<>();
        try {
            System.out.println(new File("").getAbsolutePath());
            BufferedReader csvReader = new BufferedReader(new FileReader(path));
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                values.add(data[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return values;
    }

    public JSONObject getJSONObject(){
        JSONObject json = super.getJSONObject();
        JSONArray teams = new JSONArray();
        for (int t : getTeamEntityIndexes())
            teams.add(getEntities().get(t).getJSONObject());
        json.put("teams", teams);
        return json;
    }

    public String getJSONString() {
        return getJSONObject().toString();
    }

    private int getNextUniqueKey() { return getIdCreator().incrementAndGet(); }
    private int getRandomInteger(int bound) { return getRandomInstance().nextInt(bound);}
    private double getRandomDouble() { return getRandomDouble(0, 1);}
    private double getRandomDouble(int low, int high) { return low + (high - low) * getRandomInstance().nextDouble();}




}
