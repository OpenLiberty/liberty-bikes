package org.libertybikes.game.round.service;

import java.util.Random;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.bind.annotation.JsonbTransient;

import org.libertybikes.game.core.GameRound;

@Dependent
public class Party {

    private static final Random r = new Random();
    // Chars that will be used to generate party IDs (0-9 A-Z minus most commonly used chars in words)
    private static final char[] SAFE_CHARS = "346789BCDGHJKMPQRTVWXY".toCharArray();

    @Inject
    @JsonbTransient
    GameRoundService roundService;

    public final String id;

    private volatile GameRound currentRound;

    @Inject
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
        log("Current round id=" + currentRound.id);
        return this.currentRound;
    }

    // Get a string of 4 random letters
    private static String getRandomPartyID() {
        char[] chars = new char[4];
        for (int i = 0; i < 4; i++)
            chars[i] = SAFE_CHARS[r.nextInt(SAFE_CHARS.length)];
        return new String(chars);
    }

    private void log(String msg) {
        System.out.println("[Party-" + id + "]  " + msg);
    }

    // Installs a callback on the GameRound that updates this party's current round
    private void installCallback(GameRound round) {
        log("Install callback for round id=" + round.id);
        round.addCallback(() -> {
            log("Updating current round from " + round.id + " -> " + round.nextRoundId);
            currentRound = roundService.createRoundById(round.nextRoundId);
            this.installCallback(currentRound);
            return null;
        });
    }

}
