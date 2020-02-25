package core;

import gameplay.GameSimulation;
import gameplay.PlayerStat;
import gameplay.TeamStat;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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


    private DatabaseConnection() {
        // The database will be saved with the same name of the league
        try {
            String connectionURL = "jdbc:sqlite:" + League.getInstance().getLeagueSave().getAbsolutePath();
            connection = DriverManager.getConnection(connectionURL);
            initializeTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private DatabaseConnection(File previousSave) {
        try {
            String connectionURL = "jdbc:sqlite:" + previousSave.getAbsolutePath();
            connection = DriverManager.getConnection(connectionURL);
            initializeTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the singleton instance for this class
     *
     * @return DatabaseConnection
     */
    public static DatabaseConnection getInstance() {
        if (databaseConnection == null)
            databaseConnection = new DatabaseConnection();
        return databaseConnection;
    }

    public static DatabaseConnection getInstance(boolean clear) {
        assert databaseConnection == null;
        databaseConnection = new DatabaseConnection();
        clearDB();
        return databaseConnection;
    }

    public static DatabaseConnection getInstance(File previosuSave) {
        assert databaseConnection == null;
        databaseConnection = new DatabaseConnection(previosuSave);
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
    private void executeSQL(String sql) {
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes a query and return a ResultSet
     */
    private ResultSet executeQuery(String sql) {
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
            sql.append(attr.toString()).append(" real NOT NULL,");
        sql.replace(sql.length() - 1, sql.length(), ");");
        executeSQL(sql.toString());
    }

    /**
     * Creates a table to store player stats
     */
    private void createPlayerStatsTable() {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS player_stats ");
        sql.append("( pid integer NOT NULL, name text NOT NULL, gid integer NOT NULL, ");
        for (PlayerStat stat : PlayerStat.values())
            sql.append(stat.toString()).append(" real,");
        sql.append(" PRIMARY KEY (pid, gid));");
        executeSQL(sql.toString());
    }

    /**
     * Creates a table to store team data
     */
    private void createTeamTable() {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS teams ");
        sql.append("( tid integer PRIMARY KEY,  name text NOT NULL, ");
        for (TeamAttributes attr : TeamAttributes.values())
            sql.append(attr.toString()).append(" real NOT NULL,");
        sql.replace(sql.length() - 1, sql.length(), ");");
        executeSQL(sql.toString());
    }

    /**
     * Creates a table to store team stats
     */
    private void createTeamStatsTable() {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS team_stats ");
        sql.append("( tid integer NOT NULL, name text NOT NULL, gid integer NOT NULL, ");
        for (TeamStat stat : TeamStat.values())
            sql.append(stat.toString()).append(" real,");
        sql.append("PRIMARY KEY (tid, gid));");
        executeSQL(sql.toString());
    }

    /**
     * Creates a table to store League data
     */
    private void createLeagueTable() {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS league ");
        sql.append("( lid integer PRIMARY KEY, name text NOT NULL, userTeam integer );");
        executeSQL(sql.toString());
    }

    /**
     * Creates a table to store game data
     */
    private void createGamesTable() {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS games ");
        sql.append("( gid integer PRIMARY KEY, homeTeam integer NOT NULL, awayTeam integer NOT NULL, gameLog text );");
        executeSQL(sql.toString());
    }

    /**
     * Returns true if the id exists in the table
     *
     * @param table String
     * @param id    int
     * @return boolean
     */
    private boolean idExistsInTable(String table, int id) {
        String idName = String.valueOf(table.charAt(0)).toLowerCase() + "id";
        String sql = "SELECT EXISTS(SELECT 1 FROM " + table + " WHERE " + idName + "=" + id + ");";
        ResultSet rs = executeQuery(sql);
        if (rs == null) {
            return false;
        }
        try {
            return rs.getInt(1) == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates a new league entry in the League database
     */
    void insertNewLeagueEntry(League league, Team userTeam) {
        try {
            if (!idExistsInTable("league", league.getID())) {
                String sql = "INSERT INTO league(lid, name, userTeam) VALUES(?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, league.getID());
                statement.setString(2, league.getName());
                statement.setInt(3, userTeam.getID());
                statement.execute();
            } else {
                String sql = "UPDATE league SET name=?, userTeam=? WHERE lid=?";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, league.getName());
                statement.setInt(2, userTeam.getID());
                statement.setInt(3, league.getID());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates a Team entry in the team table. Creates a new entry if it does not exist yet
     */
    void updateTeamEntry(Team t) {
        StringBuilder sql = new StringBuilder();
        if (!idExistsInTable("teams", t.getID())) {
            sql.append("INSERT INTO teams (tid,name,");
            int count = 2;
            for (int i = 0; i < TeamAttributes.values().length; i++) {
                sql.append(TeamAttributes.values()[i].toString()).append(",");
                count++;
            }
            sql.replace(sql.length() - 1, sql.length(), ") VALUES(");
            for (int i = 0; i < count; i++)
                sql.append("?,");
            sql.replace(sql.length() - 1, sql.length(), ");");
            try {
                PreparedStatement statement = connection.prepareStatement(sql.toString());
                statement.setInt(1, t.getID());
                statement.setString(2, t.getName());
                int i = 3;
                for (TeamAttributes attr : TeamAttributes.values()) {
                    statement.setDouble(i, t.getEntityAttribute(attr.toString()));
                    i++;
                }
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Else update the existing team entry
            sql.append("UPDATE teams SET name = ?, ");
            for (int i = 0; i < TeamAttributes.values().length; i++) {
                sql.append(TeamAttributes.values()[i].toString()).append(" = ?");
                if (i != TeamAttributes.values().length - 1) {
                    sql.append(", ");
                }
            }
            sql.append(" WHERE tid = ?");
            try {
                PreparedStatement statement = connection.prepareStatement(sql.toString());
                statement.setString(1, t.getName());
                int i = 2;
                for (TeamAttributes attr : TeamAttributes.values()) {
                    statement.setDouble(i, t.getEntityAttribute(attr.toString()));
                    i++;
                }
                statement.setInt(i, t.getID());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Updates a player entry in the database, or adds a new entry if they do not yet exist
     */
    void updatePlayerEntry(Player p) {
        StringBuilder sql = new StringBuilder();
        if (!idExistsInTable("players", p.getID())) {
            sql.append("INSERT INTO players(pid,name,tid,");
            int count = 3;
            for (int i = 0; i < PlayerAttributes.values().length; i++) {
                count++;
                sql.append(PlayerAttributes.values()[i].toString()).append(",");
            }
            sql.replace(sql.length() - 1, sql.length(), ") VALUES(");
            for (int i = 0; i < count; i++) {
                sql.append("?,");
            }
            sql.replace(sql.length() - 1, sql.length(), ");");
            try {
                PreparedStatement statement = connection.prepareStatement(sql.toString());
                statement.setInt(1, p.getID());
                statement.setString(2, p.getName());
                Team playerTeam = LeagueFunctions.getPlayerTeam(p);
                if (playerTeam != null)
                    statement.setInt(3, playerTeam.getID());
                else
                    statement.setNull(3, Types.NULL);
                int i = 4;
                for (PlayerAttributes attr : PlayerAttributes.values()) {
                    statement.setDouble(i, p.getEntityAttribute(attr.toString()));
                    i++;
                }
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Else update the existing team entry
            sql.append("UPDATE players SET name = ?, tid = ?, ");
            for (int i = 0; i < PlayerAttributes.values().length; i++) {
                sql.append(PlayerAttributes.values()[i].toString()).append(" = ?");
                if (i != PlayerAttributes.values().length - 1) {
                    sql.append(", ");
                }
            }
            sql.append(" WHERE pid = ?");
            try {
                PreparedStatement statement = connection.prepareStatement(sql.toString());
                statement.setString(1, p.getName());
                Team playerTeam = LeagueFunctions.getPlayerTeam(p);
                if (playerTeam != null)
                    statement.setInt(2, playerTeam.getID());
                else
                    statement.setNull(2, Types.NULL);
                int i = 3;
                for (PlayerAttributes attr : PlayerAttributes.values()) {
                    statement.setDouble(i, p.getEntityAttribute(attr.toString()));
                    i++;
                }
                statement.setInt(i, p.getID());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Adds new player stats entry to the player stats table
     */
    void addPlayerStatEntry(Player p) {
        List<GameSimulation> playerGames = LeagueFunctions.getGamesForPlayer(p);
        for (GameSimulation gs : playerGames) {
            if (!gs.gameIsOver())
                continue;
            try {
                String sql = "SELECT EXISTS(SELECT 1 FROM player_stats WHERE pid=" + p.getID() + " AND gid=" + gs.getId() + ");";
                ResultSet rs = executeQuery(sql);
                boolean newEntry;
                if (rs == null)
                    newEntry = true;
                else
                    newEntry = rs.getInt(1) == 0;
                if (newEntry) {
                    StringBuilder insertSQL = new StringBuilder("INSERT INTO player_stats(pid, name, gid, ");
                    for (PlayerStat stat : PlayerStat.values())
                        insertSQL.append(stat.toString()).append(",");
                    insertSQL = new StringBuilder(insertSQL.toString().replaceAll(".$", ") VALUES("));
                    for (int i = 0; i < PlayerStat.values().length + 3; i++) {
                        insertSQL.append("?,");
                    }
                    insertSQL = new StringBuilder(insertSQL.toString().replaceAll(".$", ");"));
                    PreparedStatement statement = connection.prepareStatement(insertSQL.toString());
                    statement.setInt(1, p.getID());
                    statement.setString(2, p.getName());
                    statement.setInt(3, gs.getId());
                    int index = 4;
                    int gameIndex = playerGames.indexOf(gs);
                    for (PlayerStat stat : PlayerStat.values()) {
                        statement.setInt(index, (Integer) p.getStatContainer().getNthInstanceOfStat(stat, gameIndex));
                        index++;
                    }
                    statement.execute();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Adds a new team stat entry to the team stats table
     */
    void addTeamStatEntry(Team t) {
        List<GameSimulation> teamGames = LeagueFunctions.getGamesForTeam(t);
        for (GameSimulation gs : teamGames) {
            if (!gs.gameIsOver())
                continue;
            try {
                String sql = "SELECT EXISTS(SELECT 1 FROM team_stats WHERE tid=" + t.getID() + " AND gid=" + gs.getId() + ");";
                ResultSet rs = executeQuery(sql);
                boolean newEntry;
                if (rs == null)
                    newEntry = true;
                else
                    newEntry = rs.getInt(1) == 0;
                if (newEntry) {
                    StringBuilder insertSQL = new StringBuilder("INSERT INTO team_stats(tid, name, gid, ");
                    for (TeamStat stat : TeamStat.values())
                        insertSQL.append(stat.toString()).append(",");
                    insertSQL = new StringBuilder(insertSQL.toString().replaceAll(".$", ") VALUES("));
                    for (int i = 0; i < TeamStat.values().length + 3; i++) {
                        insertSQL.append("?,");
                    }
                    insertSQL = new StringBuilder(insertSQL.toString().replaceAll(".$", ");"));
                    PreparedStatement statement = connection.prepareStatement(insertSQL.toString());
                    statement.setInt(1, t.getID());
                    statement.setString(2, t.getName());
                    statement.setInt(3, gs.getId());
                    int index = 4;
                    int gameIndex = teamGames.indexOf(gs);
                    for (TeamStat stat : TeamStat.values()) {
                        statement.setInt(index, (Integer) t.getStatContainer().getNthInstanceOfStat(stat, gameIndex));
                        index++;
                    }
                    statement.execute();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Adds a new game entry to the games table
     */
    void addGameEntry(GameSimulation gs) {
        if (!idExistsInTable("games", gs.getId())) {
            String sql = "INSERT INTO games(gid, homeTeam, awayTeam, gameLog) VALUES(?, ?, ?, ?)";
            try {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, gs.getId());
                statement.setInt(2, gs.getHomeTeam().getID());
                statement.setInt(3, gs.getAwayTeam().getID());
                statement.setString(4, gs.getFullGameLog());
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Returns the data stored in the league table.
     */
    ResultSet getLeagueEntry() {
        try {
            // Should only be one row in this table
            assert executeQuery("SELECT COUNT(*) as count FROM league;").getInt("count") == 1;
            ResultSet rs = executeQuery("SELECT lid, name, userTeam FROM league;");
            assert rs != null;
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    ResultSet getAllTeamEntries() {
        return executeQuery("SELECT * from teams");
    }

    ResultSet getAllPlayerEntries() {
        return executeQuery("SELECT * from players");
    }

    ResultSet getAllGameEntriesSortedByID() {
        return executeQuery("SELECT * from games ORDER BY gid ASC");
    }

    ResultSet getAllGameAndTeamStatEntries() {
        return executeQuery("SELECT * from games JOIN team_stats ON games.gid = team_stats.gid");
    }

    ResultSet getAllGameAndPlayerStatEntries() {
        return executeQuery("SELECT * from games JOIN player_stats on games.gid = player_stats.gid");
    }

}
