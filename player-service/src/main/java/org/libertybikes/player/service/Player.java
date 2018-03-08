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
        return b.stats.numWins - a.stats.numWins;
    }

    public static double compareByWinRatio(Player a, Player b) {
        return b.stats.winLossRatio() - a.stats.winLossRatio();
    }

}
