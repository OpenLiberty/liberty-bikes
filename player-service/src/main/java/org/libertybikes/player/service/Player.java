package org.libertybikes.player.service;

import java.util.UUID;

import javax.json.bind.annotation.JsonbCreator;

public class Player {

    public final String id;

    public final String name;

    public final PlayerStats stats = new PlayerStats();

    public Player(String name) {
        this(name, UUID.randomUUID().toString());
    }

    @JsonbCreator
    public Player(String name, String id) {
        this.id = id;
        this.name = name;
    }

    public static int compareByWins(Player a, Player b) {
        return Integer.compare(b.stats.numWins, a.stats.numWins);
    }

    public static int compareByWinRatio(Player a, Player b) {
        return Double.compare(b.stats.winLossRatio(), a.stats.winLossRatio());
    }

    public static int compareOverall(Player a, Player b) {
        int wins = compareByWins(a, b);
        if (wins != 0)
            return wins;
        return compareByWinRatio(a, b);
    }

}
