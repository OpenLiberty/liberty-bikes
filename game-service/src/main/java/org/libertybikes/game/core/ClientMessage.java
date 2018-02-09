/**
 *
 */
package org.libertybikes.game.core;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Andrew
 *
 */
public class ClientMessage {

    private static final Jsonb jsonb = JsonbBuilder.create();

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
        return jsonb.toJson(this);
    }

}
