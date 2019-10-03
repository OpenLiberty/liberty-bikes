package org.libertybikes.player.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.libertybikes.player.service.RankingService.ratingChange;

import org.junit.Before;
import org.junit.Test;
import org.libertybikes.player.data.InMemPlayerDB;
import org.libertybikes.player.data.PlayerDB;

public class RankingServiceTest {

    final int initialRank = RankingService.RATING_INITIAL;

    PlayerService players;

    RankingService ranks;

    @Before
    public void beforeEach() {
        PlayerDB db = new InMemPlayerDB();
        players = new PlayerService();
        players.db = db;
        ranks = new RankingService();
        ranks.playerSvc = players;
        ranks.db = db;
    }

    @Test
    public void duplicateName() {
        assertEquals("123", players.createPlayer("Andy", "123"));
        assertEquals("456", players.createPlayer("Andy", "456"));
        ranks.recordGameInternal("123", 1);

        Player p1 = players.getPlayerById("123");
        assertNotNull(p1);
        assertEquals("Andy", p1.name);
        assertEquals(1, p1.stats.numWins);
        assertEquals(initialRank + ratingChange(1), p1.stats.rating);

        Player p2 = players.getPlayerById("456");
        assertNotNull(p2);
        assertEquals("Andy", p2.name);
        assertEquals(0, p2.stats.numWins);
        assertEquals(initialRank, p2.stats.rating);
    }

    @Test
    public void sameDomains() {
        assertEquals("BASIC:Andy", players.createPlayer("Andy", "BASIC:Andy"));
        ranks.recordGameInternal("BASIC:Andy", 1);
        assertEquals("BASIC:Andy", players.createPlayer("Andy", "BASIC:Andy"));

        Player p = players.getPlayerById("BASIC:Andy");
        assertNotNull(p);
        assertEquals("Andy", p.name);
        assertEquals(1, p.stats.numWins);
        assertEquals(initialRank + ratingChange(1), p.stats.rating);
    }

    @Test
    public void separateDomains() {
        assertEquals("BASIC:Andy", players.createPlayer("Andy", "BASIC:Andy"));
        ranks.recordGameInternal("BASIC:Andy", 1);
        assertEquals("GMAIL:Andy", players.createPlayer("Andy", "GMAIL:Andy"));
        ranks.recordGameInternal("GMAIL:Andy", 4);

        Player p1 = players.getPlayerById("BASIC:Andy");
        assertNotNull(p1);
        assertEquals("Andy", p1.name);
        assertEquals(1, p1.stats.numWins);
        assertEquals(initialRank + ratingChange(1), p1.stats.rating);

        Player p2 = players.getPlayerById("GMAIL:Andy");
        assertNotNull(p2);
        assertEquals("Andy", p2.name);
        assertEquals(0, p2.stats.numWins);
        assertEquals(initialRank + ratingChange(4), p2.stats.rating);
    }

    @Test
    public void multipleRatingChanges() {
        assertEquals("BASIC:Andy", players.createPlayer("Andy", "BASIC:Andy"));

        int expRating = initialRank;
        Player p = players.getPlayerById("BASIC:Andy");
        assertNotNull(p);
        assertEquals(0, p.stats.numWins);
        assertEquals(expRating, p.stats.rating);

        expRating += ratingChange(1);
        ranks.recordGameInternal("BASIC:Andy", 1);
        p = players.getPlayerById("BASIC:Andy");
        assertNotNull(p);
        assertEquals(1, p.stats.numWins);
        assertEquals(expRating, p.stats.rating);

        expRating += ratingChange(3);
        ranks.recordGameInternal("BASIC:Andy", 3);
        p = players.getPlayerById("BASIC:Andy");
        assertNotNull(p);
        assertEquals(1, p.stats.numWins);
        assertEquals(expRating, p.stats.rating);

        expRating += ratingChange(2);
        ranks.recordGameInternal("BASIC:Andy", 2);
        p = players.getPlayerById("BASIC:Andy");
        assertNotNull(p);
        assertEquals(1, p.stats.numWins);
        assertEquals(expRating, p.stats.rating);

        expRating += ratingChange(1);
        ranks.recordGameInternal("BASIC:Andy", 1);
        p = players.getPlayerById("BASIC:Andy");
        assertNotNull(p);
        assertEquals(2, p.stats.numWins);
        assertEquals(expRating, p.stats.rating);
    }

    @Test
    public void ratingChangeTest() {
        assertEquals(0, ratingChange(-1));
        assertEquals(0, ratingChange(0));
        assertEquals(28, ratingChange(1));
        assertEquals(14, ratingChange(2));
        assertEquals(-5, ratingChange(3));
        assertEquals(-12, ratingChange(4));
        assertEquals(0, ratingChange(5));
    }

}
