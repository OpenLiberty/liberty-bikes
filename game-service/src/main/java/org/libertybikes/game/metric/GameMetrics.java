/**
 *
 */
package org.libertybikes.game.metric;

import javax.enterprise.inject.spi.CDI;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetadataBuilder;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.Timer.Context;

public class GameMetrics {
    // MpMetric Metadatas
    public static final Metadata currentRoundsCounter = new MetadataBuilder()
                    .withName("current_num_of_rounds")
                    .withDisplayName("Current Number of Rounds")
                    .withDescription("Number of rounds currently running")
                    .withType(MetricType.CONCURRENT_GAUGE)
                    .build();

    public static final Metadata totalRoundsCounter = new MetadataBuilder()
                    .withName("total_num_of_rounds")
                    .withDisplayName("Total Number of Rounds")
                    .withDescription("Number of rounds that have been created")
                    .withType(MetricType.COUNTER)
                    .build();

    public static final Metadata currentPlayersCounter = new MetadataBuilder()
                    .withName("current_num_of_players")
                    .withDisplayName("Current Number of Players")
                    .withDescription("Number of players that are currently playing in a round")
                    .withType(MetricType.CONCURRENT_GAUGE)
                    .build();

    public static final Metadata totalPlayersCounter = new MetadataBuilder()
                    .withName("total_num_of_players")
                    .withDisplayName("Total Number of Players That Have Played")
                    .withDescription("Number of players that have played in a round, requeuing and replaying increases the count")
                    .withType(MetricType.COUNTER)
                    .build();

    public static final Metadata totalMobilePlayersCounter = new MetadataBuilder()
                    .withName("total_num_of_mobile_players")
                    .withDisplayName("Total Number of Mobile Players That Have Played")
                    .withDescription("Number of mobile players that have played in a round, requeuing and replaying increases the count")
                    .withType(MetricType.COUNTER)
                    .build();

    public static final Metadata gameRoundTimerMetadata = new MetadataBuilder()
                    .withName("game_round_timer")
                    .withDisplayName("Game Round Timer")
                    .withDescription("The Time Game Rounds Last")
                    .withType(MetricType.TIMER)
                    .withUnit(MetricUnits.SECONDS)
                    .build();

    public static final Metadata currentPartiesCounterMetadata = new MetadataBuilder()
                    .withName("current_number_of_parties")
                    .withDisplayName("Current Number of Parties")
                    .withDescription("Number of parties currently running")
                    .withType(MetricType.CONCURRENT_GAUGE)
                    .build();

    public static final Metadata currentQueuedPlayersCounter = new MetadataBuilder()
                    .withName("current_num_of_players_in_queue")
                    .withDisplayName("Current Number of Players Waiting In A Queue")
                    .withDescription("Number of players that are currently waiting in a queue")
                    .withType(MetricType.CONCURRENT_GAUGE)
                    .build();

    public static final Metadata openWebsocketTimerMetadata = new MetadataBuilder()
                    .withName("open_game_websocket_timer")
                    .withDisplayName("Open Game Round Websocket Timer")
                    .withDescription("The Time Game Round Websockets Are Open")
                    .withType(MetricType.TIMER)
                    .withUnit(MetricUnits.SECONDS)
                    .build();

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
            registry.concurrentGauge(metricMetadata).inc();
        }
    }

    public static void counterDec(Metadata metricMetadata) {
        if (registry != null || (getRegistry() != null)) {
            registry.concurrentGauge(metricMetadata).dec();
        }
    }

    public static Context timerStart(Metadata metricMetadata) {
        if (registry != null || (getRegistry() != null)) {
            return registry.timer(metricMetadata).time();
        }
        return null;
    }
}
