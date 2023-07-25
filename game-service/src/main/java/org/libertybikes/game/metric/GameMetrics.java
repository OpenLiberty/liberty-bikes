/**
 *
 */
package org.libertybikes.game.metric;

import jakarta.enterprise.inject.spi.CDI;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.Timer.Context;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetadataBuilder;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.Gauge;

public class GameMetrics {
    // MpMetric Metadatas

    private static int totalPlayers = 0; 

    @Gauge(unit = MetricUnits.NONE,
            name = "playerNumberGauge",
            absolute = true,
            description = "Number of players in the game")
    public static int getPlayerCount() {
        return totalPlayers;
    }

    public static void incPlayerCount(){
        totalPlayers = totalPlayers + 1;
    }

    public static void decPlayerCount(){
        totalPlayers = totalPlayers -1 ;
    }
    /*public static final Metadata totalRoundsCounter = new MetadataBuilder()
                    .withName("total_num_of_rounds")
                    .withDescription("Number of rounds that have been created")
                    .build();

    public static final Metadata currentPlayersCounter = new MetadataBuilder()
                    .withName("current_num_of_players")
                    .withDescription("Number of players that are currently playing in a round")
                    .build();

    public static final Metadata totalPlayersCounter = new MetadataBuilder()
                    .withName("total_num_of_players")
                    .withDescription("Number of players that have played in a round, requeuing and replaying increases the count")
                    .build();

    public static final Metadata totalMobilePlayersCounter = new MetadataBuilder()
                    .withName("total_num_of_mobile_players")
                    .withDescription("Number of mobile players that have played in a round, requeuing and replaying increases the count")
                    .build();

    public static final Metadata gameRoundTimerMetadata = new MetadataBuilder()
                    .withName("game_round_timer")
                    .withDescription("The Time Game Rounds Last")
                    .withUnit(MetricUnits.SECONDS)
                    .build();

    public static final Metadata currentPartiesCounterMetadata = new MetadataBuilder()
                    .withName("current_number_of_parties")
                    .withDescription("Number of parties currently running")
                    .build();

    public static final Metadata currentQueuedPlayersCounter = new MetadataBuilder()
                    .withName("current_num_of_players_in_queue")
                    .withDescription("Number of players that are currently waiting in a queue")
                    .build();

    public static final Metadata openWebsocketTimerMetadata = new MetadataBuilder()
                    .withName("open_game_websocket_timer")
                    .withDescription("The Time Game Round Websockets Are Open")
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
    } */
}
