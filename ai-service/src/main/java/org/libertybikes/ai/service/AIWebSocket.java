/**
 *
 */
package org.libertybikes.ai.service;

import java.io.IOException;
import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.libertybikes.ai.AIClass;

// how to join a game currently: http://localhost:8083/ai-service/open/join/{partyID}

@ClientEndpoint
public class AIWebSocket {

    // TODO: obviously shouldn't be hard coded
    private static String uri = "ws://localhost:8080" + "/round/ws/";

    public Session session;

    public enum DIRECTION {
        UP, DOWN, LEFT, RIGHT
    };

    AIClass AI;

    public AIWebSocket(String roundId) {
        System.out.println("Initializing WebSocket with round " + roundId);

        try {

            // Connect to LibertyBikes GameRoundWebsocket
            URI endpointURI = new URI(uri + roundId);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);

            // Tell the websocket we are joining, TODO: don't want to hardcode bender
            String dataai = "{\"playerjoined\":\"BOT:Bender\"}";
            sendMessage(dataai);

            // BOT TODO: parse the "playerlist" Json we get back to determine spot position
            AI = new AIClass(this, 0, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Opened Websocket");
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Close Websocket");
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        System.out.println("On Message: " + message);

        /*
         * TODO: Describe the JSON we get back:
         */
        AI.processAiMove(message);
    }

    public void sendDirection(DIRECTION dir) {
        switch (dir) {
            case RIGHT:
                sendMessage("{\"direction\":\"RIGHT\"}");
                break;
            case DOWN:
                sendMessage("{\"direction\":\"DOWN\"}");
                break;
            case LEFT:
                sendMessage("{\"direction\":\"LEFT\"}");
                break;
            default:
                sendMessage("{\"direction\":\"UP\"}");
                break;
        }
    }

    public void sendMessage(String message) {
        this.session.getAsyncRemote().sendText(message);
    }

}
