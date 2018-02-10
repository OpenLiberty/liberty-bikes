/**
 *
 */
package org.libertybikes.game.core;

import javax.websocket.Session;

public class Client {

    public final Session session;
    public final Player player;

    /**
     * Create a client which is only a spectator of a game
     */
    public Client(Session s) {
        this(s, null);
    }

    /**
     * Create a player who will be participating in the game
     */
    public Client(Session s, Player p) {
        session = s;
        player = p;
    }

    public boolean isPlayer() {
        return player != null;
    }

}
