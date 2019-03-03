package org.libertybikes.player.service;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import io.openliberty.testcontainers.LibertyContainer;

public class PlayerTest {

    static Logger LOGGER = LoggerFactory.getLogger(PlayerTest.class);

    static {
        BasicConfigurator.configure();
    }

    @ClassRule
    public static LibertyContainer playerContainer = new LibertyContainer("libertybikes-player")
                    .withExposedPorts(8081)
                    .waitForMPHealth();

    @Rule
    public TestName testName = new TestName();

    private static PlayerService playerSvc;
    private static RankingService rankingService;

    @BeforeClass
    public static void setupClass() {
        playerContainer.followOutput(new Slf4jLogConsumer(LOGGER));
        playerSvc = playerContainer.createRestClient(PlayerService.class);
        rankingService = playerContainer.createRestClient(RankingService.class);
    }

    @Before
    public void setUp() {
        System.out.println("BEGIN TEST: " + testName.getMethodName());
    }

    @Test
    public void testCreatePlayer() throws Exception {
        String createdID = playerSvc.createPlayer("Andy", null);
        assertEquals("BASIC:Andy", createdID);
    }

    @Test
    public void testGetPlayer() throws Exception {
        playerSvc.createPlayer("Andy", null);
        String bobID = playerSvc.createPlayer("Bob", null);
        playerSvc.createPlayer("Chuck", null);

        Player bob = playerSvc.getPlayerById(bobID);
        assertEquals("BASIC:Bob", bob.id);
        assertEquals("Bob", bob.name);
    }

    @Test
    public void testRankDefaultAmount() throws Exception {
        Collection<Player> top5Players = rankingService.topNPlayers(5);
        assertEquals(5, top5Players.size());
    }

    @Test
    public void testRankCustomAmount() throws Exception {
        Collection<Player> top10Players = rankingService.topNPlayers(10);
        assertEquals(10, top10Players.size());
    }
}