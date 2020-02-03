package core;

import gameplay.GameSimulation;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class League extends AbstractEntity {

    private static final int NUM_TEAMS = 10;
    private static final int PLAYERS_PER_TEAM = 15;
    private static final int NUM_PLAYERS = (NUM_TEAMS * PLAYERS_PER_TEAM) + 100;

    private static final int NUM_TIMES_TEAMS_PLAY_EACH_OTHER = 6;
    private static final int NUM_GAMES_PER_TEAM = (NUM_TEAMS - 1) * NUM_TIMES_TEAMS_PLAY_EACH_OTHER;


    private static League instance = null;
    private static AtomicInteger idCreator = new AtomicInteger();
    private List<String> firstNames;
    private List<String> lastNames;
    private List<String> cities;
    private Random randomInstance = new Random(System.currentTimeMillis());
    private List<AbstractEntity> entities = new LinkedList<>();
    private List<GameSimulation> games = new LinkedList<>();
    private List<Team> gameResults = new LinkedList<>();


    private League(int id) {
        super(id);
        initialize();
    }

    private League(int id, String name) {
        super(id);
        setID(id);
        setEntityName(name);
    }

    private static AtomicInteger getIdCreator() {
        return idCreator;
    }

    public static boolean loaded() {
        return instance != null;
    }


    public static League getInstance() {
        if (instance == null)
            instance = new League(0);
        return instance;
    }

    private static League getInstance(AbstractEntity previous) {
        instance = new League(previous.getID(), previous.getName());
        return instance;
    }

    static League loadLeagueFromJSON(JSONObject json) throws Utils.LeagueLoadException {
        League league = League.getInstance(AbstractEntity.loadEntityFromJSON(json));
        if (!json.containsKey("teams"))
            throw new Utils.LeagueLoadException("teams", json);
        JSONArray teams = (JSONArray) json.get("teams");
        for (Object team : teams) {
            Team t = Team.loadTeamFromJSON((JSONObject) team);
            league.addEntity(t);
            for (Player p : t.getRoster())
                league.addEntity(p);
        }
        return league;
    }

    List<String> getFirstNames() {
        return firstNames;
    }

    private void setFirstNames(List<String> firstNames) {
        this.firstNames = firstNames;
    }

    private String getRandomFirstName() {
        return getFirstNames().get(getRandomInteger(getFirstNames().size() - 1));
    }

    private List<String> getLastNames() {
        return lastNames;
    }

    private void setLastNames(List<String> lastNames) {
        this.lastNames = lastNames;
    }

    private String getRandomLastName() {
        return getLastNames().get(getRandomInteger(getLastNames().size() - 1));
    }

    private List<String> getCities() {
        return cities;
    }

    private void setCities(List<String> cities) {
        this.cities = cities;
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

    private List<AbstractEntity> getEntities() {
        return entities;
    }

    private List<GameSimulation> getGames() {
        return games;
    }

    private void addGame(GameSimulation game) {
        getGames().add(game);
    }

    private List<Team> getGameResults() {
        return gameResults;
    }

    private void recordGameResult(Team winner) {
        getGameResults().add(winner);
    }

    private boolean entityExists(AbstractEntity entity) {
        return getEntities().contains(entity);
    }

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
            } else if (entity instanceof Team) {
                entity.setEntityName(getRandomCity());
            } else {
                throw new IllegalStateException("Unrecognized entity type");
            }
        }
        for (int p : getPlayerEntityIndexes())
            initializeAttributes(getEntities().get(p));
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

    public void automatedDraft() {
        assert getNumPlayers() == NUM_PLAYERS && getNumTeams() == NUM_TEAMS;
        List<Integer> sortedPlayerIndexes = new LinkedList<>(getRankedPlayerEntityIndexes());
        List<Integer> teamIndexes = getTeamEntityIndexes();
        while (!draftIsOver()) {
            for (int t : teamIndexes) {
                Player p = (Player) getEntities().get(sortedPlayerIndexes.remove(0));
                Team team = (Team) getEntities().get(t);
                ((Team) getEntities().get(t)).addPlayerToRoster(p);
            }
        }
        for (int t : getTeamEntityIndexes())
            initializeAttributes(getEntities().get(t));
    }

    private boolean draftIsOver() {
        for (int t : getTeamEntityIndexes()) {
            Team team = (Team) getEntities().get(t);
            if (team.getRosterSize() < PLAYERS_PER_TEAM)
                return false;
        }
        return true;
    }

    public void setupRoundRobinTournament() {
        int numDays = (getNumTeams() - 1);
        List<Integer> teamIndexes = new LinkedList<>(getTeamEntityIndexes());
        Team team0 = (Team) getEntities().get(teamIndexes.remove(0));
        for (int day = 0; day < numDays; day++) {
            int teamIdx = day % teamIndexes.size();
            Team nextTeam = (Team) getEntities().get(teamIndexes.get(teamIdx));
            addGame(new GameSimulation(team0, nextTeam));
            for (int idx = 1; idx < (getNumTeams() / 2); idx++) {
                int firstTeam = (day + idx) % teamIndexes.size();
                int secondTeam = (day + teamIndexes.size() - idx) % teamIndexes.size();
                Team t1 = (Team) getEntities().get(teamIndexes.get(firstTeam));
                Team t2 = (Team) getEntities().get(teamIndexes.get(secondTeam));
                addGame(new GameSimulation(t1, t2));
            }
        }
    }

    public void simulateRoundRobinTournament() {
        if (getGames().size() == 0) {
            System.out.println("There are no games to play!");
            return;
        }
        for (GameSimulation game : getGames())
            recordGameResult(game.simulateGame());
        System.out.println("\n\nRankings: ");
        Map<Team, Long> results = getGameResults().stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Map<Team, Long> sortedMapReverseOrder = results.entrySet().
                stream().
                sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        int i = 1;
        for (Map.Entry<Team, Long> entry : sortedMapReverseOrder.entrySet()) {
            System.out.println(i + ". " + entry.getKey().getName() + "\nWins: " + entry.getValue() + "\n");
            i++;
        }
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
            if (getEntities().get(i) instanceof Team)
                indexes.add(i);
        return indexes;
    }

    public void printAllTeams() {
        for (int t : getTeamEntityIndexes())
            System.out.println((Team) getEntities().get(t));
    }

    private List<Integer> getPlayerEntityIndexes() {
        List<Integer> indexes = new LinkedList<>();
        for (int i = 0; i <= getEntities().size() - 1; i++)
            if (getEntities().get(i) instanceof Player)
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

    public JSONObject getJSONObject() {
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

    private int getNextUniqueKey() {
        return getIdCreator().incrementAndGet();
    }

    private int getRandomInteger(int bound) {
        return getRandomInstance().nextInt(bound);
    }

    private double getRandomDouble() {
        return getRandomDouble(0.4, 1);
    }

    private double getRandomDouble(int low, int high) {
        return low + (high - low) * getRandomInstance().nextDouble();
    }

    private double getRandomDouble(double low, double high) {
        return low + (high - low) * getRandomInstance().nextDouble();
    }


}
