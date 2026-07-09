/**
 *
 */
package org.libertybikes.game.metric;

import java.util.concurrent.atomic.AtomicLong;

public class GameMetrics {

    // Backing atomics for gauge-style up/down counters.
    // ConcurrentGauge was removed in MP Metrics 5.0; values tracked in-memory.
    public static final AtomicLong currentRoundsValue        = new AtomicLong();
    public static final AtomicLong currentPlayersValue       = new AtomicLong();
    public static final AtomicLong currentPartiesValue       = new AtomicLong();
    public static final AtomicLong currentQueuedPlayersValue = new AtomicLong();

    // Metric name constants
    public static final String currentRoundsCounter          = "current_num_of_rounds";
    public static final String totalRoundsCounter            = "total_num_of_rounds";
    public static final String currentPlayersCounter         = "current_num_of_players";
    public static final String totalPlayersCounter           = "total_num_of_players";
    public static final String totalMobilePlayersCounter     = "total_num_of_mobile_players";
    public static final String gameRoundTimerMetadata        = "game_round_timer";
    public static final String currentPartiesCounterMetadata = "current_number_of_parties";
    public static final String currentQueuedPlayersCounter   = "current_num_of_players_in_queue";
    public static final String openWebsocketTimerMetadata    = "open_game_websocket_timer";

    private static AtomicLong atomicFor(String name) {
        switch (name) {
            case "current_num_of_rounds":           return currentRoundsValue;
            case "current_num_of_players":          return currentPlayersValue;
            case "current_number_of_parties":       return currentPartiesValue;
            case "current_num_of_players_in_queue": return currentQueuedPlayersValue;
            default: return null;
        }
    }

    public static void counterInc(String metricName) {
        AtomicLong atomic = atomicFor(metricName);
        if (atomic != null) {
            atomic.incrementAndGet();
        }
        // Non-gauge totals (totalRoundsCounter, totalPlayersCounter, etc.) are
        // tracked via @Counted on the entry-point methods; no-op here.
    }

    public static void counterDec(String metricName) {
        AtomicLong atomic = atomicFor(metricName);
        if (atomic != null) {
            atomic.decrementAndGet();
        }
        // pure counters cannot decrement; dec calls on non-gauge metrics are no-ops
    }
}
