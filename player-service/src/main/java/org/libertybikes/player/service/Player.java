package org.libertybikes.player.service;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;

public class Player {

    public static enum DOMAIN {
        BASIC,
        GMAIL,
        GITHUB,
        TWITTER;

        @Override
        public String toString() {
            return super.toString() + ':';
        }
    }

    private static final Jsonb jsonb = JsonbBuilder.create();

    public final String id;

    public final String name;

    public final PlayerStats stats;

    public Player(String name) {
        this(name, null);
    }

    @JsonbCreator
    public Player(@JsonbProperty("name") String name,
                  @JsonbProperty("id") String id) {
        this(name, id, new PlayerStats());
    }

    public Player(String name, String id, PlayerStats stats) {
        this.id = (id == null || id.equals("null")) ? createDefaultId(name) : id;
        this.name = name;
        this.stats = stats;
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

    public static String createDefaultId(String name) {
        return DOMAIN.BASIC + name;
    }

    @JsonbTransient
    public DOMAIN getDomain() {
        for (DOMAIN d : DOMAIN.values()) {
            if (id.startsWith(d.toString()))
                return d;
        }
        return DOMAIN.BASIC;
    }

    @Override
    public String toString() {
        return jsonb.toJson(this);
    }

}
