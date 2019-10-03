package org.libertybikes.player.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.libertybikes.player.service.Player;

public class InMemPlayerDB implements PlayerDB {

    private final Map<String, Player> allPlayers = new HashMap<>();

    /**
     * Inserts a new player into the database.
     *
     * @return Returns true if the player was created. False if a player with the same ID already existed
     */
    @Override
    public boolean create(Player p) {
        return allPlayers.putIfAbsent(p.id, p) == null;
    }

    @Override
    public void update(Player p) {
        allPlayers.put(p.id, p);
    }

    @Override
    public Player get(String id) {
        return allPlayers.get(id);
    }

    @Override
    public Collection<Player> getAll() {
        return allPlayers.values();
    }

    @Override
    public Collection<Player> topPlayers(int numPlayers) {
        return allPlayers.values()
                        .stream()
                        .sorted(Player::compareOverall)
                        .limit(numPlayers)
                        .collect(Collectors.toList());
    }

    @Override
    public boolean exists(String id) {
        return allPlayers.containsKey(id);
    }

}
