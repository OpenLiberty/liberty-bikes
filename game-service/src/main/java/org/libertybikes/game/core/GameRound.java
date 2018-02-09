package org.libertybikes.game.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.libertybikes.game.core.Player.STATUS;

public class GameRound implements Runnable {

    public static enum State {
        OPEN, FULL, RUNNING, FINISHED
    }

    public static final int GAME_TICK_SPEED = 50;
    public static final int GAME_SIZE = 600;

    private static final Random r = new Random();

    public final String id;
    public final String nextRoundId;

    public Set<Player> players = new HashSet<Player>();
    public State state = State.OPEN;

    private boolean[][] board = new boolean[121][121];
    private AtomicBoolean gameRunning = new AtomicBoolean(false);
    private AtomicBoolean paused = new AtomicBoolean(false);

    // Get a string of 6 random uppercase letters (A-Z)
    private static String getRandomId() {
        char[] chars = new char[6];
        for (int i = 0; i < 6; i++)
            chars[i] = (char) (r.nextInt(26) + 65);
        return new String(chars);
    }

    public GameRound() {
        this(getRandomId());
    }

    public GameRound(String id) {
        this.id = id;
        nextRoundId = getRandomId();
    }

    public void addPlayer(Player p) {
        players.add(p);
        System.out.println("Player " + players.size() + " has joined.");
        broadcastPlayerLocations();
        broadcastPlayerList();
    }

    public void removePlayer(Player p) {
        p.disconnect();
        System.out.println(p.playerName + " disconnected.");
        broadcastPlayerList();
    }

    @Override
    public void run() {
        for (int i = 0; i < GAME_SIZE / Player.PLAYER_SIZE + 1; i++)
            Arrays.fill(board[i], true);
        gameRunning.set(true);
        System.out.println("Starting round: " + id);

        while (gameRunning.get()) {
            while (!paused.get()) {
                delay(GAME_TICK_SPEED);
                gameTick();
            }
            delay(500); // don't thrash when game is paused
        }
        System.out.println("Finished round: " + id);
    }

    private void gameTick() {
        // Move all living players forward 1
        boolean playerStatusChange = false;
        boolean playersMoved = false;
        for (Player p : players) {
            if (p.isAlive) {
                if (p.movePlayer(board)) {
                    playersMoved = true;
                } else {
                    // Since someone died, check for winning player
                    checkForWinner(p);
                    playerStatusChange = true;
                }
            }
        }

        if (playersMoved)
            broadcastPlayerLocations();
        if (playerStatusChange)
            broadcastPlayerList();
    }

    private void delay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
        }
    }

    private void broadcastPlayerLocations() {
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (Player p : players)
            arr.add(p.toJson());
        String playerLocations = Json.createObjectBuilder().add("playerlocs", arr).build().toString();
        for (Player client : players)
            client.sendTextToClient(playerLocations);
    }

    private void broadcastPlayerList() {
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (Player p : players) {
            array.add(Json.createObjectBuilder()
                            .add("name", p.playerName)
                            .add("status", p.getStatus().toString())
                            .add("color", p.color));
        }
        JsonObject obj = Json.createObjectBuilder().add("playerlist", array).build();
        System.out.println("Playerlist: " + obj.toString());

        for (Player player : players)
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
        broadcastPlayerList();
        if (!gameRunning.get()) {
            try {
                ExecutorService exec = InitialContext.doLookup("java:comp/DefaultManagedExecutorService");
                exec.submit(this);
            } catch (NamingException e) {
                System.out.println("Unable to start game due to: " + e);
                e.printStackTrace();
            }
        }
    }

    public void pause() {
        paused.set(true);
    }

    public void stopGame() {
        gameRunning.set(false);
    }

}
