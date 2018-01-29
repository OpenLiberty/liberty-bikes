/**
 *
 */
package org.libertybikes.game.round.service;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.libertybikes.game.core.ClientMessage;
import org.libertybikes.game.core.ClientMessage.GameEvent;
import org.libertybikes.game.core.GameRound;
import org.libertybikes.game.core.Player;
import org.libertybikes.game.core.PlayerFactory;

@Dependent
@ServerEndpoint("/round/ws/{roundId}") //
public class GameRoundWebsocket {

    @Inject
    GameRoundService gameSvc;

    private final Jsonb jsonb = JsonbBuilder.create();

    private static final Map<Session, Player> clients = new HashMap<>();

    @OnOpen
    public void onOpen(@PathParam("roundId") String roundId, Session session) {
        System.out.println("@AGG opened a session for game: " + roundId + " using ws=" + this.toString());
    }

    @OnClose
    public void onClose(@PathParam("roundId") String roundId, Session peer) {
        System.out.println("@AGG closed a session for game: " + roundId);
        GameRound game = gameSvc.getRound(roundId);
        game.removePlayer(clients.get(peer));
        clients.remove(peer);
    }

    @OnMessage
    public void processMsg(@PathParam("roundId") final String roundId, String message, Session session) {
        System.out.println("roundId=" + roundId + "  msg=" + message);

        final ClientMessage msg = jsonb.fromJson(message, ClientMessage.class);
        final GameRound round = gameSvc.getRound(roundId);
        System.out.println("@AGG parsed msg: " + msg);

        if (msg.event != null) {
            if (GameEvent.GAME_START == msg.event)
                round.startGame();
            else if (GameEvent.GAME_PAUSE == msg.event)
                round.pause();
            else if (GameEvent.GAME_REQUEUE == msg.event) {
                Player p = clients.get(session);
                // TODO Game.getUnstartedGame();
                JsonObject obj = Json.createObjectBuilder().add("requeue", "requeue").build();
                p.sendTextToClient(obj.toString());
                System.out.println("@AGG TODO: requeue not implemented");
                throw new RuntimeException("Not yet implemented");
            }
        }

        if (msg.direction != null) {
            Player p = clients.get(session);
            Player.DIRECTION curDir = p.getDrirection();
            if (curDir != msg.direction) {
                p.setDirection(msg.direction);
            }
        }

        if (msg.playerJoinedId != null) {
            Player p = PlayerFactory.initNextPlayer(round, session, msg.playerJoinedId);
            clients.put(session, p);
            round.addPlayer(p);
        }
    }

}
