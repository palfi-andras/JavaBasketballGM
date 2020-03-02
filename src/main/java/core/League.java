package core;

import attributes.LeagueAttributes;
import utilities.CoreConfiguration;
import utilities.DatabaseConnection;
import utilities.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
    public static final int PLAYERS_PER_TEAM = CoreConfiguration.getInstance().getIntProperty
            ("league.players_per_team");
    private static final int NUM_TEAMS = CoreConfiguration.getInstance().getIntProperty("league.num_teams");
    // Number of players in the league is equal to the full roster size of each team + 100 extra free agents
    static final int NUM_PLAYERS = (NUM_TEAMS * PLAYERS_PER_TEAM) + 100;
    // Max number of threads that can be used in the thread pool
    private static final int MAX_NUM_THREADS = Runtime.getRuntime().availableProcessors() + 1;
    // League singleton
    private static League instance = null;
    // An AtomicInteger is a thread-safe way to create IDs for our entities.
    private static AtomicInteger idCreator = new AtomicInteger();
    // Lists of names needed to populate random names for the various Entities
    private List<String> firstNames;
    private List<String> lastNames;
    private List<String> cities;
    // Structures for Storing Entity Types
    private List<Team> teams;
    private List<Player> players;
    private List<GameSimulation> games;


    private League(int id, String name) throws SQLException {
        super(id, name, "lid", "league");

        teams = new LinkedList<>();
        players = new LinkedList<>();
        games = new LinkedList<>();
        // Check if this is the first time this league has been launched
        ResultSet playerEntries = DatabaseConnection.getInstance().getAllPlayerEntries();
        ResultSet teamEntries = DatabaseConnection.getInstance().getAllTeamEntries();
        ResultSet gameEntries = DatabaseConnection.getInstance().getAllGameEntries();
        if (!playerEntries.next() || !teamEntries.next() || !gameEntries.next())
            buildLeague();
        else
            rebuildLeague(playerEntries, teamEntries, gameEntries);

    }

    private static AtomicInteger getIdCreator() {
        return idCreator;
    }


    public static League getInstance() {
        assert instance != null;
        return instance;
    }

    public static League getInstance(int id, String name) throws SQLException {
        assert instance == null;
        instance = new League(id, name);
        return instance;
    }

    public static int getMaxNumThreads() {
        return MAX_NUM_THREADS;
    }

    public static int getNextUniqueKey() {
        return getIdCreator().incrementAndGet();
    }

    private void buildLeague() throws SQLException {
        firstNames = getFirstRowFromCSVFile(CoreConfiguration.getInstance().
                getStringProperty("file_path.first_names_csv"));
        lastNames = getFirstRowFromCSVFile(CoreConfiguration.getInstance().
                getStringProperty("file_path.last_names_csv"));
        cities = getFirstRowFromCSVFile(CoreConfiguration.getInstance().
                getStringProperty("file_path.cities_csv"));
        for (int i = 0; i < NUM_PLAYERS; i++)
            players.add(new Player(getNextUniqueKey(), String.format("%s %s",
                    getRandomFirstName(), getRandomLastName())));
        for (int i = 0; i < NUM_TEAMS; i++)
            teams.add(new Team(getNextUniqueKey(), getRandomCity()));

    }

    private void rebuildLeague(ResultSet playerEntries, ResultSet teamEntries, ResultSet gameEntries) throws SQLException {
        playerEntries.beforeFirst();
        teamEntries.beforeFirst();
        gameEntries.beforeFirst();
        while (playerEntries.next()) {
            int pid = playerEntries.getInt("pid");
            String name = playerEntries.getString("name");
            //Reload this player into the players list from the db
            players.add(new Player(pid, name));
        }
        while (teamEntries.next()) {
            int tid = teamEntries.getInt("tid");
            String name = teamEntries.getString("name");
            teams.add(new Team(tid, name));
        }
        while (gameEntries.next()) {
            int gid = gameEntries.getInt("gid");
            int homeTeamID = gameEntries.getInt("HOME_TEAM");
            int awayTeamID = gameEntries.getInt("AWAY_TEAM");
            games.add(new GameSimulation(getTeam(homeTeamID),
                    getTeam(awayTeamID), gid));
        }
    }

    @Override
    public void initializeAttributes() {
        setEntityAttribute(LeagueAttributes.SALARY_CAP.toString(), CoreConfiguration.getInstance().
                getIntProperty("league.salary_cap"));
        setEntityAttribute(LeagueAttributes.USER_TEAM.toString(), null);
    }

    /**
     * @return A random first name
     */
    private String getRandomFirstName() {
        return firstNames.get(
                Utils.getRandomInteger(firstNames.size() - 1));
    }

    /**
     * @return a random last name
     */
    private String getRandomLastName() {
        return lastNames.get(
                Utils.getRandomInteger(lastNames.size() - 1));
    }

    public Team getUserTeam() {
        return getTeam((int) getEntityAttribute(LeagueAttributes.USER_TEAM.toString()));
    }

    public void setUserTeam(Team userTeam) {
        setEntityAttribute(LeagueAttributes.USER_TEAM.toString(),
                userTeam.getID());
    }

    private String getRandomCity() {
        int i = Utils.getRandomInteger(cities.size() - 1);
        String city = cities.get(i);
        cities.remove(i);
        return city;
    }

    public List<GameSimulation> getGames() {
        return games;
    }

    public GameSimulation getGame(int gid) {
        for (GameSimulation g : getGames())
            if (g.getID() == gid)
                return g;
        return null;
    }

    void addGame(GameSimulation game) {
        games.add(game);
    }

    public List<Team> getTeams() {
        return teams;
    }

    public int getNumTeams() {
        return teams.size();
    }

    public Team getTeam(int tid) {
        for (Team team : getTeams()) {
            if (team.getID() == tid)
                return team;
        }
        return null;
    }

    public Team getTeam(String name) {
        for (Team t : getTeams())
            if (t.getName().equals(name))
                return t;
        return null;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getNumPlayers() {
        return players.size();
    }

    public Player getPlayer(int pid) {
        for (Player p : getPlayers())
            if (p.getID() == pid)
                return p;
        return null;
    }

    public Entity getEntity(int id) {
        for (Player p : getPlayers())
            if (p.getID() == id)
                return p;
        for (Team t : getTeams())
            if (t.getID() == id)
                return t;
        for (GameSimulation g : getGames())
            if (g.getID() == id)
                return g;
        return null;
    }


    /**
     * Sets up a round robin tournament between the teams in the league. Adds each GameSimulation to the list of
     * games so that they may be simulated later.
     */
    public void setupRoundRobinTournament() {
        int numDays = (getNumTeams() - 1);
        List<Team> teams = new LinkedList<>(getTeams());
        Team team0 = teams.remove(0);
        try {
            for (int day = 0; day < numDays; day++) {
                int teamIdx = day % teams.size();
                Team nextTeam = teams.get(teamIdx);
                addGame(new GameSimulation(team0, nextTeam, getNextUniqueKey()));
                for (int idx = 1; idx < (getNumTeams() / 2); idx++) {
                    int firstTeam = (day + idx) % teams.size();
                    int secondTeam = (day + teams.size() - idx) % teams.size();
                    Team t1 = teams.get(firstTeam);
                    Team t2 = teams.get(secondTeam);
                    int gid = getNextUniqueKey();
                    // Initialize the stat objects for this future game
                    t1.addTeamStat(new TeamStat(t1.getID(), gid));
                    t2.addTeamStat(new TeamStat(t2.getID(), gid));
                    for (Player p : t1.getRoster())
                        p.addPlayerStat(new PlayerStat(p.getID(), t1.getID(), gid));
                    for (Player p : t2.getRoster())
                        p.addPlayerStat(new PlayerStat(p.getID(), t2.getID(), gid));
                    addGame(new GameSimulation(t1, t2, gid));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

}
