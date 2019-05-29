/**
 *
 */
package org.libertybikes.game.metric;

import javax.enterprise.inject.spi.CDI;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.Timer.Context;

public class GameMetrics {
    // MpMetric Metadatas
    public static final Metadata currentRoundsCounter = new Metadata("current_num_of_rounds", // name
                    "Current Number of Rounds", // display name
                    "Number of rounds currently running", // description
                    MetricType.COUNTER, // type
                    MetricUnits.NONE); // units

    public static final Metadata totalRoundsCounter = new Metadata("total_num_of_rounds", // name
                    "Total Number of Rounds", // display name
                    "Number of rounds that have been created", // description
                    MetricType.COUNTER, // type
                    MetricUnits.NONE); // units

    public static final Metadata currentPlayersCounter = new Metadata("current_num_of_players", // name
                    "Current Number of Players", // display name
                    "Number of players that are currently playing in a round", // description
                    MetricType.COUNTER, // type
                    MetricUnits.NONE); // units

    public static final Metadata totalPlayersCounter = new Metadata("total_num_of_players", // name
                    "Total Number of Players That Have Played", // display name
                    "Number of players that have played in a round, requeuing and replaying increases the count", // description
                    MetricType.COUNTER, // type
                    MetricUnits.NONE); // units

    public static final Metadata totalMobilePlayersCounter = new Metadata("total_num_of_mobile_players", // name
                    "Total Number of Mobile Players That Have Played", // display name
                    "Number of mobile players that have played in a round, requeuing and replaying increases the count", // description
                    MetricType.COUNTER, // type
                    MetricUnits.NONE); // units

    public static final Metadata gameRoundTimerMetadata = new Metadata("game_round_timer", // name
                    "Game Round Timer", // display name
                    "The Time Game Rounds Last", // description
                    MetricType.TIMER, // type
                    MetricUnits.SECONDS); // units

    public static final Metadata currentPartiesCounterMetadata = new Metadata("current_number_of_parties", // name
                    "Current Number of Parties", // display name
                    "Number of parties currently running", // description
                    MetricType.COUNTER, // type
                    MetricUnits.NONE); // units

    public static final Metadata currentQueuedPlayersCounter = new Metadata("current_num_of_players_in_queue", // name
                    "Current Number of Players Waiting In A Queue", // display name
                    "Number of players that are currently waiting in a queue", // description
                    MetricType.COUNTER, // type
                    MetricUnits.NONE); // units

    public static final Metadata openWebsocketTimerMetadata = new Metadata("open_game_websocket_timer", // name
                    "Open Game Round Websocket Timer", // display name
                    "The Time Game Round Websockets Are Open", // description
                    MetricType.TIMER, // type
                    MetricUnits.SECONDS); // units

    private static MetricRegistry registry;

    private static MetricRegistry getRegistry() {
        try {
            registry = CDI.current().select(MetricRegistry.class).get();
            System.out.println("MetricRegistry configured");
            return registry;
        } catch (IllegalStateException ise) {
            System.out.println("WARNING: Unable to locate CDIProvider");
            ise.printStackTrace();
        }
        return null;
    }

    public static void counterInc(Metadata metricMetadata) {
        if (registry != null || (getRegistry() != null)) {
            registry.counter(metricMetadata).inc();
        }
    }

    public static void counterDec(Metadata metricMetadata) {
        if (registry != null || (getRegistry() != null)) {
            registry.counter(metricMetadata).dec();
        }
    }

    public static Context timerStart(Metadata metricMetadata) {
        if (registry != null || (getRegistry() != null)) {
            return registry.timer(metricMetadata).time();
        }
        return null;
    }
}
