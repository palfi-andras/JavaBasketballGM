package core;

import attributes.GameAttributes;
import attributes.PlayerAttributes;
import attributes.PlayerStatTypes;
import attributes.TeamStatTypes;
import utilities.CoreConfiguration;
import utilities.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
public class GameSimulation extends AbstractEntity {
    // The amount of fouls a player can get before they foul out of the game
    private static final int FOUL_LIMIT = CoreConfiguration.getInstance().getIntProperty("simulation.foul_limit");
    // THe length in minutes of each quarter
    private static final int GAME_LENGTH_MIN_PER_QUARTER = CoreConfiguration.getInstance().
            getIntProperty("simulation.game_length_min_per_quarter");
    // The length in minutes of the entire game
    private static final int GAME_LENGTH_MIN = GAME_LENGTH_MIN_PER_QUARTER * 4;
    // The length in seconds of the entire game
    private static final int GAME_LENGTH_SECONDS = GAME_LENGTH_MIN * 60;
    // The amount of time the offense has before they must put up a shot
    private static final int SHOT_CLOCK_LENGTH_SECONDS = CoreConfiguration.getInstance().
            getIntProperty("simulation.shot_clock_seconds");
    // Blowout defines the point differential between two teams. If this differential is reached, the game is considered
    // all but over and the offense will adjust by running the clock down more
    private static final int BLOWOUT = CoreConfiguration.getInstance().
            getIntProperty("simulation.blowout_point_threshold");
    // The rate that random fouls occur (Non-shooting fouls only)
    private static final double FOUL_RATE = CoreConfiguration.getInstance().getDoubleProperty("simulation.foul_rate");
    private static final double STEAL_RATE = CoreConfiguration.getInstance().getDoubleProperty("simulation.steal_rate");
    private static final double PERIMETER_BLOCK_RATE = CoreConfiguration.getInstance().
            getDoubleProperty("simulation.perimeter_block_rate");
    private static final double INSIDE_BLOCK_RATE = CoreConfiguration.getInstance().
            getDoubleProperty("simulation.inside_block_rate");
    private static final double DEFENSIVE_REBOUND_RATE = CoreConfiguration.getInstance().
            getDoubleProperty("simulation.defensive_rebound_rate");
    private static final double TURNOVER_RATE = CoreConfiguration.getInstance().
            getDoubleProperty("simulation.turnover_rate");
    private static final double ASSIST_RATE = CoreConfiguration.getInstance().
            getDoubleProperty("simulation.assist_rate");

    /*
    Member variables
     */
    private int id; // unique id for this game
    private Team homeTeam; // the home team
    private Team awayTeam; // the away team
    private Team teamOnOffense; // used to signify which team is currently on offense
    // A map of each teams current players on court. Each team can have only 5 players on at any given time
    private Map<Team, List<Player>> playersOnCourt;


    public GameSimulation(Team home, Team away, int gid) throws SQLException {
        super(gid, String.format("%s vs %s", home.getName(), away.getName()), "gid", "games");
        // Reset each teams players energy to full energy
        home.resetEnergyLevels();
        away.resetEnergyLevels();
        // Mark the home and away teams
        setHomeTeam(home);
        setAwayTeam(away);

        setPlayersOnCourt(new HashMap<>());
        // Place the best 5 players on the court at the start of the game
        setHomePlayersOnCourt(new ArrayList<>(getHomeTeam().getRankedRoster().subList(0, 5)));
        setAwayPlayersOnCourt(new ArrayList<>(getAwayTeam().getRankedRoster().subList(0, 5)));
    }

    @Override
    public void initializeAttributes() {
        setEntityAttribute(GameAttributes.GAME_CLOCK.toString(), 0);
        setEntityAttribute(GameAttributes.HOME_TEAM.toString(), homeTeam.getID());
        setEntityAttribute(GameAttributes.AWAY_TEAM.toString(), awayTeam.getID());
        setEntityAttribute(GameAttributes.GAME_LOG.toString(), new LinkedList<>());
    }


    public List<String> getGameLog() {
        return (List<String>) getEntityAttribute(GameAttributes.GAME_LOG.toString());
    }


    private void log(String message) {
        getGameLog().add(message);
    }

    /**
     * Increments the game clock by the amount represented in 'seconds'
     *
     * @param seconds int
     */
    private void incrementGameTime(int seconds) {
        setEntityAttribute(GameAttributes.GAME_CLOCK.toString(),
                getGameTime() + seconds);
    }

    private int getGameTime() {
        return (int) getEntityAttribute(GameAttributes.GAME_CLOCK.toString());
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
     * @return Map<String, Integer>
     */
    public TeamStat getHomeTeamStats() {
        return getHomeTeam().getTeamStat(getID());
    }

    /**
     * Return the Team Stats for this game for the Away Team
     *
     * @return Map<TeamStat, Integer>
     */
    public TeamStat getAwayTeamStats() {
        return getAwayTeam().getTeamStat(getID());
    }

    /**
     * Return the value of a particular stat for the Home Team
     *
     * @param stat TeamStat
     * @return int
     */
    public int getHomeTeamStat(TeamStatTypes stat) {
        return (int) getHomeTeamStats().getEntityAttribute(stat.toString());
    }

    /**
     * Return the value of a particular stat for the Away Team
     *
     * @param stat TeamStat
     * @return int
     */
    public int getAwayTeamStat(TeamStatTypes stat) {
        return (int) getAwayTeamStats().getEntityAttribute(stat.toString());
    }

    /**
     * Returns the value of a particular stat for either the home team or the away team
     *
     * @param team Team
     * @param stat TeamStat
     * @return int
     */
    private Integer getTeamStat(Team team, TeamStatTypes stat) {
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
    private void setHomeTeamStat(TeamStatTypes stat, int val) {
        getHomeTeamStats().setEntityAttribute(stat.toString(), val);
    }

    /**
     * Set a stat value for the Away Team
     *
     * @param stat TeamStat
     * @param val  int
     */
    private void setAwayTeamStat(TeamStatTypes stat, int val) {
        getHomeTeamStats().setEntityAttribute(stat.toString(), val);
    }

    /**
     * Set the value of a particular stat for either the home or the away team
     *
     * @param team Team
     * @param stat TeamStat
     * @param val  int
     */
    public void setTeamStat(Team team, TeamStatTypes stat, int val) {
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
    public PlayerStat getPlayerStats(Player player) {
        return player.getPlayerStat(getID());
    }

    /**
     * Returns the exact stat value of a specified Stat for a particular player
     *
     * @param player Player
     * @param stat   PlayerStat
     * @return int
     */
    public int getPlayerStat(Player player, PlayerStatTypes stat) {
        return (int) getPlayerStats(player).getEntityAttribute(stat.toString());
    }

    /**
     * Set a player stat for a particular player
     *
     * @param player Player
     * @param stat   PlayerStat
     * @param val    int
     */
    public void setPlayerStat(Player player, PlayerStatTypes stat, int val) {
        getPlayerStats(player).setEntityAttribute(stat.toString(), val);
    }

    /**
     * Increments a player's stat value by a specified amount
     *
     * @param player Player
     * @param stat   PlayerStat
     * @param amount int
     */
    private void incrementPlayerStat(Player player, PlayerStatTypes stat, int amount) {
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
    private void incrementTeamStat(Team team, TeamStatTypes stat, int amount) {
        setTeamStat(team, stat,
                getTeamStat(team, stat) + amount);
    }

    public int getGameStat(Entity entity, Object gameStat) {
        assert (gameStat instanceof TeamStatTypes && entity instanceof Team)
                || (gameStat instanceof PlayerStatTypes && entity instanceof Player);
        if (entity instanceof Team) {
            return getTeamStat((Team) entity, (TeamStatTypes) gameStat);
        } else {
            return getPlayerStat((Player) entity, (PlayerStatTypes) gameStat);
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
        return getGameTime() >= GAME_LENGTH_SECONDS;
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
        return getHomeTeamStat(TeamStatTypes.TEAM_PTS) == getAwayTeamStat(TeamStatTypes.TEAM_PTS);
    }

    /**
     * Returns the team that won the game
     *
     * @return Team
     */
    public Team getWinner() {
        assert regulationIsOver();
        return (getHomeTeamStat(TeamStatTypes.TEAM_PTS) > getAwayTeamStat(TeamStatTypes.TEAM_PTS)) ? getHomeTeam() : getAwayTeam();
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
                    getPlayerStat(p, PlayerStatTypes.FOULS) < FOUL_LIMIT &&
                    p.getPlayerEnergy() == 1.0)
                getHomePlayersOnCourt().add(p);
        }
        for (Player p : getAwayTeam().getRankedRoster()) {
            if (getAwayPlayersOnCourt().size() == 5)
                break;
            if (!getAwayPlayersOnCourt().contains(p) &&
                    getPlayerStat(p, PlayerStatTypes.FOULS) < FOUL_LIMIT &&
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
        teamOnOffense = (Utils.getRandomDouble(0.0, 1.0) > 0.5)
                ? getHomeTeam() : getAwayTeam();
    }

    /**
     * Randomly determine how long a possession will take. I set the lower bound to 4 since it's hard to get a play
     * to happen quicker than that. The upper limit is the length of the shot clock (24 seconds).
     *
     * @return int
     */
    private int determinePossessionTime() {
        return Utils.getRandomInteger(4, SHOT_CLOCK_LENGTH_SECONDS);
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
        double turnoverProb = getTeamOnOffense().getAvgValueOfTeamStat(TeamStatTypes.TEAM_TOV)
                / GAME_LENGTH_MIN;
        turnoverProb = (turnoverProb != 0) ? turnoverProb : TURNOVER_RATE;
        return turnoverProb >= Utils.getRandomDouble(0.0, 1.0);
    }

    /**
     * Used when determineIfTurnover() returns true. Will increment both team and player stats marking that a turnover
     * was committed.
     */
    private void simulateTurnover() {
        // Turnover occurred
        // Pick a player who committed the turnover randomly
        int i = Utils.getRandomInteger(0, 4);
        Player turoverPlayer = (getTeamOnOffense() == getHomeTeam()) ?
                getHomePlayersOnCourt().get(i) : getAwayPlayersOnCourt().get(i);
        // Increment the player and team tov stats
        incrementTeamStat(getTeamOnOffense(), TeamStatTypes.TEAM_TOV, 1);
        incrementPlayerStat(turoverPlayer, PlayerStatTypes.TOV, 1);

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
        return FOUL_RATE >= Utils.getRandomDouble(0.0, 1.0);
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
        int index = Utils.getRandomInteger(0, 4);
        Player foulingPlayer = (foulingTeam == getHomeTeam()) ?
                getHomePlayersOnCourt().get(index) : getAwayPlayersOnCourt().get(index);
        // Increment team and player foul stats
        incrementTeamStat(foulingTeam, TeamStatTypes.TEAM_FOULS, 1);
        incrementPlayerStat(foulingPlayer, PlayerStatTypes.FOULS, 1);
        // If the fouling team was defense, then the offense shoots free throws. Else the possession will just change.
        if (foulingTeam == getTeamOnDefense()) {
            // Pick a player to shoot the foul shots if not set
            if (fouledPlayer == null) {
                index = Utils.getRandomInteger(0, 4);
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
                if (Utils.getRandomDouble(0.0, 1.0) <=
                        (double) fouledPlayer.getEntityAttribute(PlayerAttributes.FREE_THROW.toString())) {
                    // Free throw made! Increment stats as needed
                    System.out.println(fouledPlayer.getName() + " has made one free-throw");
                    log(String.format("%s has made a free throw", fouledPlayer.getName()));
                    incrementTeamStat(getTeamOnOffense(), TeamStatTypes.TEAM_PTS, 1);
                    incrementTeamStat(getTeamOnOffense(), TeamStatTypes.TEAM_FREE_THROW_MADE, 1);
                    incrementPlayerStat(fouledPlayer, PlayerStatTypes.FREE_THROW_MADE, 1);
                    incrementPlayerStat(fouledPlayer, PlayerStatTypes.PTS, 1);
                }
                // Increment free throws attempted stats
                incrementPlayerStat(fouledPlayer, PlayerStatTypes.FREE_THROW_ATTEMPTS, 1);
                incrementTeamStat(getTeamOnOffense(), TeamStatTypes.TEAM_FREE_THROW_ATTEMPTS, 1);
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
        incrementTeamStat(getTeamOnOffense(), TeamStatTypes.TEAM_THREE_POINT_ATTEMPTS, 1);
        incrementPlayerStat(shooter, PlayerStatTypes.THREE_POINT_ATTEMPTS, 1);
        // Check if the shot was made based of thee players three pt attr. There is also an opportunity that the shot
        // is blocked before the shot goes up
        boolean shotBlocked = simulateBlock(true, shooter);
        if (Utils.getRandomDouble(0.0, 1.0)
                <= ((double) shooter.getEntityAttribute(PlayerAttributes.THREE_P_SCORING.toString()) * 0.5) && !shotBlocked) {
            simulateAssist();
            // Three point shot made! Increment stats as needed
            System.out.println(shooter.getName() + " has made a three point shot");
            log(String.format("%s has made a three point shot", shooter.getName()));
            incrementTeamStat(getTeamOnOffense(), TeamStatTypes.TEAM_THREE_POINT_MADE, 1);
            incrementTeamStat(getTeamOnOffense(), TeamStatTypes.TEAM_PTS, 3);
            incrementPlayerStat(shooter, PlayerStatTypes.THREE_POINT_MADE, 1);
            incrementPlayerStat(shooter, PlayerStatTypes.PTS, 3);
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
        incrementTeamStat(getTeamOnOffense(), TeamStatTypes.TEAM_TWO_POINT_MADE, 1);
        incrementTeamStat(getTeamOnOffense(), TeamStatTypes.TEAM_PTS, 2);
        incrementPlayerStat(shooter, PlayerStatTypes.TWO_POINT_MADE, 1);
        incrementPlayerStat(shooter, PlayerStatTypes.PTS, 2);
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
        double insideScoringAttr = (double) shooter.getEntityAttribute(PlayerAttributes.INSIDE_SCORING.toString());
        double midRangeShotAttr = (double) shooter.getEntityAttribute(PlayerAttributes.MID_SCORING.toString());
        // Increment attempts stat
        incrementTeamStat(getTeamOnOffense(), TeamStatTypes.TEAM_TWO_POINT_ATTEMPTS, 1);
        incrementPlayerStat(shooter, PlayerStatTypes.TWO_POINT_ATTEMPTS, 1);
        if (midRangeShotAttr > insideScoringAttr) {
            // Player will take a mid-range jump shot
            double probabilityFoul = 0.08; // A mid range shot has an 8% chance of being fouled
            double outcome = Utils.getRandomDouble(0.0, 1.0);
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
                boolean probabilityAndOne = (Utils.getRandomDouble(0.0, 1.0)) <= 0.05;
                recordMadeTwoPointer(shooter, probabilityAndOne);
            } else {
                boolean offensiveRebound = simulateRebound();
                if (offensiveRebound)
                    simulateShot();
            }
        } else {
            // if it was not a shot, then it was either a layup (inside_scoring) or dunk.
            double dunkAttribute = (double) shooter.getEntityAttribute(PlayerAttributes.DUNK.toString());
            if (dunkAttribute > insideScoringAttr) {
                // Dunk
                double probabilityFoul = 0.2; // Probability of foul on dunk is 40%
                double outcome = Utils.getRandomDouble(0.0, 1.0);
                if (outcome <= probabilityFoul) {
                    simulateFreeThrows(getTeamOnDefense(), 2, shooter);
                    return;
                }
                boolean blocked = simulateBlock(false, shooter);
                if (outcome <= dunkAttribute && !blocked) {
                    simulateAssist();
                    // Player made the dunk!
                    // Check to see if an and-one, probability is 25% on dunks
                    boolean probabilityAndOne = (Utils.getRandomDouble(0.0, 1.0)) <= 0.25;
                    recordMadeTwoPointer(shooter, probabilityAndOne);
                } else {
                    if (simulateRebound())
                        simulateShot();
                }
            } else {
                // Normal layup
                double probabilityFoul = 0.15;
                double outcome = Utils.getRandomDouble(0.0, 1.0);
                if (outcome <= probabilityFoul) {
                    simulateFreeThrows(getTeamOnDefense(), 2, shooter);
                    return;
                }
                boolean blocked = simulateBlock(false, shooter);
                if (outcome <= insideScoringAttr && !blocked) {
                    simulateAssist();
                    // Player made the layup!
                    // Check to see if an and-one
                    boolean probabilityAndOne = (Utils.getRandomDouble(0.0, 1.0)) <= 0.15;
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
        int i = Utils.getRandomInteger(0, 4);
        Player shooter = (getTeamOnOffense() == getHomeTeam())
                ? getHomePlayersOnCourt().get(i) : getAwayPlayersOnCourt().get(i);
        // Check to see if the shooter has the ball stolen
        if (simulateSteal(shooter))
            return; // No shot happens if the ball is stolen

        // First we check to see if a 3 pointer can occur
        double playerThreePtPercent;
        double teamThreePtPercent;
        try {
            playerThreePtPercent = shooter.getSumOfPlayerStat(PlayerStatTypes.THREE_POINT_MADE) /
                    shooter.getSumOfPlayerStat(PlayerStatTypes.THREE_POINT_ATTEMPTS);
            teamThreePtPercent = getTeamOnOffense().getSumOfTeamStat(TeamStatTypes.TEAM_THREE_POINT_MADE) /
                    getTeamOnOffense().getSumOfTeamStat(TeamStatTypes.TEAM_THREE_POINT_ATTEMPTS);
        } catch (ArithmeticException e) {
            playerThreePtPercent = 0;
            teamThreePtPercent = 0;
        }
        double threePtCutoffPoint = (playerThreePtPercent == 0 || teamThreePtPercent == 0) ?
                0 : ((playerThreePtPercent + teamThreePtPercent) / 0.2) * 0.3;
        if ((double) shooter.getEntityAttribute(PlayerAttributes.THREE_P_SCORING.toString()) > 0.85) {
            if (threePtCutoffPoint != 0 &&
                    Utils.getRandomDouble(0.0, 1.0) <= threePtCutoffPoint) {
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
        double decisionPoint = Utils.getRandomDouble(0.0, 1.0);
        double cutoffPoint = Utils.getRandomDouble(0.0, 1.0);
        boolean decision;
        int i = Utils.getRandomInteger(0, 4);
        if (jumpShot) {
            decision = decisionPoint <= PERIMETER_BLOCK_RATE;
            if (decision) {
                Player blockingPlayer = (getTeamOnOffense() == getHomeTeam()) ?
                        getAwayPlayersOnCourt().get(i) : getHomePlayersOnCourt().get(i);
                if ((double) blockingPlayer.getEntityAttribute(PlayerAttributes.PERIMETER_DEFENSE.toString()) >= cutoffPoint) {
                    log(String.format("%s has blocked a jump shot from %s", blockingPlayer.getName(), shooter.getName()));
                    incrementTeamStat(getTeamOnDefense(), TeamStatTypes.TEAM_BLK, 1);
                    incrementPlayerStat(blockingPlayer, PlayerStatTypes.BLK, 1);
                    return true;
                }
            }
        } else {
            decision = decisionPoint <= INSIDE_BLOCK_RATE;
            if (decision) {
                Player blockingPlayer = (getTeamOnOffense() == getHomeTeam()) ?
                        getAwayPlayersOnCourt().get(i) : getHomePlayersOnCourt().get(i);
                if ((double) blockingPlayer.getEntityAttribute(PlayerAttributes.INSIDE_DEFENSE.toString()) >= cutoffPoint) {
                    log(String.format("%s has blocked a inside shot from %s", blockingPlayer.getName(), shooter.getName()));
                    incrementTeamStat(getTeamOnDefense(), TeamStatTypes.TEAM_BLK, 1);
                    incrementPlayerStat(blockingPlayer, PlayerStatTypes.BLK, 1);
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
        if (Utils.getRandomDouble(0.0, 1.0) <= STEAL_RATE) {
            //Steal will (maybe) occur
            // Pick a random player to be the stealer
            int i = Utils.getRandomInteger(0, 4);
            Player stealer = (getTeamOnOffense() == getHomeTeam()) ? getAwayPlayersOnCourt().get(i) :
                    getHomePlayersOnCourt().get(i);
            if ((double) stealer.getEntityAttribute(PlayerAttributes.PERIMETER_DEFENSE.toString())
                    >= Utils.getRandomDouble(0.0, 1.0)) {
                log(String.format("%s has stolen the ball from %s", stealer.getName(), shooter.getName()));
                incrementTeamStat(getTeamOnDefense(), TeamStatTypes.TEAM_STL, 1);
                incrementPlayerStat(stealer, PlayerStatTypes.STL, 1);
                incrementTeamStat(getTeamOnOffense(), TeamStatTypes.TEAM_TOV, 1);
                incrementPlayerStat(shooter, PlayerStatTypes.TOV, 1);
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
        boolean assistHappens = Utils.getRandomDouble(0.0, 1.0) <= ASSIST_RATE;
        if (!assistHappens)
            return;
        int i = Utils.getRandomInteger(1, 15);
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
        incrementTeamStat(getTeamOnOffense(), TeamStatTypes.TEAM_ASSIST, 1);
        incrementPlayerStat(assister, PlayerStatTypes.ASSIST, 1);
    }

    /**
     * Simulate a rebound. Returns true if the offensive team gets the ball back
     *
     * @return boolean
     */
    private boolean simulateRebound() {
        // Check if it is offensive or defensive rebound. There is much higher chance for a defensive rebound
        boolean defensiveRebound = Utils.getRandomDouble(0.0, 1.0) <= DEFENSIVE_REBOUND_RATE;
        // Pick a player to shoot. Taller players have better chance
        int i = Utils.getRandomInteger(1, 15);
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
            incrementTeamStat(getTeamOnOffense(), TeamStatTypes.TEAM_DRB, 1);
            incrementPlayerStat(rebounder, PlayerStatTypes.DRB, 1);
            // log
            log(String.format("%s has grabbed a defensive rebound", rebounder.getName()));
            return false;
        } else {
            // Mark stats for rebound
            incrementTeamStat(getTeamOnOffense(), TeamStatTypes.TEAM_ORB, 1);
            incrementPlayerStat(rebounder, PlayerStatTypes.ORB, 1);
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
        int pointDiff = getHomeTeamStat(TeamStatTypes.TEAM_PTS) - getAwayTeamStat(TeamStatTypes.TEAM_PTS);
        // If there is a blowout for either team and the game is almost over, then  just run out the clock without a
        // a play and return the play length
        if (((getGameTime() + playLength) >= GAME_LENGTH_SECONDS)
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
            Team foulingTeam = (Utils.getRandomDouble(0.0, 1.0) >= 0.25)
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
