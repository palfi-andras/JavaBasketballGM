package core;

import gameplay.GameSimulation;
import gameplay.PlayerStat;
import gameplay.TeamStat;
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

/**
 * CS-622
 * League.java
 * <p>
 * The League class is the largest AbstractEntity concrete class, and is charged with managing all other AbstractEntities
 * such as the teams and the layers.
 * <p>
 * The League is used as a singleton since the program supports only one League at a time. This singleton contains most
 * of the core logic required to managing and simulating the league.
 *
 * @author Andras Palfi apalfi@bu.edu
 * @version 1.0
 */
public class League extends AbstractEntity {

    // Number of teams in the league
    public static final int NUM_TEAMS = 10;
    // Roster size
    private static final int PLAYERS_PER_TEAM = 15;
    // Number of players in the league is equal to the full roster size of each team + 100 extra free agents
    public static final int NUM_PLAYERS = (NUM_TEAMS * PLAYERS_PER_TEAM) + 100;

    // League singleton
    private static League instance = null;
    // An AtomicInteger is a thread-safe way to create IDs for our entities.
    private static AtomicInteger idCreator = new AtomicInteger();
    // Lists of names needed to populate random names for the various Entities
    private List<String> firstNames;
    private List<String> lastNames;
    private List<String> cities;
    // Store an instance of a random object to use for generating random numbers throughout the class
    private Random randomInstance = new Random(System.currentTimeMillis());
    // The List of all AbstractEntities in the League. This includes teams and players
    private List<AbstractEntity> entities = new LinkedList<>();
    // A list of games to play
    private List<GameSimulation> games = new LinkedList<>();
    // A list of teams who won games in this league
    private List<Team> gameResults = new LinkedList<>();

    /**
     * Constructors
     */

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

    /**
     * Loads a previous instance of a League object. This is used when loading from JSON.
     *
     * @param previous The Previous instance of this AbstractEntity base of the league
     * @return League object
     */
    private static League getInstance(AbstractEntity previous) {
        instance = new League(previous.getID(), previous.getName());
        return instance;
    }

    /**
     * Returns a League object produced from a JSON Object
     *
     * @param json The JSONObject to load from
     * @return League object
     * @throws Utils.LeagueLoadException if the JSON object has no team attributes
     */
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

    /**
     * @return A random first name
     */
    private String getRandomFirstName() {
        return getFirstNames().get(getRandomInteger(getFirstNames().size() - 1));
    }

    private List<String> getLastNames() {
        return lastNames;
    }

    private void setLastNames(List<String> lastNames) {
        this.lastNames = lastNames;
    }

    /**
     * @return a random last name
     */
    private String getRandomLastName() {
        return getLastNames().get(getRandomInteger(getLastNames().size() - 1));
    }

    private List<String> getCities() {
        return cities;
    }

    private void setCities(List<String> cities) {
        this.cities = cities;
    }

    /**
     * @return a random city
     */
    private String getRandomCity() {
        int i = getRandomInteger(getCities().size() - 1);
        String city = getCities().get(i);
        getCities().remove(i);
        return city;
    }

    private Random getRandomInstance() {
        return randomInstance;
    }

    /**
     * @return list of entities in the league
     */
    public List<AbstractEntity> getEntities() {
        return entities;
    }

    /**
     * @return list of games in the league
     */
    private List<GameSimulation> getGames() {
        return games;
    }

    /**
     * Adds a game to be simulated
     *
     * @param game GameSimulation to be added
     */
    private void addGame(GameSimulation game) {
        getGames().add(game);
    }

    /**
     * @return list of game result winners
     */
    private List<Team> getGameResults() {
        return gameResults;
    }

    /**
     * Records the winner of game
     *
     * @param winner Team the winner of the game
     */
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

    /**
     * Initialze the league. Set the name, and load the random name data from CSVs. Then add the required number of teams
     * and players to the pool of entities. For each entity, initialize their attributes once they are created.
     */
    private void initialize() {
        this.setEntityName("league1");
        // Load the list of names for each entity type from CSVs
        setFirstNames(getFirstRowFromCSVFile(Player.getPathToFirstNameCSV()));
        setLastNames(getFirstRowFromCSVFile(Player.getPathToLastNameCSV()));
        setCities(getFirstRowFromCSVFile(Team.getPathToCitiesCSV()));
        // Create the required amount of teams and players
        for (int i = 0; i < NUM_TEAMS; i++)
            addEntity(new Team(getNextUniqueKey()));
        for (int i = 0; i < NUM_PLAYERS; i++)
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

    /**
     * Initializes the attributes for an AbstractEntity. Goes through each value in either PlayerAttriobutes or TeamAttributes
     * and assigns it a random double value
     *
     * @param entity the entity the intitialze
     */
    private void initializeAttributes(AbstractEntity entity) {
        if (entity instanceof Player)
            for (PlayerAttributes attr : PlayerAttributes.values()) {
                if (attr == PlayerAttributes.ENERGY) {
                    entity.setEntityAttribute(attr.toString(), 1.0);
                    continue;
                }
                entity.setEntityAttribute(attr.toString(), Utils.round(getRandomDouble(), 2));
            }

        if (entity instanceof Team)
            for (TeamAttributes attr : TeamAttributes.values()) {
                double val = 0.0;
                for (Player p : ((Team) entity).getRoster())
                    val += p.getEntityAttribute(attr.toString());
                entity.setEntityAttribute(attr.toString(), val / ((Team) entity).getRosterSize());
            }
    }

    /**
     * Performs an automated draft of the league. Each team takes turns drafting players until their roster is filled up.
     */
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

    /**
     * @return true when the draft is over, i.e. each team has a full roster
     */
    private boolean draftIsOver() {
        for (int t : getTeamEntityIndexes()) {
            Team team = (Team) getEntities().get(t);
            if (team.getRosterSize() < PLAYERS_PER_TEAM)
                return false;
        }
        return true;
    }

    /**
     * Sets up a round robin tournament between the teams in the league. Adds each GameSimulation to the list of
     * games so that they may be simulated later.
     */
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

    /**
     * Simulates a round robin tournament of scheduled games.Prints the winner of each game along with final results at the end.
     */
    public void simulateRoundRobinTournament() {
        if (getGames().size() == 0) {
            System.out.println("There are no games to play!");
            return;
        }
        for (GameSimulation game : getGames()) {
            recordGameResult(game.simulateGame());
            recordStats(game);
        }

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

    /**
     * Updates Player and Team StatContainers after a game has ended
     */
    private void recordStats(GameSimulation game) {
        for (Map.Entry<TeamStat, Integer> entry : game.getHomeTeamStats().entrySet())
            getEntity(game.getHomeTeam().getID()).getStatContainer().updateStat(entry.getKey(), entry.getValue());

        for (Map.Entry<TeamStat, Integer> entry : game.getAwayTeamStats().entrySet())
            getEntity(game.getAwayTeam().getID()).getStatContainer().updateStat(entry.getKey(), entry.getValue());

        for (Player p : game.getPlayerStats().keySet())
            for (Map.Entry<PlayerStat, Integer> entry : game.getPlayerStats(p).entrySet())
                getEntity(p.getID()).getStatContainer().updateStat(entry.getKey(), entry.getValue());
    }

    public int getNumTeams() {
        return getTeamEntityIndexes().size();
    }

    public int getNumPlayers() {
        return getPlayerEntityIndexes().size();
    }

    private AbstractEntity getEntity(int id) {
        for (AbstractEntity entity : getEntities()) {
            if (entity.getID() == id)
                return entity;
        }
        return null;
    }

    /**
     * Returns the indexes in the LinkedList of entities that are teams
     *
     * @return <List><Integer>indexes</Integer></List>
     */
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

    /**
     * Returns the indexes of the entities that are players
     *
     * @return <List><Integer>indexes</Integer></List>
     */
    private List<Integer> getPlayerEntityIndexes() {
        List<Integer> indexes = new LinkedList<>();
        for (int i = 0; i <= getEntities().size() - 1; i++)
            if (getEntities().get(i) instanceof Player)
                indexes.add(i);
        return indexes;
    }

    /**
     * Returns the entity index of an AbstractEntity
     *
     * @param entity <AbstractEntity></AbstractEntity>
     * @return
     */
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

    /**
     * Returns a Sorted List of players in the league, sorted by their overall player rating.
     *
     * @return
     */
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

    @Override
    public JSONObject getJSONObject() {
        JSONObject json = super.getJSONObject();
        JSONArray teams = new JSONArray();
        for (int t : getTeamEntityIndexes())
            teams.add(getEntities().get(t).getJSONObject());
        json.put("teams", teams);
        return json;
    }

    @Override
    public String getJSONString() {
        return getJSONObject().toString();
    }

    public int getNextUniqueKey() {
        return getIdCreator().incrementAndGet();
    }

    public int getRandomInteger(int bound) {
        return getRandomInstance().nextInt(bound);
    }

    public int getRandomInteger(int low, int high) {
        return (int) getRandomDouble(low, high);
    }

    public double getRandomDouble() {
        return getRandomDouble(0.1, 1);
    }

    public double getRandomDouble(int low, int high) {
        return low + (high - low) * getRandomInstance().nextDouble();
    }

    public double getRandomDouble(double low, double high) {
        return low + (high - low) * getRandomInstance().nextDouble();
    }


}
