package org.libertybikes.player.service;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.Collection;

import javax.ws.rs.NotAuthorizedException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.openliberty.testcontainers.LibertyContainer;

public class PlayerTest {

    static Logger LOGGER = LoggerFactory.getLogger(PlayerTest.class);

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

    @Test(expected = NotAuthorizedException.class)
    public void testUnsecuredRankUpdateRejected() {
        String id = playerSvc.createPlayer("SneakyUser", null);
        rankingService.recordGame(id, 1, "bogus");
    }

    @Test(expected = NotAuthorizedException.class)
    public void testBadJWTRankUpdate() throws Exception {
        String id = playerSvc.createPlayer("BadJWTUser", null);
        rankingService.recordGame(id, 1, "Bearer BOGUS123BOGUS");
    }

    @Test
    public void testLegitRankUpdate() throws Exception {
        String id = playerSvc.createPlayer("LegitUser", null);
        rankingService.recordGame(id, 1, createJWT(id));
    }

    @Test
    public void testRankUpdates() throws Exception {
        String id = playerSvc.createPlayer("TheGOAT", null);
        Player theGOAT = playerSvc.getPlayerById(id);
        assertEquals(RankingService.RATING_INITIAL, theGOAT.stats.rating);

        String jwt = createJWT(id);
        for (int i = 0; i < 10; i++)
            rankingService.recordGame(id, 1, jwt);

        theGOAT = playerSvc.getPlayerById(id);
        assertEquals(theGOAT.toString(),
                     RankingService.RATING_INITIAL + (RankingService.ratingChange(1) * 10),
                     theGOAT.stats.rating);

        assertEquals(1, rankingService.getRank(id));
    }

    // TODO: Creating a simple JWT to test with is a big pain... see if we can streamline this a bit
    private String createJWT(String userID) throws Exception {
        KeyStore signingKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
        signingKeystore.load(new FileInputStream(System.getProperty("user.dir") + "/src/integrationTest/resources/testKeys.jks"),
                             "secret".toCharArray());
        Key signingKey = signingKeystore.getKey("bike", "secret".toCharArray());

        Claims onwardsClaims = Jwts.claims();

        onwardsClaims.put("upn", "game-service");
        onwardsClaims.put("groups", "admin");
        // Set the subject using the "id" field from our claims map.
        onwardsClaims.setSubject(userID);
        onwardsClaims.setId(userID);

        // We'll use this claim to know this is a user token
        onwardsClaims.setAudience("client");

        onwardsClaims.setIssuer("https://libertybikes.mybluemix.net");
        // we set creation time to 24hrs ago, to avoid timezone issues in the
        // browser verification of the jwt.
        Calendar calendar1 = Calendar.getInstance();
        calendar1.add(Calendar.HOUR, -24);
        onwardsClaims.setIssuedAt(calendar1.getTime());

        // client JWT has 24 hrs validity from now.
        Calendar calendar2 = Calendar.getInstance();
        calendar2.add(Calendar.HOUR, 48);
        onwardsClaims.setExpiration(calendar2.getTime());

        // finally build the new jwt, using the claims we just built, signing it
        // with our signing key, and adding a key hint as kid to the encryption header,
        // which is optional, but can be used by the receivers of the jwt to know which
        // key they should verify it with.
        String jwt = Jwts.builder()
                        .setHeaderParam("kid", "rebike")
                        .setHeaderParam("alg", "RS256")
                        .setClaims(onwardsClaims)
                        .signWith(SignatureAlgorithm.RS256, signingKey)
                        .compact();
        return "Bearer " + jwt;
    }
}