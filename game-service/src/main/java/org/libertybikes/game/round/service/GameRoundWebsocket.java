/**
 *
 */
package org.libertybikes.game.round.service;

import java.io.IOException;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.libertybikes.game.core.GameRound;
import org.libertybikes.game.core.GameRound.State;
import org.libertybikes.game.core.InboundMessage;
import org.libertybikes.game.core.InboundMessage.GameEvent;
import org.libertybikes.restclient.PlayerService;

@Dependent
@ServerEndpoint("/round/ws/{roundId}")
public class GameRoundWebsocket {

    @Inject
    GameRoundService gameSvc;

    @Inject
    @RestClient
    PlayerService playerSvc;

    private final static Jsonb jsonb = JsonbBuilder.create();

    @OnOpen
    public void onOpen(@PathParam("roundId") String roundId, Session session) {
        log(roundId, "Opened a session");
        checkIdleTimeout(session, roundId);
    }

    @OnClose
    public void onClose(@PathParam("roundId") String roundId, Session session) {
        checkIdleTimeout(session, roundId);
        log(roundId, "Closed a session");
        try {
            GameRound round = gameSvc.getRound(roundId);
            if (round != null)
                if (round.removeClient(session) == 0)
                    gameSvc.deleteRound(round);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(@PathParam("roundId") final String roundId, String message, Session session) {
        checkIdleTimeout(session, roundId);
        try {
            final InboundMessage msg = jsonb.fromJson(message, InboundMessage.class);
            final GameRound round = gameSvc.getRound(roundId);
            if (round == null || round.gameState == State.FINISHED) {
                log(roundId, "[onMessage] Received message for round that did not exist or has completed.  Closing this websocket connection.");
                session.close();
                return;
            }
            // System.out.println("[onMessage] roundId=" + roundId + "  msg=" + message);

            if (GameEvent.GAME_START == msg.event) {
                round.startGame();
            } else if (msg.direction != null) {
                round.updatePlayerDirection(session, msg);
            } else if (msg.playerJoinedId != null) {
                org.libertybikes.restclient.Player playerResponse = playerSvc.getPlayerById(msg.playerJoinedId);
                round.addPlayer(session, msg.playerJoinedId, playerResponse.name, msg.hasGameBoard);
            } else if (Boolean.TRUE == msg.isSpectator) {
                round.addSpectator(session);
            } else {
                log(roundId, "ERR: Unrecognized message: " + jsonb.toJson(msg));
            }
        } catch (Exception e) {
            log(roundId, "ERR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sendToClient(Session client, Object message) {
        if (client != null) {
            String msg = message instanceof String ? (String) message : jsonb.toJson(message);
            try {
                client.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendToClients(Set<Session> clients, Object message) {
        String msg = message instanceof String ? (String) message : jsonb.toJson(message);
        // System.out.println("Sending " + clients.size() + " clients the message: " + message);
        for (Session client : clients)
            sendToClient(client, msg);
    }

    private static void log(String roundId, String msg) {
        System.out.println("[websocket-" + roundId + "]  " + msg);
    }

    // TODO: remove this method once we find out if session timeout is being altered on cloud env
    private static void checkIdleTimeout(Session session, String roundId) {
        // See if we can catch a reason why sessions timeout in cloud env
        if (session.getMaxIdleTimeout() > 0) {
            log(roundId, "WARNING: Session idle timeout is: " + session.getMaxIdleTimeout());
        }
        if (session.getContainer().getDefaultMaxSessionIdleTimeout() > 0) {
            log(roundId, "WARNING: Default session idle timeout is: " + session.getContainer().getDefaultMaxSessionIdleTimeout());
        }
    }

}
