package org.libertybikes.player.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.libertybikes.player.service.Player;
import org.libertybikes.player.service.PlayerStats;

public class PersistentPlayerDB implements PlayerDB {

    private static final String TABLE_NAME = "PLAYERS";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_NUM_GAMES = "totalGames";
    private static final String COL_NUM_WINS = "totalWins";
    private static final String COL_RATING = "rating";

    private final DataSource ds;

    public PersistentPlayerDB() throws NamingException {
        ds = InitialContext.doLookup("java:comp/DefaultDataSource");
    }

    public boolean isAvailable() {
        try (Connection con = ds.getConnection()) {
            String CREATE_TABLE = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                            .append(TABLE_NAME)
                            .append(" (")
                            .append(COL_ID)
                            .append(" varchar(30) not null, ")
                            .append(COL_NAME)
                            .append(" varchar(20) not null, ")
                            .append(COL_NUM_GAMES)
                            .append(" integer, ")
                            .append(COL_NUM_WINS)
                            .append(" integer, ")
                            .append(COL_RATING)
                            .append(" integer, ")
                            .append("PRIMARY KEY (")
                            .append(COL_ID)
                            .append(") )")
                            .toString();
            System.out.println("Creating table with SQL:");
            System.out.println(CREATE_TABLE);
            con.createStatement().execute(CREATE_TABLE);
            return true;
        } catch (SQLException notConfigured) {
            System.err.println("Unable to initialize database because of: " + notConfigured.getMessage());
            return false;
        }
    }

    /**
     * Inserts a new player into the database.
     *
     * @return Returns true if the player was created. False if a player with the same ID already existed
     */
    @Override
    public boolean create(Player p) {
        if (exists(p.id))
            return false;

        try (Connection con = ds.getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO " + TABLE_NAME + " VALUES (?,?,?,?,?)");
            ps.setString(1, p.id);
            ps.setString(2, p.name);
            ps.setInt(3, p.stats.totalGames);
            ps.setInt(4, p.stats.numWins);
            ps.setInt(5, p.stats.rating);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void update(Player p) {
        try (Connection con = ds.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE " + TABLE_NAME +
                                                        " SET " +
                                                        COL_NAME + " = ?, " +
                                                        COL_NUM_GAMES + " = ?, " +
                                                        COL_NUM_WINS + " = ?, " +
                                                        COL_RATING + " = ? " +
                                                        " WHERE " + COL_ID + " = ?");
            ps.setString(1, p.name);
            ps.setInt(2, p.stats.totalGames);
            ps.setInt(3, p.stats.numWins);
            ps.setInt(4, p.stats.rating);
            ps.setString(5, p.id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Player get(String id) {
        try (Connection con = ds.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID + " = ?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? inflate(rs) : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Collection<Player> getAll() {
        Set<Player> allPlayers = new HashSet<>();
        try (Connection con = ds.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM " + TABLE_NAME);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                allPlayers.add(inflate(rs));
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
        return allPlayers;
    }

    @Override
    public Collection<Player> topPlayers(int numPlayers) {
        List<Player> allPlayers = new ArrayList<>();
        try (Connection con = ds.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM " + TABLE_NAME +
                                                        " ORDER BY " + COL_RATING + " DESC, " + COL_NUM_WINS + " DESC" +
                                                        " LIMIT ?");
            ps.setInt(1, numPlayers);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                allPlayers.add(inflate(rs));
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
        return allPlayers;
    }

    @Override
    public boolean exists(String id) {
        try (Connection con = ds.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT EXISTS (" +
                                                        "SELECT 1 FROM " + TABLE_NAME + " WHERE " + COL_ID + " = ? )");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Player inflate(ResultSet rs) throws SQLException {
        PlayerStats stats = new PlayerStats();
        stats.totalGames = rs.getInt(COL_NUM_GAMES);
        stats.numWins = rs.getInt(COL_NUM_WINS);
        stats.rating = rs.getInt(COL_RATING);
        Player p = new Player(rs.getString(COL_NAME), rs.getString(COL_ID), stats);
        return p;
    }

}
