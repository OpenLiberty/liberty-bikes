/**
 *
 */
package org.libertybikes.game.core;

import javax.json.bind.annotation.JsonbProperty;

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

    @JsonbProperty("playerjoined")
    public String playerJoinedId;

    @JsonbProperty("message")
    public GameEvent event;

    @Override
    public String toString() {
        return "{ direction=" + direction + ", playerjoined=" + playerJoinedId + ", event=" + event + " }";
    }

}
