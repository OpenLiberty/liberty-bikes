package org.libertybikes.game.core;

import static org.libertybikes.game.round.service.GameRoundWebsocket.sendTextToClient;
import static org.libertybikes.game.round.service.GameRoundWebsocket.sendTextToClients;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.inject.spi.CDI;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.websocket.Session;

import org.libertybikes.game.core.ClientMessage.GameEvent;
import org.libertybikes.game.core.Player.STATUS;
import org.libertybikes.game.round.service.GameRoundService;
import org.libertybikes.game.round.service.GameRoundWebsocket;

@JsonbPropertyOrder({ "id", "gameState", "board", "nextRoundId" })
public class GameRound implements Runnable {

    public static enum State {
        OPEN, FULL, RUNNING, FINISHED
    }

    public static final Jsonb jsonb = JsonbBuilder.create();
    public static final int GAME_TICK_SPEED = 50; // ms
    private static final Random r = new Random();
    private static final AtomicInteger runningGames = new AtomicInteger();

    // Properties exposed in JSON representation of object
    public final String id;
    public final String nextRoundId;
    public State gameState = State.OPEN;
    private final GameBoard board = new GameBoard();

    private final AtomicBoolean gameRunning = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final Map<Session, Client> clients = new HashMap<>();
    private final boolean[] takenPlayerSlots = new boolean[PlayerFactory.MAX_PLAYERS];

    private int ticksWithoutMovement = 0;

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
        board.addObstacle(new Obstacle(5, 5, 60, 60));
        board.addObstacle(new MovingObstacle(5, 5, 80, 85, -1, -1, 2));
        board.addObstacle(new MovingObstacle(5, 5, 80, 95, 1, 1));
    }

    public GameBoard getBoard() {
        return board;
    }

    public void handleMessage(ClientMessage msg, Session session) {
        if (GameEvent.GAME_START == msg.event)
            startGame();

        if (msg.direction != null) {
            Client c = clients.get(session);
            if (c.isPlayer())
                c.player.setDirection(msg.direction);
        }

        if (msg.playerJoinedId != null) {
            addPlayer(session, msg.playerJoinedId);
        }

        if (Boolean.TRUE == msg.isSpectator) {
            addSpectator(session);
        }
    }

    public void addPlayer(Session s, String playerId) {
        // Front end should be preventing a player joining a full game but
        // defensive programming
        if (gameState != State.OPEN) {
            System.out.println("Cannot add player " + playerId + " to game " + id + " because game has already started.");
            return;
        }

        if (board.players.size() + 1 > PlayerFactory.MAX_PLAYERS - 1) {
            gameState = State.FULL;
        }

        // Find first open player slot to fill, which determines position
        int playerNum = -1;
        for (int i = 0; i < takenPlayerSlots.length; i++) {
            if (!takenPlayerSlots[i]) {
                playerNum = i;
                takenPlayerSlots[i] = true;
                System.out.println("Player slot " + i + " taken");
                break;
            }
        }

        // Initialize Player
        Player p = PlayerFactory.initNextPlayer(this, playerId, playerNum);
        board.addPlayer(p);
        clients.put(s, new Client(s, p));
        System.out.println("Player " + playerId + " has joined.");
        broadcastPlayerList();
        broadcastGameBoard();
    }

    public void addSpectator(Session s) {
        System.out.println("A spectator has joined.");
        clients.put(s, new Client(s));
        sendTextToClient(s, getPlayerList());
        sendTextToClient(s, jsonb.toJson(board));
    }

    private void removePlayer(Player p) {
        p.disconnect();
        System.out.println(p.playerName + " disconnected.");
        broadcastPlayerList();

        // Open player slot for new joiners
        if (State.FULL == gameState && board.players.size() - 1 < PlayerFactory.MAX_PLAYERS) {
            gameState = State.OPEN;
        }
        takenPlayerSlots[p.getPlayerNum()] = false;
    }

    public int removeClient(Session client) {
        Client c = clients.remove(client);
        if (c != null && c.player != null)
            removePlayer(c.player);
        return clients.size();
    }

    // @JsonbTransient // TODO re-enable this anno once Liberty upgrades to yasson 1.0.1
    public Set<Player> players() {
        return clients.values()
                        .stream()
                        .filter(c -> c.isPlayer())
                        .map(c -> c.player)
                        .collect(Collectors.toSet());
    }

    @Override
    public void run() {
        gameRunning.set(true);
        System.out.println("Starting round: " + id);
        int numGames = runningGames.incrementAndGet();
        if (numGames > 3)
            System.out.println("WARNING: There are currently " + numGames + " game instances running.");
        while (gameRunning.get()) {
            delay(GAME_TICK_SPEED);
            gameTick();
            if (ticksWithoutMovement > 5)
                gameRunning.set(false); // end the game if nobody can move anymore
        }
        runningGames.decrementAndGet();
        System.out.println("Finished round: " + id);

        System.out.println("Clients flagged for auto-requeue will be redirected to the next round in 5 seconds...");
        delay(5000);
        GameRoundService gameSvc = CDI.current().select(GameRoundService.class).get();
        for (Client c : clients.values())
            if (c.autoRequeue)
                GameRoundWebsocket.requeueClient(gameSvc, this, c.session);
    }

    private void gameTick() {
        if (gameState != State.RUNNING)
            return;

        boolean boardUpdated = board.moveObjects();

        // Move all living players forward 1
        boolean playerStatusChange = false;
        boolean playersMoved = false;
        for (Player p : players()) {
            if (p.isAlive) {
                if (p.movePlayer(board.board())) {
                    playersMoved = true;
                } else {
                    // Since someone died, check for winning player
                    checkForWinner(p);
                    playerStatusChange = true;
                }
            }
        }

        if (playersMoved || boardUpdated) {
            ticksWithoutMovement = 0;
            broadcastGameBoard();
        } else {
            ticksWithoutMovement++;
        }

        if (playerStatusChange)
            broadcastPlayerList();
    }

    private void delay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
        }
    }

    private String getPlayerList() {
        // TODO: Use JSON-B instead of JSON-P here
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (Player p : players()) {
            array.add(Json.createObjectBuilder()
                            .add("name", p.playerName)
                            .add("status", p.getStatus().toString())
                            .add("color", p.color));
        }
        return Json.createObjectBuilder().add("playerlist", array).build().toString();
    }

    private void broadcastGameBoard() {
        sendTextToClients(clients.keySet(), jsonb.toJson(board));
    }

    private void broadcastPlayerList() {
        sendTextToClients(clients.keySet(), getPlayerList());
    }

    private void checkForWinner(Player dead) {
        if (players().size() < 2) // 1 player game, no winner
            return;
        int alivePlayers = 0;
        Player alive = null;
        for (Player cur : players()) {
            if (cur.isAlive) {
                alivePlayers++;
                alive = cur;
            }
        }
        if (alivePlayers == 1) {
            alive.setStatus(STATUS.Winner);
            gameState = State.FINISHED;
        }
    }

    public void startGame() {
        paused.set(false);
        for (Player p : players())
            if (STATUS.Connected == p.getStatus())
                p.setStatus(STATUS.Alive);
        broadcastPlayerList();
        if (!gameRunning.get()) {
            try {
                ManagedScheduledExecutorService exec = InitialContext.doLookup("java:comp/DefaultManagedScheduledExecutorService");
                exec.submit(this);
            } catch (NamingException e) {
                System.out.println("Unable to start game due to: " + e);
                e.printStackTrace();
            }
        }
        gameState = State.RUNNING;
    }
}
