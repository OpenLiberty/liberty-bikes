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
import org.eclipse.microprofile.metrics.annotation.Timed;

@Path("/")
@ApplicationScoped
public class GameMetrics {
    // MpMetric Metadatas

    private int totalCurrentPlayers = 0;
    private int totalCurrentMobilePlayers = 0;
    private int totalPlayers = 0;
    private int currentRounds = 0;

    @Counted(unit = MetricUnits.NONE,
             name = "roundNumberCounter",
             absolute = true,
             description = "Number of total rounds played")
    public void totalRoundsCounter() {
        return;
    }

    @Gauge(unit = MetricUnits.NONE,
           name = "currentRoundNumberCounter",
           absolute = true,
           description = "Number of rounds currently running")
    public int getCurrentRoundsCounter() {
        return currentRounds;
    }

    @Gauge(unit = MetricUnits.NONE,
           name = "playerNumberGauge",
           absolute = true,
           description = "Number of players in the game")
    public int getCurrentPlayerCount() {
        System.out.println("Total players (updated again and again) " + totalCurrentPlayers);
        return totalCurrentPlayers;
    }

    @Gauge(unit = MetricUnits.NONE,
           name = "mobilePlayerNumberGauge",
           absolute = true,
           description = "Number of mobile players in the game")
    public int getMobilePlayerCount() {
        return totalCurrentMobilePlayers;
    }

    @Counted(unit = MetricUnits.NONE,
             name = "totalPlayerNumberGauge",
             absolute = true,
             description = "Number of players that have played in a round, requeuing and replaying increases the count")
    public int getTotalPlayerCount() {
        System.out.println("Total players (updated again and again) " + totalPlayers);
        return totalPlayers;
    }

    @Timed(unit = MetricUnits.SECONDS,
           name = "timer",
           absolute = true,
           description = "The time an app has been running")
    public void timer() {
        return;
    }

    public void incPlayerCount() {
        System.out.println("Increasing player count");
        totalCurrentPlayers = getCurrentPlayerCount() + 1;
        System.out.println("Increasing player count");
    }

    public void decPlayerCount() {
        totalCurrentPlayers = getCurrentPlayerCount() - 1;
    }

    public void incMobilePlayerCount() {
        totalCurrentMobilePlayers = totalCurrentMobilePlayers + 1;
    }

    public void decMobilePlayerCount() {
        totalCurrentMobilePlayers = totalCurrentMobilePlayers - 1;
    }

    public void incRoundCounter() {
        currentRounds = currentRounds + 1;
    }

    public void decRoundCounter() {
        currentRounds = currentRounds - 1;
    }

    // public static Context timerStart(Metadata metricMetadata) {
    //     if (registry != null || (getRegistry() != null)) {
    //         return registry.timer(metricMetadata).time();
    //     }
    //     return null;
    // }

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

}
