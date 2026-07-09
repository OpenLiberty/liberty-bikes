package org.libertybikes.game.core;

import java.util.Date;

import jakarta.enterprise.concurrent.LastExecution;
import jakarta.enterprise.concurrent.Trigger;

/**
 * Schedules heartbeat events for a GameRound at a fixed interval.
 * Extracted from GameRound to avoid loading jakarta.enterprise.concurrent.Trigger
 * at GameRound class-load time (which breaks unit tests running outside a container).
 */
class HeartbeatTrigger implements Trigger {

    private static final int HEARTBEAT_INTERVAL_SEC = 100;

    private final GameRound round;

    HeartbeatTrigger(GameRound round) {
        this.round = round;
    }

    @Override
    public Date getNextRunTime(LastExecution lastExecutionInfo, Date taskScheduledTime) {
        if (round.clients.size() == 0) {
            round.log("No clients remaining.  Cancelling heartbeat.");
            round.endGame();
            return null;
        }
        return Date.from(java.time.Instant.now().plusSeconds(HEARTBEAT_INTERVAL_SEC));
    }

    @Override
    public boolean skipRun(LastExecution lastExecutionInfo, Date scheduledRunTime) {
        return round.clients.size() == 0;
    }
}
