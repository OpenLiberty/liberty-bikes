package org.libertybikes.player.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.libertybikes.player.data.InMemPlayerDB;

public class PlayerServiceTest {

    PlayerService svc;

    @Before
    public void beforeEach() {
        svc = new PlayerService();
        svc.db = new InMemPlayerDB();
    }

    @Test
    public void trimPlayerNames() {
        assertEquals("123", svc.createPlayer("    Andy   ", "123"));
        Player p = svc.getPlayerById("123");
        assertNotNull(p);
        assertEquals("Andy", p.name);
    }

    @Test
    public void removeInvalidChars() {
        assertEquals("123", svc.createPlayer("<h1>  Andy  </h1>  ", "123"));
        Player p = svc.getPlayerById("123");
        assertNotNull(p);
        assertEquals("h1  Andy  h1", p.name);
    }

    @Test
    public void truncateNames() {
        // Create with a name that is >20 chars, should get truncated to 20 chars
        assertEquals("123", svc.createPlayer("12345678901234567890123", "123"));
        Player p = svc.getPlayerById("123");
        assertNotNull(p);
        assertEquals("12345678901234567890", p.name);
    }

    @Test
    public void validName() {
        assertEquals("123", svc.createPlayer("Andy McAndy-Face", "123"));
        Player p = svc.getPlayerById("123");
        assertNotNull(p);
        assertEquals("Andy McAndy-Face", p.name);
    }
}
