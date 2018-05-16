package org.libertybikes.game.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.junit.Test;
import org.libertybikes.game.core.InboundMessage.GameEvent;
import org.libertybikes.game.core.OutboundMessage.PlayerList;
import org.libertybikes.game.core.OutboundMessage.RequeueGame;
import org.libertybikes.game.core.OutboundMessage.StartingCountdown;

public class JsonDataTest {

    private static final Jsonb jsonb = JsonbBuilder.create();

    @Test
    public void testPlayerJoined() {
        InboundMessage playerJoined = new InboundMessage();
        playerJoined.playerJoinedId = "1234";
        assertEquals("{\"playerjoined\":\"1234\"}", jsonb.toJson(playerJoined));
    }

    @Test
    public void testGameEventRequeue() {
        InboundMessage gameEvent = new InboundMessage();
        gameEvent.event = GameEvent.GAME_REQUEUE;
        assertEquals("{\"message\":\"GAME_REQUEUE\"}", jsonb.toJson(gameEvent));
    }

    @Test
    public void testGameEventStart() {
        InboundMessage gameEvent = new InboundMessage();
        gameEvent.event = GameEvent.GAME_START;
        assertEquals("{\"message\":\"GAME_START\"}", jsonb.toJson(gameEvent));
    }

    @Test
    public void testSpectatorJoined() {
        InboundMessage msg = new InboundMessage();
        msg.isSpectator = Boolean.FALSE;
        assertEquals("{\"spectatorjoined\":false}", jsonb.toJson(msg));

        msg.isSpectator = Boolean.TRUE;
        assertEquals("{\"spectatorjoined\":true}", jsonb.toJson(msg));
    }

    @Test
    public void testObstacle() {
        Obstacle o = new Obstacle(1, 2, 3, 4);
        assertEquals("{\"height\":2,\"width\":1,\"x\":3,\"y\":4}", jsonb.toJson(o));
    }

    @Test
    public void testGameBoard() {
        GameBoard board = new GameBoard(0);
        assertEquals("{\"movingObstacles\":[],\"obstacles\":[],\"players\":[]}", jsonb.toJson(board));

        String obstacleJson = "{\"height\":2,\"width\":1,\"x\":3,\"y\":4}";
        board.addObstacle(new Obstacle(1, 2, 3, 4));
        assertEquals("{\"movingObstacles\":[],\"obstacles\":[" + obstacleJson + "],\"players\":[]}", jsonb.toJson(board));

        String bobJson = "{\"id\":\"1234\",\"name\":\"Bob\",\"color\":\"" + Player.PLAYER_COLORS[0]
                         + "\",\"status\":\"Connected\",\"alive\":true,\"x\":9,\"y\":9,\"width\":3,\"height\":3,\"direction\":\"RIGHT\"}";
        board.addPlayer("1234", "Bob");
        assertEquals("{\"movingObstacles\":[],\"obstacles\":[" + obstacleJson + "],\"players\":[" + bobJson + "]}",
                     jsonb.toJson(board));

        board.addObstacle(new MovingObstacle(11, 12, 13, 14, -1, 2, 50));
        assertEquals("{\"movingObstacles\":[{\"height\":12,\"width\":11,\"x\":13,\"y\":14}],\"obstacles\":["
                     + obstacleJson + "],\"players\":[" + bobJson + "]}",
                     jsonb.toJson(board));
    }

    @Test
    public void testGameRound() {
        GameRound round = new GameRound("ABCDEF");
        System.out.println(jsonb.toJson(round));
        assertContains("{\"id\":\"ABCDEF\",\"gameState\":\"OPEN\",\"board\":{", jsonb.toJson(round));
        assertContains("nextRoundId\":\"", jsonb.toJson(round));
    }

    @Test
    public void testBindPlayer() {
        String playerSvcResponse = "{\"id\":\"112233\",\"name\":\"andy\",\"stats\":{\"totalGames\":0,\"numWins\":0}}";
        org.libertybikes.restclient.Player p = jsonb.fromJson(playerSvcResponse, org.libertybikes.restclient.Player.class);
        assertEquals("112233", p.id);
        assertEquals("andy", p.name);
    }

    @Test
    public void testPlayerList() {
        Set<Player> players = new LinkedHashSet<>();
        Player p = new Player("123", "Bob", (short) 0);
        p.x = 9;
        p.y = 9;
        p.direction = DIRECTION.RIGHT;
        players.add(p);
        PlayerList list = new OutboundMessage.PlayerList(players);
        String bob = "{\"id\":\"123\",\"name\":\"Bob\",\"color\":\"" + Player.PLAYER_COLORS[0]
                     + "\",\"status\":\"Connected\",\"alive\":true,\"x\":9,\"y\":9,\"width\":3,\"height\":3,\"direction\":\"RIGHT\"}";
        String bot1 = "{\"id\":\"\",\"name\":\"Bot Player\",\"color\":\"" + Player.PLAYER_COLORS[1]
                      + "\",\"status\":\"Connected\",\"alive\":true,\"x\":0,\"y\":0,\"width\":3,\"height\":3}";
        String bot2 = "{\"id\":\"\",\"name\":\"Bot Player\",\"color\":\"" + Player.PLAYER_COLORS[2]
                      + "\",\"status\":\"Connected\",\"alive\":true,\"x\":0,\"y\":0,\"width\":3,\"height\":3}";
        String bot3 = "{\"id\":\"\",\"name\":\"Bot Player\",\"color\":\"" + Player.PLAYER_COLORS[3]
                      + "\",\"status\":\"Connected\",\"alive\":true,\"x\":0,\"y\":0,\"width\":3,\"height\":3}";
        System.out.println("@AGG " + jsonb.toJson(list));
        assertEquals("{\"playerlist\":[" + bob + "," + bot1 + "," + bot2 + "," + bot3 + "]}",
                     jsonb.toJson(list));

        String chuck = "{\"id\":\"456\",\"name\":\"Chuck\",\"color\":\"" + Player.PLAYER_COLORS[2]
                       + "\",\"status\":\"Connected\",\"alive\":true,\"x\":9,\"y\":110,\"width\":3,\"height\":3,\"direction\":\"UP\"}";
        Player p2 = new Player("456", "Chuck", (short) 2);
        p2.x = 9;
        p2.y = 110;
        p2.direction = DIRECTION.UP;
        players.add(p2);
        list = new OutboundMessage.PlayerList(players);
        assertEquals("{\"playerlist\":[" + bob + "," + bot1 + "," + chuck + "," + bot3 + "]}",
                     jsonb.toJson(list));
    }

    @Test
    public void testRequeue() {
        RequeueGame req = new OutboundMessage.RequeueGame("1234");
        assertEquals("{\"requeue\":\"1234\"}", jsonb.toJson(req));
    }

    @Test
    public void testCountdown() {
        StartingCountdown countdown = new OutboundMessage.StartingCountdown(5);
        assertEquals("{\"countdown\":5}", jsonb.toJson(countdown));
    }

    private void assertContains(String expected, String search) {
        assertTrue("Did not find '" + expected + "' inside of the string: " + search, search.contains(expected));
    }

}
