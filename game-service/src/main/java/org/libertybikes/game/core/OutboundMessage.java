/**
 *
 */
package org.libertybikes.game.core;

import java.util.Set;

import javax.json.bind.annotation.JsonbProperty;

public class OutboundMessage {

    public static class PlayerList {
        @JsonbProperty("playerlist")
        public final Set<Player> playerlist;

        public PlayerList(Set<Player> playerlist) {
            this.playerlist = playerlist;
        }
    }

    public static class RequeueGame {
        @JsonbProperty("requeue")
        public final String roundId;

        public RequeueGame(String nextRoundId) {
            this.roundId = nextRoundId;
        }
    }

    public static class StartingCountdown {
        @JsonbProperty("countdown")
        public final int seconds;

        public StartingCountdown(int startingSeconds) {
            this.seconds = startingSeconds;
        }
    }

}
