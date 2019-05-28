package org.libertybikes.game.party;

import java.util.Random;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.bind.annotation.JsonbTransient;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.libertybikes.game.core.GameRound;
import org.libertybikes.game.core.GameRound.LifecycleCallback;
import org.libertybikes.game.round.service.GameRoundService;

@Dependent
public class Party {

    private static final Random r = new Random();
    // Chars that will be used to generate party IDs (A-Z minus most commonly used chars in words)
    private static final char[] SAFE_CHARS = "BCDGHJKMPQRTVWXY".toCharArray();

    @Inject
    @JsonbTransient
    GameRoundService roundService;

    public final String id;
    private final PartyQueue queue = new PartyQueue(this);
    private volatile GameRound currentRound;

    Metadata currentPartiesCounterMetadata = new Metadata("current_number_of_parties", // name
                    "Current Number of Parties", // display name
                    "Number of parties currently running", // description
                    MetricType.COUNTER, // type
                    MetricUnits.NONE); // units

    @Inject
    private MetricRegistry registry;

    private Counter currentPartiesCounter;

    @PostConstruct
    public void postConstruct() {
        currentPartiesCounter = registry.counter(currentPartiesCounterMetadata);
        currentPartiesCounter.inc();
    }

    @PreDestroy
    public void preDestroy() {
        currentPartiesCounter.dec();
    }

    @Inject
    @Counted(unit = MetricUnits.NONE,
             name = "number_of_parties",
             monotonic = true,
             description = "Total Number of Parties",
             absolute = true)
    public Party() {
        this(getRandomPartyID());
    }

    public Party(String id) {
        this.id = id;
    }

    public GameRound getCurrentRound() {
        if (currentRound == null) {
            currentRound = roundService.getRound(roundService.createRound());
            installCallback(currentRound);
        }
        return this.currentRound;
    }

    public void enqueueClient(String playerId, SseEventSink sink, Sse sse) {
        queue.add(playerId, sink, sse);
    }

    public void close() {
        queue.close();
    }

    public void log(String msg) {
        System.out.println("[Party-" + id + "]  " + msg);
    }

    // Installs a callback on the GameRound that updates this party's current round
    private void installCallback(GameRound round) {
        log("Install callback for round id=" + round.id);
        LifecycleCallback callback = new LifecycleCallback() {
            @Override
            public void gameEnding() {
                log("Updating current round from " + round.id + " -> " + round.nextRoundId);
                currentRound = roundService.createRoundById(round.nextRoundId);
                Party.this.installCallback(currentRound);

                log("Processing next members in queue...");
                queue.promoteClients();
            }
        };
        round.addCallback(callback);
    }

    // Get a string of 4 random letters
    private static String getRandomPartyID() {
        char[] chars = new char[4];
        for (int i = 0; i < 4; i++)
            chars[i] = SAFE_CHARS[r.nextInt(SAFE_CHARS.length)];
        return new String(chars);
    }
}
