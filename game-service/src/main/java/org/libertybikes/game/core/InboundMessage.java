/**
 *
 */
package org.libertybikes.game.core;

import jakarta.json.bind.annotation.JsonbProperty;

public class InboundMessage {

    public static enum GameEvent {
        GAME_START,
        GAME_REQUEUE
    }

    public DIRECTION direction;

    @JsonbProperty("playerjoined")
    public String playerJoinedId;

    @JsonbProperty("message")
    public GameEvent event;

    @JsonbProperty("spectatorjoined")
    public Boolean isSpectator;

    @JsonbProperty("hasGameBoard")
    public Boolean hasGameBoard;

}
