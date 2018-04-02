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
import org.libertybikes.game.core.InboundMessage;
import org.libertybikes.game.core.InboundMessage.GameEvent;
import org.libertybikes.game.core.OutboundMessage;
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
                    gameSvc.deleteRound(roundId);
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
            if (round == null) {
                log(roundId, "[onMessage] unable to locate roundId=" + roundId);
                return;
            }
            // System.out.println("[onMessage] roundId=" + roundId + "  msg=" + message);

            if (GameEvent.GAME_REQUEUE == msg.event) {
                requeueClient(gameSvc, round, session);
            }
            if (GameEvent.GAME_START == msg.event) {
                round.startGame();
            }
            if (msg.direction != null) {
                round.updatePlayerDirection(session, msg);
            }
            if (msg.playerJoinedId != null) {
                org.libertybikes.restclient.Player playerResponse = playerSvc.getPlayerById(msg.playerJoinedId);
                round.addPlayer(session, msg.playerJoinedId, playerResponse.name, msg.hasGameBoard);
            }
            if (Boolean.TRUE == msg.isSpectator) {
                round.addSpectator(session);
            }
        } catch (Exception e) {
            log(roundId, "ERR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void requeueClient(GameRoundService gameSvc, GameRound oldRound, Session s) {
        GameRound nextGame = gameSvc.requeue(oldRound, oldRound.isPlayer(s));
        if (nextGame == null)
            return;
        String requeueMsg = jsonb.toJson(new OutboundMessage.RequeueGame(nextGame.id));
        sendTextToClient(s, requeueMsg);
    }

    public static void sendTextToClient(Session client, String message) {
        if (client != null) {
            try {
                client.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendTextToClients(Set<Session> clients, String message) {
        // System.out.println("Sending " + clients.size() + " clients the message: " + message);
        for (Session client : clients)
            sendTextToClient(client, message);
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
