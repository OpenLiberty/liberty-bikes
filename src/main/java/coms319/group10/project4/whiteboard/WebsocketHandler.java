package coms319.group10.project4.whiteboard;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/websocket")
public class WebsocketHandler {

    private static final ConcurrentMap<Session, Player> clients = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {}

    @OnClose
    public void onClose(Session peer) {
        Game game = clients.get(peer).game;
        game.removePlayer(clients.get(peer));
        clients.remove(peer);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException, EncodeException {
        System.out.println("got message: " + message);
        JsonObject json = Json.createReader(new StringReader(message)).readObject();

        if (json.containsKey("message")) {
            if (json.getString("message").equals("GAME_START")) {
                clients.get(session).game.startGame();
            }
            if (json.getString("message").equals("GAME_PAUSE")) {
                clients.get(session).game.pause();
                System.out.println("Stopped the game");
            }
            if (json.getString("message").equals("GAME_REQUEUE")) {
                Player p = clients.get(session);
                Game.getUnstartedGame();
                JsonObject obj = Json.createObjectBuilder().add("requeue", "requeue").build();
                p.sendTextToClient(obj.toString());
            }
        }
        else if (json.containsKey("direction")) {
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
        }
        else if (json.containsKey("playerjoined")) {
            Game game = Game.getUnstartedGame();
            Player p = PlayerFactory.initNextPlayer(game, session, json.getString("playerjoined"));
            clients.put(session, p);
            game.addPlayer(p);
        }
    }
}
