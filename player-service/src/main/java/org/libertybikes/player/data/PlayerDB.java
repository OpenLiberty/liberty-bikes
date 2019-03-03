package org.libertybikes.player.data;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.libertybikes.player.service.Player;

@ApplicationScoped
public class PlayerDB {

    // TODO back this by a DB instead of in-mem
    private final Map<String, Player> allPlayers = new ConcurrentHashMap<>();

    /**
     * Inserts a new player into the database.
     *
     * @return Returns true if the player was created. False if a player with the same ID already existed
     */
    public boolean create(Player p) {
        return allPlayers.putIfAbsent(p.id, p) == null;
    }

    public void update(Player p) {
        allPlayers.put(p.id, p);
    }

    public Player get(String id) {
        return allPlayers.get(id);
    }

    public Collection<Player> getAll() {
        return allPlayers.values();
    }

    public Collection<Player> topPlayers(int numPlayers) {
        return allPlayers.values()
                        .stream()
                        .sorted(Player::compareOverall)
                        .limit(numPlayers)
                        .collect(Collectors.toList());
    }

    public long getRank(String id) {
        Player p = get(id);
        if (p == null)
            return -1;
        int wins = p.stats.numWins;
        long numPlayersAhead = allPlayers.values()
                        .stream()
                        .filter(otherPlayer -> otherPlayer.stats.numWins > wins)
                        .count();
        return numPlayersAhead + 1;
    }

    public boolean exists(String id) {
        return allPlayers.containsKey(id);
    }

}
