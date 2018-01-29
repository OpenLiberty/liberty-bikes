/**
 *
 */
package org.libertybikes.game.core;

/**
 * @author Andrew
 *
 */
public class ClientMessage {

    public static enum GameEvent {
        GAME_START,
        GAME_PAUSE,
        GAME_REQUEUE
    }

    public Player.DIRECTION direction;

    public String playerjoined;

    public GameEvent event;

}
