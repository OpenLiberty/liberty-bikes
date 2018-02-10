/**
 *
 */
package org.libertybikes.game.core;

import javax.websocket.Session;

public class Client {

    public final Session session;
    public final Player player;
    public final boolean autoRequeue;

    /**
     * Create a client which is only a spectator of a game
     */
    public Client(Session s) {
        this(s, null, true);
    }

    /**
     * Create a player who will be participating in the game
     */
    public Client(Session s, Player p) {
        this(s, p, false);
    }

    private Client(Session s, Player p, boolean autoReque) {
        session = s;
        player = p;
        this.autoRequeue = autoReque;
    }

    public boolean isPlayer() {
        return player != null;
    }

}
