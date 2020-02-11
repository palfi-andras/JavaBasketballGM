package gameplay;

import core.League;
import core.Player;
import core.PlayerAttributes;
import core.Team;
import core.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The GameSimulation class is tasked with simulating a basketball game between the two teams.
 * <p>
 * The current implementation takes into consideration play-by-play mechanics.
 */
public class GameSimulation {
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
    private static final double FOUL_RATE = 0.05;
    // Default value for how often turnovers occur
    private static final double TURNOVER_RATE = 0.08;
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


    public GameSimulation(Team home, Team away) {
        setId(League.getInstance().getNextUniqueKey());
        setHomeTeam(home);
        setAwayTeam(away);
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
        setHomePlayersOnCourt(new ArrayList<>(getHomeTeam().getRankedRoster().subList(0, 5)));
        setAwayPlayersOnCourt(new ArrayList<>(getAwayTeam().getRankedRoster().subList(0, 5)));


    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private void incrementGameTime(int seconds) {
        gameTime += seconds;
    }

    public Map<Team, List<Player>> getPlayersOnCourt() {
        return playersOnCourt;
    }

    public void setPlayersOnCourt(Map<Team, List<Player>> playersOnCourt) {
        this.playersOnCourt = playersOnCourt;
    }

    private List<Player> getHomePlayersOnCourt() {
        return getPlayersOnCourt().get(getHomeTeam());
    }

    private void setHomePlayersOnCourt(List<Player> players) {
        getPlayersOnCourt().put(getHomeTeam(), players);
    }

    private List<Player> getAwayPlayersOnCourt() {
        return getPlayersOnCourt().get(getAwayTeam());
    }

    private void setAwayPlayersOnCourt(List<Player> players) {
        getPlayersOnCourt().put(getAwayTeam(), players);
    }


    public Map<Player, Map<PlayerStat, Integer>> getPlayerStats() {
        return playerStats;
    }

    public void setPlayerStats(Map<Player, Map<PlayerStat, Integer>> playerStats) {
        this.playerStats = playerStats;
    }

    public Map<Team, Map<TeamStat, Integer>> getTeamStats() {
        return teamStats;
    }

    public void setTeamStats(Map<Team, Map<TeamStat, Integer>> teamStats) {
        this.teamStats = teamStats;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    private Team getTeamOnOffense() {
        return teamOnOffense;
    }

    private Team getTeamOnDefense() {
        return getTeamOnOffense() == getHomeTeam() ? getAwayTeam() : getHomeTeam();
    }

    public Map<TeamStat, Integer> getHomeTeamStats() {
        return getTeamStats().get(getHomeTeam());
    }

    public Map<TeamStat, Integer> getAwayTeamStats() {
        return getTeamStats().get(getAwayTeam());
    }

    private int getHomeTeamStat(TeamStat stat) {
        return Objects.requireNonNull(getHomeTeamStats()).get(stat);
    }

    private int getAwayTeamStat(TeamStat stat) {
        return Objects.requireNonNull(getAwayTeamStats()).get(stat);
    }

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

    private void setHomeTeamStat(TeamStat stat, int val) {
        Objects.requireNonNull(getHomeTeamStats()).put(stat, val);
    }

    private void setAwayTeamStat(TeamStat stat, int val) {
        Objects.requireNonNull(getAwayTeamStats()).put(stat, val);
    }

    private void setTeamStat(Team team, TeamStat stat, int val) {
        if (team == getHomeTeam()) {
            setHomeTeamStat(stat, val);
        } else if (team == getAwayTeam()) {
            setAwayTeamStat(stat, val);
        } else {
            System.err.println("UNKNOWN TEAM IN GAME: " + team.getName());
        }
    }

    public Map<PlayerStat, Integer> getPlayerStats(Player player) {
        return getPlayerStats().get(player);
    }

    private int getPlayerStat(Player player, PlayerStat stat) {
        return Objects.requireNonNull(getPlayerStats(player)).get(stat);
    }

    private void setPlayerStat(Player player, PlayerStat stat, int val) {
        Objects.requireNonNull(getPlayerStats(player)).put(stat, val);
    }

    private void incrementPlayerStat(Player player, PlayerStat stat, int amount) {
        setPlayerStat(player, stat,
                getPlayerStat(player, stat) + amount);
    }

    private void incrementTeamStat(Team team, TeamStat stat, int amount) {
        setTeamStat(team, stat,
                getTeamStat(team, stat) + amount);
    }

    private void swapPossession() {
        teamOnOffense = getTeamOnDefense();
    }

    private boolean regulationIsOver() {
        return gameTime >= GAME_LENGTH_SECONDS;
    }

    private boolean overtimeRequired() {
        assert regulationIsOver();
        return getHomeTeamStat(TeamStat.TEAM_PTS) == getAwayTeamStat(TeamStat.TEAM_PTS);
    }

    private Team getWinner() {
        assert regulationIsOver();
        return (getHomeTeamStat(TeamStat.TEAM_PTS) > getAwayTeamStat(TeamStat.TEAM_PTS)) ? getHomeTeam() : getAwayTeam();
    }

    private Team getLoser() {
        assert regulationIsOver();
        return (getWinner() == getHomeTeam()) ? getAwayTeam() : getHomeTeam();
    }

    /**
     * Performs substitutions if needed.
     */
    private void modifyPlayersOnCourt() {
        double energyLimit = 0.6;
        List<Player> homePlayersToRemove = new ArrayList<>();
        List<Player> awayPlayersToRemove = new ArrayList<>();

        for (Player p : getHomePlayersOnCourt())
            if (p.getPlayerEnergy() <= energyLimit)
                homePlayersToRemove.add(p);


        for (Player p : getAwayPlayersOnCourt())
            if (p.getPlayerEnergy() <= energyLimit)
                awayPlayersToRemove.add(p);

        for (Player p : homePlayersToRemove)
            getHomePlayersOnCourt().remove(p);
        for (Player p : awayPlayersToRemove)
            getAwayPlayersOnCourt().remove(p);


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
    }

    /**
     * After each play, the players on the court experience an energy decay at a rate of playLength/1000.
     * When a players energy goes below 0.8, they will be subbed out. Players not on the court experience an energy boost
     * at the same rate
     */
    private void scaleEnergyForPlayers(int playLength) {
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
     * Determine who wins a tip off randomly
     */
    private void determineRandomTipOffWinner() {
        teamOnOffense = (League.getInstance().getRandomDouble(0.0, 1.0) > 0.5) ? getHomeTeam() : getAwayTeam();
    }

    /**
     * Randomly determine how long a possession will take
     */
    private int determinePossessionTime() {
        return League.getInstance().getRandomInteger(4, SHOT_CLOCK_LENGTH_SECONDS);
    }

    private boolean determineIfTurnover() {
        double turnoverProb = (Integer) getTeamOnOffense().getStatContainer().getAvgValueOfStat(TeamStat.TEAM_TOV)
                / GAME_LENGTH_MIN;
        turnoverProb = (turnoverProb != 0) ? turnoverProb : TURNOVER_RATE;
        return turnoverProb >= League.getInstance().getRandomDouble(0.0, 1.0);
    }

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
     * Determines if a foul will happen now.
     */
    private boolean determineIfFoul() {
        // A foul occurs if a random double is below the foul rate threshold
        return FOUL_RATE >= League.getInstance().getRandomDouble(0.0, 1.0);
    }

    private void simulateFreeThrows(Team foulingTeam, int numShots, Player fouledPlayer) {
        // If the fouling team was on defense, then the offense shoots  free throws. Else we just increment foul stats
        // and return so that possession can change

        // Pick a player who committed the foul
        int index = League.getInstance().getRandomInteger(0, 4);
        Player foulingPlayer = (foulingTeam == getHomeTeam()) ?
                getHomePlayersOnCourt().get(index) : getAwayPlayersOnCourt().get(index);
        // Increment team and player foul stats
        incrementTeamStat(foulingTeam, TeamStat.TEAM_FOULS, 1);
        incrementPlayerStat(foulingPlayer, PlayerStat.FOULS, 1);
        // If the fouling team was defense, then the offense shoots free throws
        if (foulingTeam == getTeamOnDefense()) {
            // Pick a player to shoot the foul shots if not set
            if (fouledPlayer == null) {
                index = League.getInstance().getRandomInteger(0, 4);
                fouledPlayer = (foulingTeam == getHomeTeam()) ?
                        getAwayPlayersOnCourt().get(index) : getHomePlayersOnCourt().get(index);
            }
            System.out.println(foulingPlayer.getName() + " from " + foulingTeam.getName()
                    + " has committed a foul on " + fouledPlayer.getName());
            // Simulate each free throw taking into consideration the players free throw rating
            for (int i = 0; i < numShots; i++) {
                if (League.getInstance().getRandomDouble(0.0, 1.0) <=
                        fouledPlayer.getPlayerAttribute(PlayerAttributes.FREE_THROW)) {
                    System.out.println(fouledPlayer.getName() + " has made one free-throw");
                    incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_PTS, 1);
                    incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_FREE_THROW_MADE, 1);
                    incrementPlayerStat(fouledPlayer, PlayerStat.FREE_THROW_MADE, 1);
                    incrementPlayerStat(fouledPlayer, PlayerStat.PTS, 1);
                }
                incrementPlayerStat(fouledPlayer, PlayerStat.FREE_THROW_ATTEMPTS, 1);
                incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_FREE_THROW_ATTEMPTS, 1);
            }
        }
    }

    private void simulateThreePointer(Player shooter) {
        // Three point attempt
        incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_THREE_POINT_ATTEMPTS, 1);
        incrementPlayerStat(shooter, PlayerStat.THREE_POINT_ATTEMPTS, 1);
        // Check if the shot was made based of thee players three pt attr
        if (League.getInstance().getRandomDouble(0.0, 1.0)
                <= shooter.getPlayerAttribute(PlayerAttributes.THREE_P_SCORING)) {
            System.out.println(shooter.getName() + " has made a three point shot");
            incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_THREE_POINT_MADE, 1);
            incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_PTS, 3);
            incrementPlayerStat(shooter, PlayerStat.THREE_POINT_MADE, 1);
            incrementPlayerStat(shooter, PlayerStat.PTS, 3);
        } else {
            System.out.println(shooter.getName() + " has missed a 3 point shot");
        }
    }

    private void recordMadeTwoPointer(Player shooter, boolean andOne) {
        incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_TWO_POINT_MADE, 1);
        incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_PTS, 2);
        incrementPlayerStat(shooter, PlayerStat.TWO_POINT_MADE, 1);
        incrementPlayerStat(shooter, PlayerStat.PTS, 2);
        if (andOne) {
            System.out.println(shooter.getName() + " has made a two-point shot with an and-one");
            simulateFreeThrows(getTeamOnDefense(), 1, shooter);
        } else {
            System.out.println(shooter.getName() + " has made a two-point shot");
        }
    }

    private void simulateTwoPointer(Player shooter) {
        // First determine if this will be a mid-range shot or a shot inside the post. To determine this, we look
        // at the players MID_RANGE_SHOOTING and INSIDE_SCORING attributes.
        double insideScoringAttr = shooter.getPlayerAttribute(PlayerAttributes.INSIDE_SCORING);
        double midRangeShotAttr = shooter.getPlayerAttribute(PlayerAttributes.MID_SCORING);
        incrementTeamStat(getTeamOnOffense(), TeamStat.TEAM_TWO_POINT_ATTEMPTS, 1);
        incrementPlayerStat(shooter, PlayerStat.TWO_POINT_ATTEMPTS, 1);
        if (midRangeShotAttr > insideScoringAttr) {
            // Player will take a mid-range jump shot
            double probabilityFoul = 0.8;
            double outcome = League.getInstance().getRandomDouble(0.0, 1.0);
            // First check if player was fouled without making a shot
            if (outcome <= probabilityFoul) {
                simulateFreeThrows(getTeamOnDefense(), 2, shooter);
                return;
            }
            // Next check if the shot was made
            if (outcome <= midRangeShotAttr) {
                // Player made the shot!
                // Check to see if an and-one
                boolean probabilityAndOne = (League.getInstance().getRandomDouble(0.0, 1.0)) <= 0.05;
                recordMadeTwoPointer(shooter, probabilityAndOne);
            }
        } else {
            // if it was not a shot, then it was either a layup (inside_scoring) or dunk.
            double dunkAttribute = shooter.getPlayerAttribute(PlayerAttributes.DUNK);
            if (dunkAttribute > insideScoringAttr) {
                // Dunk
                double probabilityFoul = 0.4;
                double outcome = League.getInstance().getRandomDouble(0.0, 1.0);
                if (outcome <= probabilityFoul) {
                    simulateFreeThrows(getTeamOnDefense(), 2, shooter);
                    return;
                }
                if (outcome <= dunkAttribute) {
                    // Player made the dunk!
                    // Check to see if an and-one
                    boolean probabilityAndOne = (League.getInstance().getRandomDouble(0.0, 1.0)) <= 0.25;
                    recordMadeTwoPointer(shooter, probabilityAndOne);
                }
            } else {
                // Normal layup
                double probabilityFoul = 0.35;
                double outcome = League.getInstance().getRandomDouble(0.0, 1.0);
                if (outcome <= probabilityFoul) {
                    simulateFreeThrows(getTeamOnDefense(), 2, shooter);
                    return;
                }
                if (outcome <= insideScoringAttr) {
                    // Player made the layup!
                    // Check to see if an and-one
                    boolean probabilityAndOne = (League.getInstance().getRandomDouble(0.0, 1.0)) <= 0.15;
                    recordMadeTwoPointer(shooter, probabilityAndOne);
                }
            }
        }
    }


    private void simulateShot() {
        // First pick a player to be the shooting player
        int i = League.getInstance().getRandomInteger(0, 4);
        Player shooter = (getTeamOnOffense() == getHomeTeam())
                ? getHomePlayersOnCourt().get(i) : getAwayPlayersOnCourt().get(i);
        // First we check to see if a 3 pointer can occur
//        double playerThreePtPercent = shooter.getSumOfPlayerStat(PlayerStat.THREE_POINT_MADE) /
//        shooter.getSumOfPlayerStat(PlayerStat.THREE_POINT_ATTEMPTS);
        //double teamThreePtPercent = getTeamOnOffense().getSumOfTeamStat(TeamStat.TEAM_THREE_POINT_MADE) /
        // getTeamOnOffense().getSumOfTeamStat(TeamStat.TEAM_THREE_POINT_ATTEMPTS);
        //double threePtCutoffPoint = ((playerThreePtPercent + teamThreePtPercent) / 0.2) * 0.3;
        if (shooter.getPlayerAttribute(PlayerAttributes.THREE_P_SCORING) > 0.65) {
            //&& League.getInstance().getRandomDouble(0.0, 1.0) <= threePtCutoffPoint
            simulateThreePointer(shooter);
        } else {
            // Two point shot
            simulateTwoPointer(shooter);
        }
    }

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
     * @return The winning team
     */
    public Team simulateGame() {
        simRegulation();
        return getWinner();
    }
}
