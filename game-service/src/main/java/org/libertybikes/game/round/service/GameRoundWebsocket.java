/**
 *
 */
package org.libertybikes.game.round.service;

import java.io.StringReader;
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
        JsonObject json = Json.createReader(new StringReader(message)).readObject();
        ClientMessage msg = jsonb.fromJson(message, ClientMessage.class);
        System.out.println("@AGG got jsonb msg: " + msg);
        final GameRound round = gameSvc.getRound(roundId);

        if (json.containsKey("message")) {
            if (json.getString("message").equals("GAME_START")) {
                round.startGame();
            }
            if (json.getString("message").equals("GAME_PAUSE")) {
                round.pause();
                System.out.println("Stopped the game");
            }
            if (json.getString("message").equals("GAME_REQUEUE")) {
                Player p = clients.get(session);
                // TODO Game.getUnstartedGame();
                JsonObject obj = Json.createObjectBuilder().add("requeue", "requeue").build();
                p.sendTextToClient(obj.toString());
                System.out.println("@AGG TODO: requeue not implemented");
                throw new RuntimeException("Not yet implemented");
            }
        } else if (json.containsKey("direction")) {
            System.out.println("@AGG got direction: " + json.getString("direction"));
            // TODO movement not working
            Player p = clients.get(session);
            Player.DIRECTION curDir = p.getDrirection();
            String noGo;
            if (curDir == Player.DIRECTION.UP) {
                noGo = "down";
            } else if (curDir == Player.DIRECTION.RIGHT) {
                noGo = "left";
            } else if (curDir == Player.DIRECTION.DOWN) {
                noGo = "up";
            } else {
                noGo = "right";
            }

            if (!noGo.equalsIgnoreCase(json.getString("direction"))) {
                p.setDirection(json.getString("direction"));
            }
        } else if (json.containsKey("playerjoined")) {
            Player p = PlayerFactory.initNextPlayer(round, session, json.getString("playerjoined"));
            clients.put(session, p);
            round.addPlayer(p);
        }
    }

}
