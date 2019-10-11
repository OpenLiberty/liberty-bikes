/**
 *
 */
package org.libertybikes.game.round.service;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.metrics.Timer.Context;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.libertybikes.game.core.GameRound;
import org.libertybikes.game.core.GameRound.State;
import org.libertybikes.game.core.InboundMessage;
import org.libertybikes.game.core.InboundMessage.GameEvent;
import org.libertybikes.game.core.OutboundMessage;
import org.libertybikes.game.metric.GameMetrics;
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

    private Context timerContext;

    @OnOpen
    public void onOpen(@PathParam("roundId") String roundId, Session session) {
        log(roundId, "Opened a session");
        session.setMaxTextMessageBufferSize(1000);
        session.setMaxBinaryMessageBufferSize(1000);
        session.setMaxIdleTimeout(90 * 1000);
        timerContext = GameMetrics.timerStart(GameMetrics.openWebsocketTimerMetadata);
    }

    @OnClose
    public void onClose(@PathParam("roundId") String roundId, Session session) {
        log(roundId, "Closed a session");

        if (timerContext != null)
            timerContext.close();

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
    @Metered(name = "rate_of_websocket_calls",
             displayName = "Rate of GameRound Websocket Calls",
             description = "Rate of incoming messages to the game round websocket",
             absolute = true)
    public void onMessage(@PathParam("roundId") final String roundId, String message, Session session) {
        try {
            final InboundMessage msg = jsonb.fromJson(message, InboundMessage.class);
            final GameRound round = gameSvc.getRound(roundId);
            if (round == null || round.getGameState() == State.FINISHED) {
                log(roundId, "[onMessage] Received message for round that did not exist or has completed.  Closing this websocket connection.");
                if (round == null)
                    closeWithError(session, roundId, "Round " + roundId + " did not exist.");
                // don't boot out players that may keep sending messages a few seconds after the game is done
                else if (!round.isPlayer(session))
                    closeWithError(session, roundId, "Round " + roundId + " has already completed.");
                return;
            }
            //log(roundId, "[onMessage] msg=" + message);

            if (GameEvent.GAME_START == msg.event) {
                round.startGame();
            } else if (msg.direction != null) {
                boolean updated = round.updatePlayerDirection(session, msg);
                if (!updated)
                    closeWithError(session, roundId, "Unable to update direction because client was not joined to round");
            } else if (msg.playerJoinedId != null && !msg.playerJoinedId.isEmpty()) {
                // Call playerserver for player in DB
                org.libertybikes.restclient.Player playerResponse = getPlayer(msg.playerJoinedId);
                if (playerResponse == null)
                    closeWithError(session, roundId, "Unable to add player " + msg.playerJoinedId +
                                                     " to game. This is probably because the player has not been registered yet");
                else if (!round.addPlayer(session, msg.playerJoinedId, playerResponse.name, msg.hasGameBoard == null ? true : msg.hasGameBoard))
                    closeWithError(session, roundId, "Unable to add player " + playerResponse.name
                                                     + " to game. This is probably because someone else with the same id is already in the game.");
            } else if (Boolean.TRUE == msg.isSpectator) {
                round.addSpectator(session);
            } else {
                closeWithError(session, roundId, jsonb.toJson(msg));
            }
        } catch (Exception e) {
            log(roundId, "ERR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void closeWithError(Session session, String roundId, String msg) throws IOException {
        log(roundId, msg);
        sendToClient(session, new OutboundMessage.ErrorEvent(msg));
        // close reason phrase cannot exceed 123 UTF-8 encoded bytes
        if (msg.length() > 123)
            msg = msg.substring(0, 122);
        session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION, msg));
    }

    public static void sendToClient(Session client, Object message) {
        if (client != null) {
            String msg = message instanceof String ? (String) message : jsonb.toJson(message);
            Future<Void> f = client.getAsyncRemote().sendText(msg);
            try {
                f.get(50, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
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

    // We need to specify an extra helper method here because MP Rest Client does not
    // yet allow MP Fault Tolerance annotations directly on rest client interfaces
    // see: https://github.com/eclipse/microprofile-rest-client/issues/5
    @Retry(maxRetries = 3)
    private org.libertybikes.restclient.Player getPlayer(String id) {
        return playerSvc.getPlayerById(id);
    }

}
