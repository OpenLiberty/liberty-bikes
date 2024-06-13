/**
 *
 */
package org.libertybikes.game.metric;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;

import org.eclipse.microprofile.metrics.MetricUnits;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.metrics.Timer.Context;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetadataBuilder;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.RegistryScope;

@Path("/")
@ApplicationScoped
public class GameMetrics {
    // MpMetric Metadatas

    private int totalPlayers = 0;
    private int totalMobilePlayers = 0;

    @Inject
    private MetricRegistry registry;

    public void observer(@Observes Startup ignored) {
        System.out.println("TESTING THE OBSERVER");
    }

    @Gauge(unit = MetricUnits.NONE,
             name = "playerNumberGauge",
             absolute = true,
             description = "Number of players in the game")
    public int getPlayerCount() {
        System.out.println("Total players (updated again and again) " + totalPlayers);
        return totalPlayers;
    }

    @Gauge(unit = MetricUnits.NONE,
           name = "mobilePlayerNumberGauge",
           absolute = true,
           description = "Number of mobile players in the game")
    public int getMobilePlayerCount() {
        return totalMobilePlayers;
    }

    @Counted(unit = MetricUnits.NONE,
             name = "roundNumberCounter",
             absolute = true,
             description = "Number of total rounds played")
    public void totalRoundsCounter() {
        return;
    }

    public void incPlayerCount() {
        System.out.println("Increasing player count");
        totalPlayers = getPlayerCount() + 1;
        System.out.println("Increasing player count");
    }

    public void decPlayerCount() {
        totalPlayers = getPlayerCount() - 1;
    }

    public void incMobilePlayerCount() {
        totalMobilePlayers = totalMobilePlayers + 1;
    }

    public void decMobilePlayerCount() {
        totalMobilePlayers = totalMobilePlayers - 1;
    }

    private final Metadata totalRoundsCounter = new MetadataBuilder()
                    .withName("total_num_of_rounds")
                    .withDescription("Number of rounds that have been created")
                    .build();

    private final Metadata currentPlayersCounter = new MetadataBuilder()
                    .withName("current_num_of_players")
                    .withDescription("Number of players that are currently playing in a round")
                    .build();

    private final Metadata totalPlayersCounter = new MetadataBuilder()
                    .withName("total_num_of_players")
                    .withDescription("Number of players that have played in a round, requeuing and replaying increases the count")
                    .build();

    private final Metadata totalMobilePlayersCounter = new MetadataBuilder()
                    .withName("total_num_of_mobile_players")
                    .withDescription("Number of mobile players that have played in a round, requeuing and replaying increases the count")
                    .build();
    /*
     * public static final Metadata gameRoundTimerMetadata = new MetadataBuilder()
     * .withName("game_round_timer")
     * .withDescription("The Time Game Rounds Last")
     * .withUnit(MetricUnits.SECONDS)
     * .build();
     * 
     * public static final Metadata currentPartiesCounterMetadata = new MetadataBuilder()
     * .withName("current_number_of_parties")
     * .withDescription("Number of parties currently running")
     * .build();
     * 
     * public static final Metadata currentQueuedPlayersCounter = new MetadataBuilder()
     * .withName("current_num_of_players_in_queue")
     * .withDescription("Number of players that are currently waiting in a queue")
     * .build();
     * 
     * public static final Metadata openWebsocketTimerMetadata = new MetadataBuilder()
     * .withName("open_game_websocket_timer")
     * .withDescription("The Time Game Round Websockets Are Open")
     * .withUnit(MetricUnits.SECONDS)
     * .build();
     * 
     * private static MetricRegistry registry;
     * 
     * private static MetricRegistry getRegistry() {
     * try {
     * registry = CDI.current().select(MetricRegistry.class).get();
     * System.out.println("MetricRegistry configured");
     * return registry;
     * } catch (IllegalStateException ise) {
     * System.out.println("WARNING: Unable to locate CDIProvider");
     * ise.printStackTrace();
     * }
     * return null;
     * }
     * 
     * /*public static Context timerStart(Metadata metricMetadata) {
     * if (registry != null || (getRegistry() != null)) {
     * return registry.timer(metricMetadata).time();
     * }
     * return null;
     * }
     */
}
