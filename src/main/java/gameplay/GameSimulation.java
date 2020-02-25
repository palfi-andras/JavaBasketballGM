package gameplay;

import core.Entity;
import core.League;
import core.Player;
import core.PlayerAttributes;
import core.Team;
import core.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CS622
 * GameSimulation.java
 * <p>
 * The GameSimulation class is tasked with simulating a basketball game between the two teams.
 * <p>
 * The current implementation takes into consideration play-by-play mechanics with the offense.
 *
 * @author Andras Palfi apalfi@bu.edu
 * @version 1.0
 */
public class GameSimulation implements Serializable {
    // The amount of fouls a player can get before they foul out of the game
    private static final int FOUL_LIMIT = 6;
    // THe length in minutes of each quarter
    private static final int GAME_LENGTH_MIN_PER_QUARTER = 12;
    // The length in minutes of the entire game
    private static final int GAME_LENGTH_MIN = GAME_LENGTH_MIN_PER_QUARTER * 4;
    // The length in seconds of the entire game
    private static final int GAME_LENGTH_SECONDS = GAME_LENGTH_MIN * 60;
    // The amount of time the offense has before they must put up a shot
    private static final int SHOT_CLOCK_LENGTH_SECONDS = 24;
    // Blowout defines the point differential between two teams. If this differential is reached, the game is considered
    // all but over and the offense will adjust by running the clock down more
    private static final int BLOWOUT = 20;
    // The rate that random fouls occur (Non-shooting fouls only)
    private static final double FOUL_RATE = 0.04;
    private static final double STEAL_RATE = 0.05;
    private static final double PERIMETER_BLOCK_RATE = 0.10;
    private static final double INSIDE_BLOCK_RATE = 0.20;
    private static final double DEFENSIVE_REBOUND_RATE = 0.75; // There is a 75% chance a def rebound happens vs an off reb
    // Default value for how often turnovers occur
    private static final double TURNOVER_RATE = 0.08;
    private static final double ASSIST_RATE = 0.57;
    // For now we'll assume timeouts get called every 5 minutes
    private static final int TIMEOUT_INTERVAL = 300;

    /*
    Member variables
     */
    private int id; // unique id for this game
    private Team homeTeam; // the home team
    private Team awayTeam; // the away team
    // Store a map of each players stats for this game so that they can be recorded afterwards
    private Map<Player, Map<PlayerStat, Integer>> playerStats;
    // A map of each teams stats for this game
    private Map<Team, Map<TeamStat, Integer>> teamStats;
    private Team teamOnOffense; // used to signify which team is currently on offense
    // A map of each teams current players on court. Each team can have only 5 players on at any given time
    private Map<Team, List<Player>> playersOnCourt;
    // The simulation happens by running plays and determining how long they take in seconds. THe gameTime is
    // incremented after each play until it reaches the end of the game time (GAME_LENGTH_SECONDS)
    private int gameTime = 0;
    private List<String> gameLog = new LinkedList<>();


    public GameSimulation(Team home, Team away, Integer gid) {
        if (gid == null)
            // Set the unique ID of this game
            setId(League.getInstance().getNextUniqueKey());
        // Reset each teams players energy to 1.0
        home.resetEnergyLevels();
        away.resetEnergyLevels();
        // Mark the home and away teams
        setHomeTeam(home);
        setAwayTeam(away);
        // Initialize data structures for team stats, player stats and players on court
        setTeamStats(new HashMap<>());
        setPlayerStats(new HashMap<>());
        setPlayersOnCourt(new HashMap<>());
        getTeamStats().put(getHomeTeam(), Utils.createTeamStatIntMap());
        getTeamStats().put(getAwayTeam(), Utils.createTeamStatIntMap());
        for (Player p : getHomeTeam().getRoster()) {
            getPlayerStats().put(p, Utils.createPlayerStatIntMap());
        }
        for (Player p : getAwayTeam().getRoster()) {
            getPlayerStats().put(p, Utils.createPlayerStatIntMap());
        }
        // Place the best 5 players on the court at the start of the game
        setHomePlayersOnCourt(new ArrayList<>(getHomeTeam().getRankedRoster().subList(0, 5)));
        setAwayPlayersOnCourt(new ArrayList<>(getAwayTeam().getRankedRoster().subList(0, 5)));
    }

    /**
     * Get the unique identifier for this game
     *
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Set the unique identifier for this game
     *
     * @param id int
     */
    public void setId(int id) {
        this.id = id;
    }

    public List<String> getGameLog() {
        return gameLog;
    }

    public String getFullGameLog() {
        StringBuilder log = new StringBuilder();
        for (String event : getGameLog())
            log.append(String.format("%s\n", event));
        return log.toString();
    }

    public void reconstructGameLog(String fullGameLog) {
        this.gameLog = Arrays.asList(fullGameLog.split("\\r?\\n"));
    }

    private void log(String message) {
        gameLog.add(message);
    }

    /**
     * Increments the game clock by the amount represented in 'seconds'
     *
     * @param seconds int
     */
    private void incrementGameTime(int seconds) {
        gameTime += seconds;
    }


    public void setGameTime(int seconds) {
        gameTime = seconds;
    }

    /**
     * Get a map of the current players on the court for each team
     *
     * @return Map<Team, List < Player>>
     */
    public Map<Team, List<Player>> getPlayersOnCourt() {
        return playersOnCourt;
    }

    /**
     * Set the current list of players on the court for either team
     *
     * @param playersOnCourt Map<Team, List<Player>>
     */
    public void setPlayersOnCourt(Map<Team, List<Player>> playersOnCourt) {
        this.playersOnCourt = playersOnCourt;
    }

    /**
     * Get the players on the court currently for the home team
     *
     * @return List<Player>
     */
    private List<Player> getHomePlayersOnCourt() {
        return getPlayersOnCourt().get(getHomeTeam());
    }

    /**
     * Set the list of players currently on the court for the home team
     *
     * @param players List<Player>
     */
    private void setHomePlayersOnCourt(List<Player> players) {
        getPlayersOnCourt().put(getHomeTeam(), players);
    }

    /**
     * Get the players on the court currently for the away team
     *
     * @return List<Player>
     */
    private List<Player> getAwayPlayersOnCourt() {
        return getPlayersOnCourt().get(getAwayTeam());
    }

    /**
     * Set the list of players currently on the court for the away team
     *
     * @param players List<Player>
     */
    private void setAwayPlayersOnCourt(List<Player> players) {
        getPlayersOnCourt().put(getAwayTeam(), players);
    }

    /**
     * Returns the map of player stats accumulated during this game
     *
     * @return Map<Player, Map < PlayerStat, Integer>>
     */
    public Map<Player, Map<PlayerStat, Integer>> getPlayerStats() {
        return playerStats;
    }

    /**
     * Set the Player stats map
     *
     * @param playerStats Map<Player, Map < PlayerStat, Integer>>
     */
    public void setPlayerStats(Map<Player, Map<PlayerStat, Integer>> playerStats) {
        this.playerStats = playerStats;
    }

    /**
     * Returns the map of Team Stats accumulated during this game
     *
     * @return Map<Team, Map < TeamStat, Integer>>
     */
    public Map<Team, Map<TeamStat, Integer>> getTeamStats() {
        return teamStats;
    }

    /**
     * Set the map of team stats
     *
     * @param teamStats Map<Team, Map<TeamStat, Integer>>
     */
    public void setTeamStats(Map<Team, Map<TeamStat, Integer>> teamStats) {
        this.teamStats = teamStats;
    }

    /**
     * Returns the Home Team
     *
     * @return Team
     */
    public Team getHomeTeam() {
        return homeTeam;
    }

    /**
     * Set the Home team
     *
     * @param homeTeam Team
     */
    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    /**
     * Get the away team
     *
     * @return Team
     */
    public Team getAwayTeam() {
        return awayTeam;
    }

    /**
     * Set the away team
     *
     * @param awayTeam Team
     */
    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    /**
     * Return the team currently on offense
     *
     * @return Team
     */
    private Team getTeamOnOffense() {
        return teamOnOffense;
    }

    /**
     * Return the team currently on defense
     *
     * @return Team
     */
    private Team getTeamOnDefense() {
        return getTeamOnOffense() == getHomeTeam() ? getAwayTeam() : getHomeTeam();
    }

    /**
     * Return the Team Stats for this game for the Home Team
     *
     * @return Map<TeamStat, Integer>
     */
    public Map<TeamStat, Integer> getHomeTeamStats() {
        return getTeamStats().get(getHomeTeam());
    }

    /**
     * Return the Team Stats for this game for the Away Team
     *
     * @return Map<TeamStat, Integer>
     */
    public Map<TeamStat, Integer> getAwayTeamStats() {
        return getTeamStats().get(getAwayTeam());
    }

    /**
     * Return the value of a particular stat for the Home Team
     *
     * @param stat TeamStat
     * @return int
     */
    public int getHomeTeamStat(TeamStat stat) {
        return Objects.requireNonNull(getHomeTeamStats()).get(stat);
    }

    /**
     * Return the value of a particular stat for the Away Team
     *
     * @param stat TeamStat
     * @return int
     */
    public int getAwayTeamStat(TeamStat stat) {
        return Objects.requireNonNull(getAwayTeamStats()).get(stat);
    }

    /**
     * Returns the value of a particular stat for either the home team or the away team
     *
     * @param team Team
     * @param stat TeamStat
     * @return int
     */
    private Integer getTeamStat(Team team, TeamStat stat) {
        if (team == getHomeTeam()) {
            return getHomeTeamStat(stat);
        } else if (team == getAwayTeam()) {
            return getAwayTeamStat(stat);
        } else {
            System.err.println("UNKNOWN TEAM IN GAME: " + team.getName());
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set a stat value for the Home Team
     *
     * @param stat TeamStat
     * @param val  int
     */
    private void setHomeTeamStat(TeamStat stat, int val) {
        Objects.requireNonNull(getHomeTeamStats()).put(stat, val);
    }

    /**
     * Set a stat value for the Away Team
     *
     * @param stat TeamStat
     * @param val  int
     */
    private void setAwayTeamStat(TeamStat stat, int val) {
        Objects.requireNonNull(getAwayTeamStats()).put(stat, val);
    }

    /**
     * Set the value of a particular stat for either the home or the away team
     *
     * @param team Team
     * @param stat TeamStat
     * @param val  int
     */
    public void setTeamStat(Team team, TeamStat stat, int val) {
        if (team == getHomeTeam()) {
            setHomeTeamStat(stat, val);
        } else if (team == getAwayTeam()) {
            setAwayTeamStat(stat, val);
        } else {
            System.err.println("UNKNOWN TEAM IN GAME: " + team.getName());
        }
    }

    /**
     * Returns the map of player stats for a particular player
     *
     * @param player Player
     * @return Map<PlayerStat, Integer>
     */
    public Map<PlayerStat, Integer> getPlayerStats(Player player) {
        return getPlayerStats().get(player);
    }

    /**
     * Returns the exact stat value of a specified Stat for a particular player
     *
     * @param player Player
     * @param stat   PlayerStat
     * @return int
     */
    public int getPlayerStat(Player player, PlayerStat stat) {
        return Objects.requireNonNull(getPlayerStats(player)).get(stat);
    }

    /**
     * Set a player stat for a particular player
     *
     * @param player Player
     * @param stat   PlayerStat
     * @param val    int
     */
    public void setPlayerStat(Player player, PlayerStat stat, int val) {
        Objects.requireNonNull(getPlayerStats(player)).put(stat, val);
    }

    /**
     * Increments a player's stat value by a specified amount
     *
     * @param player Player
     * @param stat   PlayerStat
     * @param amount int
     */
    private void incrementPlayerStat(Player player, PlayerStat stat, int amount) {
        setPlayerStat(player, stat,
                getPlayerStat(player, stat) + amount);
    }

    /**
     * Increments a team's stat value for the specified stat by the specified amount
     *
     * @param team   Team
     * @param stat   TeamStat
     * @param amount int
     */
    private void incrementTeamStat(Team team, TeamStat stat, int amount) {
        setTeamStat(team, stat,
                getTeamStat(team, stat) + amount);
    }

    public int getGameStat(Entity entity, Object gameStat) {
        assert (gameStat instanceof TeamStat && entity instanceof Team)
                || (gameStat instanceof PlayerStat && entity instanceof Player);
        if (entity instanceof Team) {
            return getTeamStat((Team) entity, (TeamStat) gameStat);
        } else {
            return getPlayerStat((Player) entity, (PlayerStat) gameStat);
        }
    }

    /**
     * Swaps the possession by switching the teams on offense and defense
     */
    private void swapPossession() {
        teamOnOffense = getTeamOnDefense();
    }

    /**
     * Determines if the regulation period is over. Regulation ends when the gameTime
     * has either met or passed the GAME_LENGTH_SECONDS variable
     *
     * @return boolean
     */
    private boolean regulationIsOver() {
        return gameTime >= GAME_LENGTH_SECONDS;
    }

    /**
     * TODO: Returns true if overtime is finished
     */
    private boolean overtimeIsOver() {
        if (regulationIsOver() && !overtimeRequired())
            return true;

        return regulationIsOver();

    }

    /**
     * Returns true if the entire game (regulation + OT) is done
     *
     * @return boolean
     */
    public boolean gameIsOver() {
        return regulationIsOver();
    }

    /**
     * Determines whether overtime periods are needed. An overtime period is needed if the regulation period has
     * ended but the score is still tied
     *
     * @return boolean
     */
    private boolean overtimeRequired() {
        assert regulationIsOver();
        return getHomeTeamStat(TeamStat.TEAM_PTS) == getAwayTeamStat(TeamStat.TEAM_PTS);
    }

    /**
     * Returns the team that won the game
     *
     * @return Team
     */
    public Team getWinner() {
        assert regulationIsOver();
        return (getHomeTeamStat(TeamStat.TEAM_PTS) > getAwayTeamStat(TeamStat.TEAM_PTS)) ? getHomeTeam() : getAwayTeam();
    }

    /**
     * Returns the team that lost the game
     *
     * @return Team
     */
    public Team getLoser() {
        assert regulationIsOver();
        return (getWinner() == getHomeTeam()) ? getAwayTeam() : getHomeTeam();
    }

    private List<Player> getSortedPlayersOnCourtBasedOffAttribute(Team t, PlayerAttributes attr) {
        List<Player> sorted = new LinkedList<>();
        List<Player> playersOnCourt = (t == getHomeTeam()) ? getHomePlayersOnCourt() : getAwayPlayersOnCourt();
        for (Player p : t.getSortedRosterBasedOffPlayerAttributes(attr))
            if (playersOnCourt.contains(p))
                sorted.add(p);
        return sorted;
    }

    /**
     * This function gets called at the the end of each possession. It essentially checks whether a timeout and swap of
     * players on the court should happen. In the current implementation, the starting players on the court are subbed
     * out once their energy goes below 0.6. They are replaced with the next best player in that team's roster
     */
    private void modifyPlayersOnCourt() {
        // Energy threshold for subbing out
        double energyLimit = 0.6;
        // Keep lists of each player we need to sub out
        List<Player> homePlayersToRemove = new ArrayList<>();
        List<Player> awayPlayersToRemove = new ArrayList<>();

        /*
        Iterate over both the home and away teams current players on the court. Mark any players who fall under the
        energy threshold as players that need to be replaced
         */
        for (Player p : getHomePlayersOnCourt())
            if (p.getPlayerEnergy() <= energyLimit)
                homePlayersToRemove.add(p);
        for (Player p : getAwayPlayersOnCourt())
            if (p.getPlayerEnergy() <= energyLimit)
                awayPlayersToRemove.add(p);

        /*
        Remove the players from the court that have low energy
         */
        for (Player p : homePlayersToRemove)
            getHomePlayersOnCourt().remove(p);
        for (Player p : awayPlayersToRemove)
            getAwayPlayersOnCourt().remove(p);

        /*
        For both the home and away, populate the list of players on the court with new players that have full energy.
        This effectively counts as a player substitution
         */
        for (Player p : getHomeTeam().getRankedRoster()) {
            if (getHomePlayersOnCourt().size() == 5)
                break;
            if (!getHomePlayersOnCourt().contains(p) &&
                    getPlayerStat(p, PlayerStat.FOULS) < FOUL_LIMIT &&
                    p.getPlayerEnergy() == 1.0)
                getHomePlayersOnCourt().add(p);
        }
        for (Player p : getAwayTeam().getRankedRoster()) {
            if (getAwayPlayersOnCourt().size() == 5)
                break;
            if (!getAwayPlayersOnCourt().contains(p) &&
                    getPlayerStat(p, PlayerStat.FOULS) < FOUL_LIMIT &&
                    p.getPlayerEnergy() == 1.0)
                getAwayPlayersOnCourt().add(p);
        }

        assert getHomePlayersOnCourt().size() == 5 &&
                getAwayPlayersOnCourt().size() == 5;
    }

    /**
     * After each play, the players on the court experience an energy decay at a rate of playLength/1000.
     * When a players energy goes below 0.6, they will be subbed out. Players not on the court experience an energy boost
     * at the same rate, unless they already have full energy (1.0).
     * <p>
     * For example, if a play takes 20 seconds, the energy decay amount would be (20/1000) = 0.02. Therefore, everyone who
     * is currently on the court loses 0.02 energy, and at the same time, everyone on the bench experiences an energy
     * boost of 0.02, unless they already have full energy.
     */
    private void scaleEnergyForPlayers(int playLength) {
        // Energy amount to scale by
        double amount = Utils.round(playLength / 1000.0, 4);
        for (Player p : getHomeTeam().getRoster())
            if (getHomePlayersOnCourt().contains(p))
                p.setPlayerEnergy(p.getPlayerEnergy() - amount);
            else
                p.setPlayerEnergy(p.getPlayerEnergy() + amount);
        for (Player p : getAwayTeam().getRoster())
            if (getAwayPlayersOnCourt().contains(p))
                p.setPlayerEnergy(p.getPlayerEnergy() - amount);
            else
                p.setPlayerEnergy(p.getPlayerEnergy() + amount);


    }

    /**
     * Determine who wins a tip off randomly. In the current implementation, each team has equal odds of winning the
     * tipoff. In future iterations, this should probably take into consideration the starting centers height
     */
    private void determineRandomTipOffWinner() {
        teamOnOffense = (League.getInstance().getRandomDouble(0.0, 1.0) > 0.5)
                ? getHomeTeam() : getAwayTeam();
    }

    /**
     * Randomly determine how long a possession will take. I set the lower bound to 4 since it's hard to get a play
     * to happen quicker than that. The upper limit is the length of the shot clock (24 seconds).
     *
     * @return int
     */
    private int determinePossessionTime() {
        return League.getInstance().getRandomInteger(4, SHOT_CLOCK_LENGTH_SECONDS);
    }

    /**
     * Randomly determine if a turnover will occur this possession. The calculation is as follows:
     * Take the teams average turnovers per game and divide it by the length of the game in minutes. Compare this value
     * to a random double to see if it will occur.
     * <p>
     * Should the team not have any historical stat values for turnovers, then a league default turnover rate is used
     * in lieu (set to 0.8, meaning there is a 8% chance of a turnover)
     *
     * @return boolean
     */
    private boolean determineIfTurnover() {
        double turnoverProb = (Integer) getTeamOnOffense().getStatContainer().getAvgValueOfStat(TeamStat.TEAM_TOV)
                / GAME_LENGTH_MIN;
        turnoverProb = (turnoverProb != 0) ? turnoverProb : TURNOVER_RATE;
        return turnoverProb >= League.getInstance().getRandomDouble(0.0, 1.0);
    }

    /**
     * Used when determineIfTurnover() returns true. Will increment both team and player stats marking that a turnover
     * was committed.
     */
    private void simulateTurnover() {
        // Turnover occurred
        // Pick a player who committed the turnover randomly
        int i = League.getInstance().getRandomInteger(0, 4);
        Player turoverPlayer = (getTeamOnOffense() == getHomeTeam()) ?
                getHomePlayersOnCourt().get(i) : getAwayPlayersOnCourt().get(i);
        // Increment the player and team tov stats
        incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_TOV, 1);
        incrementPlayerStat(turoverPlayer, PlayerStat.TOV, 1);

    }

    /**
     * Randomly determine if a foul will happen. This function is used before simulateShot, meaning it only simulates
     * non-shooting fouls
     * The possibility of a non-shooting foul occurring is currently set to FOUL_RATE (0.05) meaning there is a 5%
     * chance of a foul occurring
     *
     * @return boolean
     */
    private boolean determineIfFoul() {
        // A foul occurs if a random double is below the foul rate threshold
        return FOUL_RATE >= League.getInstance().getRandomDouble(0.0, 1.0);
    }

    /**
     * Simulates a free throw event. This function does not determine if a foul occured, it should only be called
     * after that determination has happened. If 'fouledPlayer' is null, the simulation will randomly pick a player that
     * was fouled.
     * <p>
     * A random player is picked as the fouling player.
     *
     * @param foulingTeam  Team: The team that committed the fouled
     * @param numShots     int: the number of free throws to take
     * @param fouledPlayer Player: The player who was fouled. Can be null
     */
    private void simulateFreeThrows(Team foulingTeam, int numShots, Player fouledPlayer) {

        // Pick a player who committed the foul randomly.
        int index = League.getInstance().getRandomInteger(0, 4);
        Player foulingPlayer = (foulingTeam == getHomeTeam()) ?
                getHomePlayersOnCourt().get(index) : getAwayPlayersOnCourt().get(index);
        // Increment team and player foul stats
        incrementTeamStat(foulingTeam, TeamStat.TEAM_FOULS, 1);
        incrementPlayerStat(foulingPlayer, PlayerStat.FOULS, 1);
        // If the fouling team was defense, then the offense shoots free throws. Else the possession will just change.
        if (foulingTeam == getTeamOnDefense()) {
            // Pick a player to shoot the foul shots if not set
            if (fouledPlayer == null) {
                index = League.getInstance().getRandomInteger(0, 4);
                fouledPlayer = (foulingTeam == getHomeTeam()) ?
                        getAwayPlayersOnCourt().get(index) : getHomePlayersOnCourt().get(index);
            }
            System.out.println(foulingPlayer.getName() + " from " + foulingTeam.getName()
                    + " has committed a foul on " + fouledPlayer.getName());
            log(String.format("%s from %s has committed a foul on %s", foulingPlayer.getName(),
                    foulingTeam.getName(), fouledPlayer.getName()));
            // Simulate each free throw taking into consideration the players free throw rating
            for (int i = 0; i < numShots; i++) {
                // A free-throw is made if the fouledPlayers free throw attribute is higher than the random number
                // that is generated
                if (League.getInstance().getRandomDouble(0.0, 1.0) <=
                        fouledPlayer.getPlayerAttribute(PlayerAttributes.FREE_THROW)) {
                    // Free throw made! Increment stats as needed
                    System.out.println(fouledPlayer.getName() + " has made one free-throw");
                    log(String.format("%s has made a free throw", fouledPlayer.getName()));
                    incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_PTS, 1);
                    incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_FREE_THROW_MADE, 1);
                    incrementPlayerStat(fouledPlayer, PlayerStat.FREE_THROW_MADE, 1);
                    incrementPlayerStat(fouledPlayer, PlayerStat.PTS, 1);
                }
                // Increment free throws attempted stats
                incrementPlayerStat(fouledPlayer, PlayerStat.FREE_THROW_ATTEMPTS, 1);
                incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_FREE_THROW_ATTEMPTS, 1);
            }
        }
    }

    /**
     * Simulates a three point shot
     *
     * @param shooter Player: the player shooting the 3 pointer
     */
    private void simulateThreePointer(Player shooter) {
        // Three point attempt, increment stats
        incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_THREE_POINT_ATTEMPTS, 1);
        incrementPlayerStat(shooter, PlayerStat.THREE_POINT_ATTEMPTS, 1);
        // Check if the shot was made based of thee players three pt attr. There is also an opportunity that the shot
        // is blocked before the shot goes up
        boolean shotBlocked = simulateBlock(true, shooter);
        if (League.getInstance().getRandomDouble(0.0, 1.0)
                <= (shooter.getPlayerAttribute(PlayerAttributes.THREE_P_SCORING) * 0.5) && !shotBlocked) {
            simulateAssist();
            // Three point shot made! Increment stats as needed
            System.out.println(shooter.getName() + " has made a three point shot");
            log(String.format("%s has made a three point shot", shooter.getName()));
            incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_THREE_POINT_MADE, 1);
            incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_PTS, 3);
            incrementPlayerStat(shooter, PlayerStat.THREE_POINT_MADE, 1);
            incrementPlayerStat(shooter, PlayerStat.PTS, 3);
        } else {
            System.out.println(shooter.getName() + " has missed a 3 point shot");
            log(String.format("%s has missed a three point shot", shooter.getName()));
            // Check to see if the offense grabbed a rebound and can have a new possession
            boolean offensiveRebound = simulateRebound();
            if (offensiveRebound)
                simulateShot();
        }
    }

    /**
     * Utility function to record a two pointer being made. Used in simulateTwoPointer. Will increment stats
     * accordingly and also force free throws if there was an and-one
     *
     * @param shooter Player: the player shooting the ball
     * @param andOne  boolean: Whether the player was fouled in the act of shooting
     */
    private void recordMadeTwoPointer(Player shooter, boolean andOne) {
        // Increment stats
        incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_TWO_POINT_MADE, 1);
        incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_PTS, 2);
        incrementPlayerStat(shooter, PlayerStat.TWO_POINT_MADE, 1);
        incrementPlayerStat(shooter, PlayerStat.PTS, 2);
        if (andOne) {
            // If the player was fouled in the act of shooting, simulate a free throw
            System.out.println(shooter.getName() + " has made a two-point shot with an and-one");
            log(String.format("%s has made a two-point shot with an and-one", shooter.getName()));
            simulateFreeThrows(getTeamOnDefense(), 1, shooter);
        } else {
            System.out.println(shooter.getName() + " has made a two-point shot");
            log(String.format("%s has made a two-point shot", shooter.getName()));
        }
    }

    /**
     * Function to simulate two pointers. Two pointers can either be a mid-range shot or a shot in the post. The shooter
     * will pick the one they are better at (I.e. whichever attribute is higher). If it is a inside shot, it can either
     * be a dunk or a layup. Again, this determination is made based off the players attributes and what they are better at
     *
     * @param shooter Player
     */
    private void simulateTwoPointer(Player shooter) {
        // First determine if this will be a mid-range shot or a shot inside the post. To determine this, we look
        // at the players MID_RANGE_SHOOTING and INSIDE_SCORING attributes.
        double insideScoringAttr = shooter.getPlayerAttribute(PlayerAttributes.INSIDE_SCORING);
        double midRangeShotAttr = shooter.getPlayerAttribute(PlayerAttributes.MID_SCORING);
        // Increment attempts stat
        incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_TWO_POINT_ATTEMPTS, 1);
        incrementPlayerStat(shooter, PlayerStat.TWO_POINT_ATTEMPTS, 1);
        if (midRangeShotAttr > insideScoringAttr) {
            // Player will take a mid-range jump shot
            double probabilityFoul = 0.08; // A mid range shot has an 8% chance of being fouled
            double outcome = League.getInstance().getRandomDouble(0.0, 1.0);
            // First check if player was fouled without making a shot
            if (outcome <= probabilityFoul) {
                simulateFreeThrows(getTeamOnDefense(), 2, shooter);
                return;
            }
            // Next check if the shot was made. Also check to see if a block happens
            boolean blocked = simulateBlock(true, shooter);
            if (outcome <= midRangeShotAttr && !blocked) {
                // check to see if the shot is assisted
                simulateAssist();
                // Player made the shot!
                // Check to see if an and-one orobability is 5% for mid range shots
                boolean probabilityAndOne = (League.getInstance().getRandomDouble(0.0, 1.0)) <= 0.05;
                recordMadeTwoPointer(shooter, probabilityAndOne);
            } else {
                boolean offensiveRebound = simulateRebound();
                if (offensiveRebound)
                    simulateShot();
            }
        } else {
            // if it was not a shot, then it was either a layup (inside_scoring) or dunk.
            double dunkAttribute = shooter.getPlayerAttribute(PlayerAttributes.DUNK);
            if (dunkAttribute > insideScoringAttr) {
                // Dunk
                double probabilityFoul = 0.2; // Probability of foul on dunk is 40%
                double outcome = League.getInstance().getRandomDouble(0.0, 1.0);
                if (outcome <= probabilityFoul) {
                    simulateFreeThrows(getTeamOnDefense(), 2, shooter);
                    return;
                }
                boolean blocked = simulateBlock(false, shooter);
                if (outcome <= dunkAttribute && !blocked) {
                    simulateAssist();
                    // Player made the dunk!
                    // Check to see if an and-one, probability is 25% on dunks
                    boolean probabilityAndOne = (League.getInstance().getRandomDouble(0.0, 1.0)) <= 0.25;
                    recordMadeTwoPointer(shooter, probabilityAndOne);
                } else {
                    if (simulateRebound())
                        simulateShot();
                }
            } else {
                // Normal layup
                double probabilityFoul = 0.15;
                double outcome = League.getInstance().getRandomDouble(0.0, 1.0);
                if (outcome <= probabilityFoul) {
                    simulateFreeThrows(getTeamOnDefense(), 2, shooter);
                    return;
                }
                boolean blocked = simulateBlock(false, shooter);
                if (outcome <= insideScoringAttr && !blocked) {
                    simulateAssist();
                    // Player made the layup!
                    // Check to see if an and-one
                    boolean probabilityAndOne = (League.getInstance().getRandomDouble(0.0, 1.0)) <= 0.15;
                    recordMadeTwoPointer(shooter, probabilityAndOne);
                } else {
                    if (simulateRebound())
                        simulateShot();
                }
            }
        }
    }


    /**
     * Simulate a shot during a team's possession. First a random player is chosen as the shooter. If that shooter has
     * an above-average 3pt-shot then they will take a 3 pointer. Else they will take a two-pointer.
     */
    private void simulateShot() {
        // First pick a player to be the shooting player
        int i = League.getInstance().getRandomInteger(0, 4);
        Player shooter = (getTeamOnOffense() == getHomeTeam())
                ? getHomePlayersOnCourt().get(i) : getAwayPlayersOnCourt().get(i);
        // Check to see if the shooter has the ball stolen
        if (simulateSteal(shooter))
            return; // No shot happens if the ball is stolen

        // First we check to see if a 3 pointer can occur
        double playerThreePtPercent;
        double teamThreePtPercent;
        try {
            playerThreePtPercent = shooter.getSumOfPlayerStat(PlayerStat.THREE_POINT_MADE) /
                    shooter.getSumOfPlayerStat(PlayerStat.THREE_POINT_ATTEMPTS);
            teamThreePtPercent = getTeamOnOffense().getSumOfTeamStat(TeamStat.TEAM_THREE_POINT_MADE) /
                    getTeamOnOffense().getSumOfTeamStat(TeamStat.TEAM_THREE_POINT_ATTEMPTS);
        } catch (ArithmeticException e) {
            playerThreePtPercent = 0;
            teamThreePtPercent = 0;
        }
        double threePtCutoffPoint = (playerThreePtPercent == 0 || teamThreePtPercent == 0) ?
                0 : ((playerThreePtPercent + teamThreePtPercent) / 0.2) * 0.3;
        if (shooter.getPlayerAttribute(PlayerAttributes.THREE_P_SCORING) > 0.85) {
            if (threePtCutoffPoint != 0 &&
                    League.getInstance().getRandomDouble(0.0, 1.0) <= threePtCutoffPoint) {
                simulateThreePointer(shooter);
            } else {
                simulateThreePointer(shooter);
            }
        } else {
            // Two point shot
            simulateTwoPointer(shooter);
        }
    }

    /**
     * Simulate a block
     */
    private boolean simulateBlock(boolean jumpShot, Player shooter) {
        double decisionPoint = League.getInstance().getRandomDouble(0.0, 1.0);
        double cutoffPoint = League.getInstance().getRandomDouble(0.0, 1.0);
        boolean decision;
        int i = League.getInstance().getRandomInteger(0, 4);
        if (jumpShot) {
            decision = decisionPoint <= PERIMETER_BLOCK_RATE;
            if (decision) {
                Player blockingPlayer = (getTeamOnOffense() == getHomeTeam()) ?
                        getAwayPlayersOnCourt().get(i) : getHomePlayersOnCourt().get(i);
                if (blockingPlayer.getPlayerAttribute(PlayerAttributes.PERIMETER_DEFENSE) >= cutoffPoint) {
                    log(String.format("%s has blocked a jump shot from %s", blockingPlayer.getName(), shooter.getName()));
                    incrementTeamStat(getTeamOnDefense(), TeamStat.TEAM_BLK, 1);
                    incrementPlayerStat(blockingPlayer, PlayerStat.BLK, 1);
                    return true;
                }
            }
        } else {
            decision = decisionPoint <= INSIDE_BLOCK_RATE;
            if (decision) {
                Player blockingPlayer = (getTeamOnOffense() == getHomeTeam()) ?
                        getAwayPlayersOnCourt().get(i) : getHomePlayersOnCourt().get(i);
                if (blockingPlayer.getPlayerAttribute(PlayerAttributes.INSIDE_DEFENSE) >= cutoffPoint) {
                    log(String.format("%s has blocked a inside shot from %s", blockingPlayer.getName(), shooter.getName()));
                    incrementTeamStat(getTeamOnDefense(), TeamStat.TEAM_BLK, 1);
                    incrementPlayerStat(blockingPlayer, PlayerStat.BLK, 1);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Simulate a steal
     */
    private boolean simulateSteal(Player shooter) {
        // First check to see if a steal occurs
        if (League.getInstance().getRandomDouble(0.0, 1.0) <= STEAL_RATE) {
            //Steal will (maybe) occur
            // Pick a random player to be the stealer
            int i = League.getInstance().getRandomInteger(0, 4);
            Player stealer = (getTeamOnOffense() == getHomeTeam()) ? getAwayPlayersOnCourt().get(i) :
                    getHomePlayersOnCourt().get(i);
            if (stealer.getPlayerAttribute(PlayerAttributes.PERIMETER_DEFENSE) >= League.getInstance().getRandomDouble(0.0, 1.0)) {
                log(String.format("%s has stolen the ball from %s", stealer.getName(), shooter.getName()));
                incrementTeamStat(getTeamOnDefense(), TeamStat.TEAM_STL, 1);
                incrementPlayerStat(stealer, PlayerStat.STL, 1);
                incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_TOV, 1);
                incrementPlayerStat(shooter, PlayerStat.TOV, 1);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if this shot attempt will come off of an assist. Currently there is a 57% chance that any shot that
     * is going in comes off an assist. The teams best assisters have the higher chance of making the assist.
     */
    private void simulateAssist() {
        boolean assistHappens = League.getInstance().getRandomDouble(0.0, 1.0) <= ASSIST_RATE;
        if (!assistHappens)
            return;
        int i = League.getInstance().getRandomInteger(1, 15);
        List<Player> players = getSortedPlayersOnCourtBasedOffAttribute(getTeamOnOffense(), PlayerAttributes.ASSIST);
        Player assister;
        if (i <= 5) {
            // Tallest player
            assister = players.get(0);
        } else if (6 <= i && i <= 9) {
            // second
            assister = players.get(1);
        } else if (10 <= i && i <= 12) {
            // third
            assister = players.get(2);
        } else if (i == 13 || i == 14) {
            // fourth
            assister = players.get(3);
        } else {
            // fifth
            assister = players.get(4);
        }
        // Increment stats
        incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_ASSIST, 1);
        incrementPlayerStat(assister, PlayerStat.ASSIST, 1);
    }

    /**
     * Simulate a rebound. Returns true if the offensive team gets the ball back
     *
     * @return boolean
     */
    private boolean simulateRebound() {
        // Check if it is offensive or defensive rebound. There is much higher chance for a defensive rebound
        boolean defensiveRebound = League.getInstance().getRandomDouble(0.0, 1.0) <= DEFENSIVE_REBOUND_RATE;
        // Pick a player to shoot. Taller players have better chance
        int i = League.getInstance().getRandomInteger(1, 15);
        // TODO factor in ORB and DRB attributes here
        List<Player> players = (defensiveRebound) ?
                getSortedPlayersOnCourtBasedOffAttribute(getTeamOnDefense(), PlayerAttributes.HEIGHT) :
                getSortedPlayersOnCourtBasedOffAttribute(getTeamOnOffense(), PlayerAttributes.HEIGHT);
        Player rebounder;
        // Pick a player to rebound.
        if (i <= 5) {
            // Tallest player
            rebounder = players.get(0);
        } else if (6 <= i && i <= 9) {
            // second
            rebounder = players.get(1);
        } else if (10 <= i && i <= 12) {
            // third
            rebounder = players.get(2);
        } else if (i == 13 || i == 14) {
            // fourth
            rebounder = players.get(3);
        } else {
            // fifth
            rebounder = players.get(4);
        }
        // Increment stats
        if (defensiveRebound) {
            // Mark stats for rebound
            incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_DRB, 1);
            incrementPlayerStat(rebounder, PlayerStat.DRB, 1);
            // log
            log(String.format("%s has grabbed a defensive rebound", rebounder.getName()));
            return false;
        } else {
            // Mark stats for rebound
            incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_ORB, 1);
            incrementPlayerStat(rebounder, PlayerStat.ORB, 1);
            // log
            log(String.format("%s has grabbed an offensive rebound", rebounder.getName()));
            return true;
        }
    }

    /**
     * Simulate an entire possession. There are various things that can happen:
     * 1. If at the end of a game and the score is a blowout the winning team runs out the clock
     * 2. A turnover may occur
     * 3. A foul may occur
     * 4. If none of those occur, then the offensive team will attempt to take a shot
     *
     * @return int: the length of time this play took
     */
    private int simPlay() {
        int playLength = determinePossessionTime();
        int pointDiff = getHomeTeamStat(TeamStat.TEAM_PTS) - getAwayTeamStat(TeamStat.TEAM_PTS);
        // If there is a blowout for either team and the game is almost over, then  just run out the clock without a
        // a play and return the play length
        if (((gameTime + playLength) >= GAME_LENGTH_SECONDS)
                && ((teamOnOffense == getHomeTeam() && pointDiff >= BLOWOUT) ||
                (teamOnOffense == getAwayTeam() && pointDiff <= -BLOWOUT)))
            return playLength;

        // Check if the offense commits a turnover before a shot attempt
        if (determineIfTurnover()) {
            simulateTurnover();
            return playLength;
        }

        // Next thing that could occur before a shot is a non-shooting foul by either team
        if (determineIfFoul()) {
            // determine which team committed the foul. There is a 75 % chance the defending team commits the foul
            Team foulingTeam = (League.getInstance().getRandomDouble(0.0, 1.0) >= 0.25)
                    ? getTeamOnOffense() : getTeamOnDefense();
            simulateFreeThrows(foulingTeam, 2, null);
            return playLength;
        }


        // If no foul, then just simulate a (potential) shot
        simulateShot();
        return playLength;
    }

    /**
     * Simulates the regulation period of the game
     */
    private void simRegulation() {
        // First determine who wins tipoff
        determineRandomTipOffWinner();
        // Run a continual loop until the game ends
        while (!regulationIsOver()) {
            // Simulate a play and move the clock up by how long the play took
            int playLength = simPlay();
            incrementGameTime(playLength);
            // Scale energy for players
            scaleEnergyForPlayers(playLength);
            // swap possession for next play
            swapPossession();
            // See if a timeout or subs should happen now
            modifyPlayersOnCourt();
        }

        // Todo: Implement overtime
        // Check if overtime is needed
        // if (overtimeRequired()) {
        //
        // }
        endGame();
    }

    /**
     * Print the final score and box score at the end of the game
     */
    public void endGame() {
        Team winner = getWinner();
        Team loser = getLoser();
        System.out.println(winner.getName() + " has defeated " + loser.getName() + " at a final score of " +
                getTeamStat(winner, TeamStat.TEAM_PTS) + " to " + getTeamStat(loser, TeamStat.TEAM_PTS));
        System.out.println("### BOX SCORE ###\n");
        System.out.println(getHomeTeam().getName() + " Stats");
        for (Map.Entry<TeamStat, Integer> entry : getHomeTeamStats().entrySet())
            System.out.println(entry.getKey() + " = " + entry.getValue());
        System.out.println("\n" + getAwayTeam().getName() + " Stats");
        for (Map.Entry<TeamStat, Integer> entry : getAwayTeamStats().entrySet())
            System.out.println(entry.getKey() + " = " + entry.getValue());
        System.out.println("\n" + getHomeTeam().getName() + " Player Stats");
        for (Player p : getHomeTeam().getRoster()) {
            System.out.println(p.getName());
            for (Map.Entry<PlayerStat, Integer> entry : getPlayerStats(p).entrySet())
                System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        System.out.println("\n" + getAwayTeam().getName() + " Player Stats");
        for (Player p : getAwayTeam().getRoster()) {
            System.out.println(p.getName());
            for (Map.Entry<PlayerStat, Integer> entry : getPlayerStats(p).entrySet())
                System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

    /**
     * Simulate the game, including any overtimes
     *
     * @return Team: The winning team
     */
    public Team simulateGame() {
        simRegulation();
        return getWinner();
    }
}
