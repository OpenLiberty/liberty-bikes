package org.libertybikes.game.core;

import java.util.Optional;

import javax.websocket.Session;

public class Client {

    public final Session session;
    public final Optional<Player> player;
    public final boolean autoRequeue;
    public boolean isPhone = false;

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
        player = Optional.ofNullable(p);
        this.autoRequeue = autoReque;
    }

}
