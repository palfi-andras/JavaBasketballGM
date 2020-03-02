package core;

import attributes.PlayerAttributes;
import utilities.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * CS-622
 * LeagueFunctions.java
 * <p>
 * This class provides static functions to interface with the league data. Essentially it is the API for data queries
 * we can make against the League object
 *
 * @author Andras Palfi apalfi@bu.edu
 */
public class LeagueFunctions {


    public static Team getPlayerTeam(Player p) {
        return League.getInstance().getTeam((int) p.getEntityAttribute(PlayerAttributes.TEAM_ID.toString()));
    }

    public static List<Player> getBestPlayers() {
        List<Player> players = new LinkedList<>(League.getInstance().getPlayers());
        players.sort(Comparator.comparingInt(Player::getOverallPlayerRating));
        Collections.reverse(players);
        return players;
    }

    public static List<GameSimulation> getAllGames() {
        return League.getInstance().getGames();
    }

    public static List<GameSimulation> getAllExpiredGames() {
        List<GameSimulation> games = new LinkedList<>();
        for (GameSimulation g : getAllGames())
            if (g.gameIsOver())
                games.add(g);
        return games;
    }

    public static List<GameSimulation> getAllUnplayedGames() {
        List<GameSimulation> games = new LinkedList<>();
        List<GameSimulation> expiredGames = getAllExpiredGames();
        for (GameSimulation g : getAllGames())
            if (!expiredGames.contains(g))
                games.add(g);
        return games;
    }


    public static List<GameSimulation> getGamesForTeam(Team team) {
        List<GameSimulation> games = new ArrayList<>();
        for (GameSimulation gs : League.getInstance().getGames())
            if (gs.getHomeTeam() == team || gs.getAwayTeam() == team)
                games.add(gs);
        return games;
    }

    public static List<GameSimulation> getGamesForPlayer(Player player) {
        List<GameSimulation> games = new LinkedList<>();
        Team playerTeam = getPlayerTeam(player);
        for (GameSimulation gs : getAllGames())
            if (gs.getHomeTeam() == playerTeam || gs.getAwayTeam() == playerTeam)
                games.add(gs);
        return games;
    }

    public static GameSimulation getNextGameForTeam(Team team) {
        return getGamesForTeam(team).get(getNumOfGamesPlayedForTeam(team));
    }


    public static List<Player> getFreeAgents() {
        List<Player> freeAgents = new LinkedList<>(getBestPlayers());
        for (Team t : League.getInstance().getTeams())
            for (Player p : t.getRoster())
                freeAgents.remove(p);
        return freeAgents;
    }

    public static Player getBestAvailableFreeAgent() {
        return (getFreeAgents().size() > 0) ? getFreeAgents().get(0) : null;
    }

    /**
     * Returns an int array representing the win-loss record of a team. Format:
     * [numWins, numLosses]
     */
    public static int[] getTeamRecord(Team team) {
        int[] record = new int[2];
        int numWin = 0, numLoss = 0;
        for (GameSimulation game : getGamesForTeam(team)) {
            if (!game.gameIsOver()) {
                continue;
            }
            if (game.getWinner() == team)
                numWin++;
            else
                numLoss++;
        }
        record[0] = numWin;
        record[1] = numLoss;
        return record;
    }

    /**
     * Returns the number of games a team has played
     */
    public static int getNumOfGamesPlayedForTeam(Team team) {
        int[] record = getTeamRecord(team);
        return record[0] + record[1];
    }

    /**
     * Returns true if a team has not yet played any games
     *
     * @return boolean
     */
    public static boolean teamHasNotPlayedGames(Team team) {
        return Arrays.equals(getTeamRecord(team), new int[]{0, 0});
    }

    /**
     * Simulate a game in the league
     */
    public static void simulateGame(GameSimulation gs) {
        gs.simulateGame();
    }


    /**
     * Releases a player from their team and into free agency
     */
    public static void releasePlayerIntoFreeAgency(Player p) {
        Team playerTeam = getPlayerTeam(p);
        assert playerTeam != null;
        playerTeam.removePlayerFromRoster(p);
    }

    public static double getLeagueAvgTeamOvrRating() {
        int count = 0;
        double avg = 0.0;
        for (Team t : League.getInstance().getTeams()) {
            if (t.getRoster().size() <= 0)
                continue;
            count++;
            avg += t.getOverallTeamRating();
        }
        return (count == 0) ? avg : Utils.round((avg / count), 2);
    }
}
