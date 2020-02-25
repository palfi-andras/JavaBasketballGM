package core;

import gameplay.GameSimulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public static List<Team> getAllTeams() {
        List<Team> teams = new LinkedList<>();
        for (int t : League.getInstance().getTeamEntityIndexes())
            teams.add((Team) League.getInstance().getEntities().get(t));
        return teams;
    }

    public static Team getTeam(int id) {
        return (Team) League.getInstance().getEntity(id);
    }

    public static Team getTeam(Team team) {
        return getTeam(team.getID());
    }

    public static Team getTeam(String teamName) {
        for (Team t : getAllTeams()) {
            if (Objects.equals(teamName, t.toString()))
                return t;
        }
        return null;
    }

    public static Team getPlayerTeam(Player p) {
        for (Team t : getAllTeams())
            if (t.getRoster().contains(p))
                return t;
        return null;
    }

    public static Player getPlayer(int pid) {
        Player p = null;
        for (Player player : getAllPlayers())
            if (player.getID() == pid)
                p = player;
        return p;
    }

    public static int getRosterSize(Team t) {
        return getTeam(t.getID()).getRoster().size();
    }


    public static List<GameSimulation> getAllGames() {
        return League.getInstance().getGames();
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

    public static List<Player> getAllPlayers() {
        List<Player> players = new LinkedList<>();
        for (int p : League.getInstance().getPlayerEntityIndexes())
            players.add((Player) League.getInstance().getEntities().get(p));
        return players;
    }

    public static List<Player> getBestPlayers() {
        List<Player> players = getAllPlayers();
        players.sort(Comparator.comparingInt(Player::getOverallPlayerRating));
        Collections.reverse(players);
        return players;
    }

    public static List<Player> getFreeAgents() {
        List<Player> freeAgents = new LinkedList<>(getBestPlayers());
        for (Team t : getAllTeams())
            for (Player p : t.getRoster())
                freeAgents.remove(p);
        return freeAgents;
    }

    public static Player getBestAvailableFreeAgent() {
        return (getFreeAgents().size() > 0) ? getFreeAgents().get(0) : null;
    }


    public static Player getPlayer(String name) {
        for (Player p : getAllPlayers()) {
            if (p.getName() == name)
                return p;
        }
        return null;
    }


    public static Map<Player, Double> getHighestPPGInTeam(Team team) {
        List<Player> ppgs = new ArrayList<>(team.getRoster());
        return sortListOfPlayersBasedOnPPG(ppgs);
    }

    private static Map<Player, Double> sortListOfPlayersBasedOnPPG(List<Player> ppgs) {
        ppgs.sort(Comparator.comparingDouble(Player::getPlayerPointsPerGame));
        Collections.reverse(ppgs);
        Map<Player, Double> map = new LinkedHashMap<>();
        for (Player p : ppgs)
            map.put(p, p.getPlayerPointsPerGame());
        return map;
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
        League.getInstance().recordGameResult(gs.simulateGame());
        League.getInstance().recordStats(gs);
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
        for (Team t : getAllTeams()) {
            if (t.getRoster().size() <= 0)
                continue;
            count++;
            avg += t.getOverallTeamRating();
        }
        return (count == 0) ? avg : Utils.round((avg / count), 2);
    }
}
