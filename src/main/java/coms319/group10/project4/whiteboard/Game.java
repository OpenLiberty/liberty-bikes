/**
 *
 */
package coms319.group10.project4.whiteboard;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import coms319.group10.project4.whiteboard.Player.STATUS;

/**
 * @author Andrew
 */
public class Game extends Thread
{
    private static Game instance = new Game();
    public static final int GAME_SIZE = 600;
    public static final int GAME_SPEED = 50;
    public boolean[][] board = new boolean[121][121];
    Set<Player> players = Collections.synchronizedSet(new HashSet<Player>());
    AtomicBoolean gameRunning = new AtomicBoolean(false);
    AtomicBoolean paused = new AtomicBoolean(false);

    private Game() {}

    public static Game getUnstartedGame() {
        if (!instance.gameRunning.get() && instance.players.size() < PlayerFactory.MAX_PLAYERS)
            return instance;
        else {
            instance = new Game();
            return instance;
        }
    }

    public void addPlayer(Player p) {
        players.add(p);
        System.out.println("Player " + players.size() + " has joined.");
        for (Player cur : players)
            broadcastLocation(this, cur);
        broadcastPlayerList(this, players);
    }

    public void removePlayer(Player p) {
        p.disconnect();
        System.out.println(p.playerName + " disconnected.");
        broadcastPlayerList(this, players);
    }

    @Override
    public void run() {
        for (int i = 0; i < GAME_SIZE / Player.PLAYER_SIZE + 1; i++) {
            Arrays.fill(board[i], true);
        }
        gameRunning.set(true);
        System.out.println("Game started");

        while (gameRunning.get()) {
            while (!paused.get()) {
                delay(GAME_SPEED);
                for (Player p : players) {
                    if (p.isAlive) {
                        if (p.movePlayer()) {
                            broadcastLocation(this, p);
                        } else {
                            // Since someone died, check for winning player
                            checkForWinner(p);
                            broadcastPlayerList(this, players);
                        }
                    }
                }
            }
            delay(500); // don't thrash for pausing
        }
    }

    private void delay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
        }
    }

    private void broadcastLocation(Game g, Player p) {
        String json = p.toJson();
        for (Player player : g.players)
            player.sendTextToClient(json);
    }

    private void broadcastPlayerList(Game g, Set<Player> players) {
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (Player p : players) {
            array.add(Json.createObjectBuilder()
                            .add("name", p.playerName)
                            .add("status", p.getStatus().toString())
                            .add("color", p.color));
        }
        JsonObject obj = Json.createObjectBuilder().add("playerlist", array).build();
        System.out.println("Playerlist: " + obj.toString());

        for (Player player : g.players)
            player.sendTextToClient(obj.toString());
    }

    private void checkForWinner(Player dead) {
        if (players.size() < 2) // 1 player game, no winner
            return;
        int alivePlayers = 0;
        Player alive = null;
        for (Player cur : players) {
            if (cur.isAlive) {
                alivePlayers++;
                alive = cur;
            }
        }
        if (alivePlayers == 1)
            alive.setStatus(STATUS.Winner);
    }

    public void startGame() {
        paused.set(false);
        for (Player p : players)
            if (STATUS.Connected == p.getStatus())
                p.setStatus(STATUS.Alive);
        broadcastPlayerList(this, players);
        if (!gameRunning.get())
            this.start();
    }

    public void pause() {
        paused.set(true);
    }

    public void stopGame() {
        gameRunning.set(false);
    }
}
