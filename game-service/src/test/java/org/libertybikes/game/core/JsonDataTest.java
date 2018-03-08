/**
 *
 */
package org.libertybikes.game.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.junit.Test;
import org.libertybikes.game.core.ClientMessage.GameEvent;

public class JsonDataTest {

    private static final Jsonb jsonb = JsonbBuilder.create();

    @Test
    public void testPlayerJoined() {
        ClientMessage playerJoined = new ClientMessage();
        playerJoined.playerJoinedId = "1234";
        assertEquals("{\"playerjoined\":\"1234\"}", jsonb.toJson(playerJoined));
    }

    @Test
    public void testGameEventRequeue() {
        ClientMessage gameEvent = new ClientMessage();
        gameEvent.event = GameEvent.GAME_REQUEUE;
        assertEquals("{\"message\":\"GAME_REQUEUE\"}", jsonb.toJson(gameEvent));
    }

    @Test
    public void testGameEventStart() {
        ClientMessage gameEvent = new ClientMessage();
        gameEvent.event = GameEvent.GAME_START;
        assertEquals("{\"message\":\"GAME_START\"}", jsonb.toJson(gameEvent));
    }

    @Test
    public void testSpectatorJoined() {
        ClientMessage msg = new ClientMessage();
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
        GameBoard board = new GameBoard();
        assertEquals("{\"movingObstacles\":[],\"obstacles\":[],\"players\":[]}", jsonb.toJson(board));

        board.addObstacle(new Obstacle(1, 2, 3, 4));
        assertEquals("{\"movingObstacles\":[],\"obstacles\":[{\"height\":2,\"width\":1,\"x\":3,\"y\":4}],\"players\":[]}", jsonb.toJson(board));

        board.addPlayer(new Player("#1234", 10, 11, 0));
        assertEquals("{\"movingObstacles\":[],\"obstacles\":[{\"height\":2,\"width\":1,\"x\":3,\"y\":4}],\"players\":[{\"color\":\"#1234\",\"direction\":\"RIGHT\",\"isAlive\":true,\"playerNum\":0,\"status\":\"Connected\",\"x\":10,\"y\":11}]}",
                     jsonb.toJson(board));
        
        board.addObstacle(new MovingObstacle(11,12,13,14,-1,2,50));
        assertEquals("{\"movingObstacles\":[{\"height\":12,\"width\":11,\"x\":13,\"y\":14,\"currentDelay\":0,\"hasMoved\":false,\"moveDelay\":50,\"oldX\":0,\"oldY\":0,\"xDir\":-1,\"yDir\":2}],\"obstacles\":[{\"height\":2,\"width\":1,\"x\":3,\"y\":4}],\"players\":[{\"color\":\"#1234\",\"direction\":\"RIGHT\",\"isAlive\":true,\"playerNum\":0,\"status\":\"Connected\",\"x\":10,\"y\":11}]}",
                     jsonb.toJson(board));
    }

    @Test
    public void testGameRound() {
        GameRound round = new GameRound("ABCDEF");
        System.out.println(jsonb.toJson(round));
        assertContains("{\"id\":\"ABCDEF\",\"gameState\":\"OPEN\",\"board\":{", jsonb.toJson(round));
        assertContains("nextRoundId\":\"", jsonb.toJson(round));
    }

    private void assertContains(String expected, String search) {
        assertTrue("Did not find '" + expected + "' inside of the string: " + search, search.contains(expected));
    }

}
