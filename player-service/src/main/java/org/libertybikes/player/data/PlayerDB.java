/**
 *
 */
package org.libertybikes.player.data;

import java.util.Collection;

import org.libertybikes.player.service.Player;

public interface PlayerDB {

    /**
     * Inserts a new player into the database.
     *
     * @return Returns true if the player was created. False if a player with the same ID already existed
     */
    public boolean create(Player p);

    public void update(Player p);

    public Player get(String id);

    public Collection<Player> getAll();

    public Collection<Player> topPlayers(int numPlayers);

    public boolean exists(String id);

}
