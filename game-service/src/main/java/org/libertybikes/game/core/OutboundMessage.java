package org.libertybikes.game.core;

import java.util.Set;

import javax.json.bind.annotation.JsonbProperty;

public class OutboundMessage {

    public static class PlayerList {
        @JsonbProperty("playerlist")
        public final Player[] playerlist;

        public PlayerList(Set<Player> players) {
            // Send players in proper order, padding out empty slots with "Bot Player"
            playerlist = new Player[Player.MAX_PLAYERS];
            for (Player p : players)
                playerlist[p.playerNum] = p;
            for (int i = 0; i < Player.MAX_PLAYERS; i++)
                if (playerlist[i] == null)
                    playerlist[i] = new Player("", "Bot Player", (short) i);
        }
    }

    public static class GameStatus {
        @JsonbProperty("gameStatus")
        public final String gameStatus;

        public GameStatus(GameRound.State status) {
            this.gameStatus = status.toString();
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

    public static class AwaitPlayersCountdown {
        @JsonbProperty("awaitplayerscountdown")
        public final int seconds;

        public AwaitPlayersCountdown(int remainingPlayerAwaitTime) {
            this.seconds = remainingPlayerAwaitTime;
        }
    }

    public static class Heartbeat {
        @JsonbProperty("keepAlive")
        public final boolean keepAlive = true;

        public Heartbeat() {}
    }

    public static class QueuePosition {
        @JsonbProperty("queuePosition")
        public final int queuePosition;

        public QueuePosition(int pos) {
            queuePosition = pos;
        }
    }

    public static class ErrorEvent {
        @JsonbProperty("errorMessage")
        public final String msg;

        public ErrorEvent(String errMsg) {
            msg = errMsg;
        }
    }

}
