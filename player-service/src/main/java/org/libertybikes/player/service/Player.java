package org.libertybikes.player.service;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbTransient;

public class Player {

    public static enum DOMAIN {
        BASIC,
        GMAIL;

        @Override
        public String toString() {
            return super.toString() + ':';
        }
    }

    private static final Jsonb jsonb = JsonbBuilder.create();

    public final String id;

    public final String name;

    public final PlayerStats stats = new PlayerStats();

    public Player(String name) {
        this(name, null);
    }

    @JsonbCreator
    public Player(String name, String id) {
        this.id = id == null ? DOMAIN.BASIC + name : id;
        this.name = name;
    }

    public static int compareByWins(Player a, Player b) {
        return Integer.compare(b.stats.numWins, a.stats.numWins);
    }

    public static int compareByWinRatio(Player a, Player b) {
        return Double.compare(b.stats.winLossRatio(), a.stats.winLossRatio());
    }

    public static int compareByRating(Player a, Player b) {
        return Integer.compare(b.stats.rating, a.stats.rating);
    }

    public static int compareOverall(Player a, Player b) {
        int rating = compareByRating(a, b);
        if (rating != 0)
            return rating;
        int wins = compareByWins(a, b);
        if (wins != 0)
            return wins;
        return compareByWinRatio(a, b);
    }

    @JsonbTransient
    public DOMAIN getDomain() {
        if (id.startsWith(DOMAIN.GMAIL.toString()))
            return DOMAIN.GMAIL;
        else
            return DOMAIN.BASIC;
    }

    @Override
    public String toString() {
        return jsonb.toJson(this);
    }

}
