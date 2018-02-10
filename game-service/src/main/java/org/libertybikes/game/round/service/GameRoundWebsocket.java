/**
 *
 */
package org.libertybikes.game.round.service;

import java.io.IOException;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.Json;
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

@Dependent
@ServerEndpoint("/round/ws/{roundId}")
public class GameRoundWebsocket {

    @Inject
    GameRoundService gameSvc;

    private final Jsonb jsonb = JsonbBuilder.create();

    @OnOpen
    public void onOpen(@PathParam("roundId") String roundId, Session session) {
        System.out.println("Opened a session for game round: " + roundId);
    }

    @OnClose
    public void onClose(@PathParam("roundId") String roundId, Session peer) {
        System.out.println("Closed a session for game round: " + roundId);
        try {
            GameRound round = gameSvc.getRound(roundId);
            if (round != null)
                if (round.removeClient(peer) == 0)
                    gameSvc.deleteRound(roundId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(@PathParam("roundId") final String roundId, String message, Session session) {
        try {
            final ClientMessage msg = jsonb.fromJson(message, ClientMessage.class);
            final GameRound round = gameSvc.getRound(roundId);
            System.out.println("[onMessage] roundId=" + roundId + "  msg=" + message);

            if (msg.event != null && GameEvent.GAME_REQUEUE == msg.event) {
                requeueClient(gameSvc, round, session);
            } else {
                round.handleMessage(msg, session);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void requeueClient(GameRoundService gameSvc, GameRound oldRound, Session s) {
        GameRound nextGame = gameSvc.requeue(oldRound);
        String requeueMsg = Json.createObjectBuilder()
                        .add("requeue", nextGame.id)
                        .build()
                        .toString();
        sendTextToClient(s, requeueMsg);
        if (oldRound.removeClient(s) == 0)
            gameSvc.deleteRound(oldRound.id);
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
        System.out.println("Sending " + clients.size() + " clients the message: " + message);
        for (Session client : clients)
            sendTextToClient(client, message);
    }

}
