package utilities;

import attributes.LeagueAttributes;
import attributes.PlayerAttributes;
import attributes.PlayerStatTypes;
import attributes.TeamAttributes;
import attributes.TeamStatTypes;
import core.Team;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * CS 622
 * DatabaseConnection.java
 * This class is implemented as a singleton, since it is the sole connection point to the database for this program.
 * <p>
 * All methods implemented here relate to interfacing with the database for this program. The database uses sqlite.
 *
 * @author apalfi
 * @version 1.0
 */
public class DatabaseConnection {

    // Singleton for the database connection
    private static DatabaseConnection databaseConnection = null;

    // We keep one connection open to the DB at all times
    private Connection connection;

    private DatabaseConnection(String saveFilePath) {
        try {
            String connectionURL = "jdbc:sqlite:" + saveFilePath;
            connection = DriverManager.getConnection(connectionURL);
            initializeTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseConnection getInstance() {
        assert databaseConnection != null;
        return databaseConnection;
    }

    public static DatabaseConnection getInstance(String saveFilePath, boolean deleteIfNotNew) {
        assert databaseConnection == null;
        databaseConnection = new DatabaseConnection(saveFilePath);
        if (deleteIfNotNew)
            clearDB();
        return databaseConnection;
    }

    private static void clearDB() {
        DatabaseConnection.getInstance().clearTables();
    }

    private void clearTables() {
        List<String> tables = Arrays.asList("teams", "players", "team_stats", "player_stats", "games", "league");
        for (String table : tables) {
            String sql = "DELETE FROM " + table;
            try {
                connection.createStatement().executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Executes a sql query
     */
    public void executeSQL(String sql) {
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes a query and return a ResultSet
     */
    public ResultSet executeQuery(String sql) {
        try {
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Initializes/Creates all of the tables we will need
     */
    private void initializeTables() {
        createPlayersTable();
        createTeamTable();
        createPlayerStatsTable();
        createTeamStatsTable();
        createLeagueTable();
        createGamesTable();
    }

    /**
     * Creates the table for players.
     */
    private void createPlayersTable() {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS players (pid integer PRIMARY KEY, name text NOT NULL, " +
                "tid integer, ");
        for (PlayerAttributes attr : PlayerAttributes.values())
            sql.append(attr.toString()).append(" real,");
        sql.replace(sql.length() - 1, sql.length(), ");");
        executeSQL(sql.toString());
    }

    /**
     * Creates a table to store player stats
     */
    private void createPlayerStatsTable() {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS player_stats ");
        sql.append("( pid integer NOT NULL, name text NOT NULL, gid integer NOT NULL, tid integer NOT NULL, ");
        for (PlayerStatTypes stat : PlayerStatTypes.values())
            sql.append(stat.toString()).append(" real,");
        sql.append(" PRIMARY KEY (pid, gid, tid));");
        executeSQL(sql.toString());
    }

    /**
     * Creates a table to store team data
     */
    private void createTeamTable() {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS teams ");
        sql.append("( tid integer PRIMARY KEY,  name text NOT NULL, ");
        for (TeamAttributes attr : TeamAttributes.values())
            if (!Team.NON_GAME_RELATED_ATTRS.contains(attr))
                sql.append(attr.toString()).append(" real,");
            else
                sql.append(attr.toString()).append(" BLOB,");
        sql.replace(sql.length() - 1, sql.length(), ");");
        executeSQL(sql.toString());
    }

    /**
     * Creates a table to store team stats
     */
    private void createTeamStatsTable() {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS team_stats ");
        sql.append("( tid integer NOT NULL, name text NOT NULL, gid integer NOT NULL, ");
        for (TeamStatTypes stat : TeamStatTypes.values())
            sql.append(stat.toString()).append(" real,");
        sql.append("PRIMARY KEY (tid, gid));");

        executeSQL(sql.toString());
    }

    /**
     * Creates a table to store League data
     */
    private void createLeagueTable() {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS league(lid integer PRIMARY KEY,name text NOT NULL,");
        for (LeagueAttributes a : LeagueAttributes.values())
            sql.append(a.toString()).append(" integer,");
        sql.replace(sql.length() - 1, sql.length(), ");");
        executeSQL(sql.toString());
    }

    /**
     * Creates a table to store game data
     */
    private void createGamesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS games (gid integer PRIMARY_KEY, name text NOT NULL, GAME_CLOCK integer," +
                " HOME_TEAM integer, AWAY_TEAM integer, GAME_LOG BLOB)";
        executeSQL(sql);
    }


    /**
     * Returns the data stored in the league table.
     */
    public ResultSet getLeagueEntry() {
        try {
            // Should only be one row in this table
            assert executeQuery("SELECT COUNT(*) as count FROM league;").getInt("count") == 1;
            ResultSet rs = executeQuery("SELECT lid, name FROM league;");
            assert rs != null;
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet getAllTeamEntries() {
        return executeQuery("SELECT * from teams");
    }

    public ResultSet getAllPlayerEntries() {
        return executeQuery("SELECT * from players");
    }

    public ResultSet getAllGameEntries() {
        return executeQuery("SELECT * from games");
    }

    public ResultSet getStatEntriesForPlayer(int pid) {
        String sql = "SELECT * from player_stats WHERE pid=" + pid;
        return executeQuery(sql);
    }

    public ResultSet getStatEntriesForTeam(int tid) {
        String sql = "SELECT * from team_stats WHERE tid=" + tid;
        return executeQuery(sql);
    }

    public PreparedStatement getBlankPreparedStatement(String sql) {
        try {
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
