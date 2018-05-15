package org.libertybikes.game.core;

import static org.libertybikes.game.round.service.GameRoundWebsocket.sendToClient;
import static org.libertybikes.game.round.service.GameRoundWebsocket.sendToClients;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.enterprise.concurrent.LastExecution;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.concurrent.Trigger;
import javax.enterprise.inject.spi.CDI;
import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.json.bind.annotation.JsonbTransient;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.websocket.Session;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.libertybikes.game.core.Player.STATUS;
import org.libertybikes.restclient.PlayerService;

@JsonbPropertyOrder({ "id", "gameState", "board", "nextRoundId" })
public class GameRound implements Runnable {

    public static enum State {
        OPEN, // not yet started, still room for players
        FULL, // not started, but no more room for players
        STARTING, // in the process of game starting countdown
        RUNNING, // game is in progress and players are still alive
        FINISHED // game has ended and a winner has been declared
    }

    private static final int GAME_TICK_SPEED_DEFAULT = 50; // ms
    private static final int DELAY_BETWEEN_ROUNDS = 5; //ticks
    private static final int STARTING_COUNTDOWN = 3; // seconds
    private static final int MAX_TIME_BETWEEN_ROUNDS_DEFAULT = 20; // seconds
    private static final int FULL_GAME_TIME_BETWEEN_ROUNDS = 5; //seconds
    private static final Random r = new Random();
    private static final AtomicInteger runningGames = new AtomicInteger();

    // Properties exposed in JSON representation of object
    public final String id;
    public final String nextRoundId;
    public volatile State gameState = State.OPEN;
    private final GameBoard board = new GameBoard();

    private final AtomicBoolean gameRunning = new AtomicBoolean();
    private final AtomicBoolean paused = new AtomicBoolean();
    private final AtomicBoolean heartbeatStarted = new AtomicBoolean();
    private final Map<Session, Client> clients = new HashMap<>();
    private final Deque<Player> playerRanks = new ArrayDeque<>();
    private final Set<LifecycleCallback> lifecycleCallbacks = new HashSet<>();
    private final int GAME_TICK_SPEED, MAX_TIME_BETWEEN_ROUNDS;
    private LobbyCountdown lobbyCountdown;
    private AtomicBoolean lobbyCountdownStarted = new AtomicBoolean();

    private int ticksFromGameEnd = 0;

    // Get a string of 4 random uppercase letters (A-Z)
    private static String getRandomId() {
        char[] chars = new char[4];
        for (int i = 0; i < 4; i++)
            chars[i] = (char) (r.nextInt(26) + 65);
        return new String(chars);
    }

    public GameRound() {
        this(getRandomId());
    }

    public GameRound(String id) {
        this.id = id;
        nextRoundId = getRandomId();

        // Get game tick speed
        Integer tickSpeed = GAME_TICK_SPEED_DEFAULT;
        try {
            tickSpeed = InitialContext.doLookup("round/gameSpeed");
        } catch (Exception e) {
            log("Unable to perform JNDI lookup to determine game tick speed, using default value");
        }
        GAME_TICK_SPEED = (tickSpeed < 20 || tickSpeed > 100) ? GAME_TICK_SPEED_DEFAULT : tickSpeed;

        // Get delay between rounds
        Integer maxTimeBetweenRounds = MAX_TIME_BETWEEN_ROUNDS_DEFAULT;
        try {
            maxTimeBetweenRounds = InitialContext.doLookup("round/autoStartCooldown");
        } catch (Exception e) {
            log("Unable to perform JNDI lookup to determine time between rounds, using default value");
        }
        MAX_TIME_BETWEEN_ROUNDS = (maxTimeBetweenRounds < 5 || maxTimeBetweenRounds > 60) ? MAX_TIME_BETWEEN_ROUNDS_DEFAULT : maxTimeBetweenRounds;
    }

    public GameBoard getBoard() {
        return board;
    }

    private void beginLobbyCountdown(Session s, boolean isPhone) {
        if (!lobbyCountdownStarted.get()) {
            lobbyCountdownStarted.set(true);
            try {
                ManagedScheduledExecutorService exec = InitialContext.doLookup("java:comp/DefaultManagedScheduledExecutorService");
                lobbyCountdown = new LobbyCountdown();
                exec.submit(lobbyCountdown);
            } catch (NamingException e) {
                log("Unable to obtain executor service reference");
                e.printStackTrace();
            }
        }
        if (!isPhone)
            sendToClient(s, new OutboundMessage.AwaitPlayersCountdown(lobbyCountdown.roundStartCountdown));
    }

    public void updatePlayerDirection(Session playerSession, InboundMessage msg) {
        Client c = clients.get(playerSession);
        if (c.isPlayer())
            c.player.setDirection(msg.direction);
    }

    public void addPlayer(Session s, String playerId, String playerName, Boolean hasGameBoard) {
        // Front end should be preventing a player joining a full game but
        // defensive programming
        if (!isOpen()) {
            log("Cannot add player " + playerId + " to game because game has already started.");
            return;
        }

        if (getPlayers().size() + 1 >= Player.MAX_PLAYERS) {
            gameState = State.FULL;
            lobbyCountdown.gameFull();
        }

        Player p = board.addPlayer(playerId, playerName);
        boolean isPhone = false;
        if (p != null) {
            Client c = new Client(s, p);
            isPhone = c.isPhone = hasGameBoard ? false : true;
            clients.put(s, c);
            log("Player " + playerId + " has joined.");
        } else {
            log("Player " + playerId + " already exists.");
        }
        broadcastPlayerList();
        broadcastGameBoard();
        beginHeartbeat();
        beginLobbyCountdown(s, isPhone);
    }

    public void addAI() {
        if (!isOpen()) {
            return;
        }

        if (getPlayers().size() + 1 >= Player.MAX_PLAYERS) {
            gameState = State.FULL;
        }

        board.addAI();
        broadcastPlayerList();
        broadcastGameBoard();
    }

    public void addSpectator(Session s) {
        log("A spectator has joined.");
        clients.put(s, new Client(s));
        sendToClient(s, new OutboundMessage.PlayerList(getPlayers()));
        sendToClient(s, board);
        beginHeartbeat();
        beginLobbyCountdown(s, false);
    }

    public void addCallback(LifecycleCallback callback) {
        lifecycleCallbacks.add(callback);
    }

    private void beginHeartbeat() {
        // Send a heartbeat to connected clients every 100 seconds in an attempt to keep them connected.
        // It appears that when running in IBM Cloud, sockets time out after 120 seconds
        if (!heartbeatStarted.getAndSet(true)) {
            try {
                ManagedScheduledExecutorService exec = InitialContext.doLookup("java:comp/DefaultManagedScheduledExecutorService");
                log("Initiating heartbeat to clients");
                exec.schedule(() -> {
                    log("Sending heartbeat to " + clients.size() + " clients");
                    sendToClients(clients.keySet(), new OutboundMessage.Heartbeat());
                }, new HeartbeatTrigger());
            } catch (NamingException e) {
                log("Unable to obtain executor service reference");
                e.printStackTrace();
            }
        }
    }

    @JsonbTransient
    public boolean isPlayer(Session s) {
        Client c = clients.get(s);
        return c != null && c.isPlayer();
    }

    private void removePlayer(Player p) {
        p.disconnect();
        log(p.name + " disconnected.");

        // Open player slot for new joiners
        if (gameState == State.FULL && getPlayers().size() - 1 < Player.MAX_PLAYERS) {
            gameState = State.OPEN;
        }

        if (isOpen()) {
            board.removePlayer(p);
        } else if (gameState == State.RUNNING) {
            checkForWinner();
        }

        if (gameState != State.FINISHED)
            broadcastPlayerList();
    }

    public int removeClient(Session client) {
        Client c = clients.remove(client);
        if (c != null && c.player != null)
            removePlayer(c.player);
        return clients.size();
    }

    @JsonbTransient
    public Set<Player> getPlayers() {
        return board.players;
    }

    @Override
    public void run() {
        gameRunning.set(true);
        log(">>> Starting round");
        ticksFromGameEnd = 0;
        int numGames = runningGames.incrementAndGet();
        if (numGames > 3)
            log("WARNING: There are currently " + numGames + " game instances running.");
        long nextTick = System.currentTimeMillis() + GAME_TICK_SPEED;
        while (gameRunning.get()) {
            delayTo(nextTick);
            nextTick += GAME_TICK_SPEED;
            gameTick();
            if (ticksFromGameEnd > DELAY_BETWEEN_ROUNDS)
                gameRunning.set(false); // end the game if nobody can move anymore
        }
        endGame();
    }

    private void updatePlayerStats() {
        if (gameState != State.FINISHED)
            throw new IllegalStateException("Canot update player stats while game is still running.");

        PlayerService playerSvc = CDI.current().select(PlayerService.class, RestClient.LITERAL).get();
        int rank = 1;
        for (Player p : playerRanks) {
            log("Player " + p.name + " came in place " + rank);
            if (p.isRealPlayer())
                playerSvc.recordGame(p.id, rank);
            rank++;
        }
    }

    private void gameTick() {
        if (gameState != State.RUNNING) {
            ticksFromGameEnd++;
            return;
        }

        board.broadcastToAI();

        boolean boardUpdated = board.moveObjects();
        boolean playerDied = false;
        boolean playersMoved = false;
        // Move all living players forward 1
        for (Player p : getPlayers()) {
            if (p.isAlive()) {
                if (p.movePlayer(board.board)) {
                    playersMoved = true;
                } else {
                    playerDied = true;
                    playerRanks.push(p);
                }
            }
        }

        if (playerDied) {
            checkForWinner();
            broadcastPlayerList();
        }
        if (playersMoved || boardUpdated)
            broadcastGameBoard();
    }

    private void delayTo(long wakeUpTime) {
        delay(wakeUpTime - System.currentTimeMillis());
    }

    private void delay(long ms) {
        if (ms < 0)
            return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
        }
    }

    private Set<Session> getNonMobileSessions() {
        return clients.entrySet()
                        .stream()
                        .filter(c -> !c.getValue().isPhone)
                        .map(s -> s.getKey())
                        .collect(Collectors.toSet());
    }

    private void broadcastTimeUntilGameStarts(int time) {
        sendToClients(getNonMobileSessions(), new OutboundMessage.AwaitPlayersCountdown(time));
    }

    private void broadcastGameBoard() {
        sendToClients(getNonMobileSessions(), board);
    }

    private void broadcastPlayerList() {
        sendToClients(getNonMobileSessions(), new OutboundMessage.PlayerList(getPlayers()));
    }

    private void checkForWinner() {
        if (getPlayers().size() < 2) {// 1 player game, no winner
            gameState = State.FINISHED;
            return;
        }
        int alivePlayers = 0;
        Player alive = null;
        for (Player cur : getPlayers()) {
            if (cur.isAlive()) {
                alivePlayers++;
                alive = cur;
            }
        }
        if (alivePlayers == 1) {
            alive.setStatus(STATUS.Winner);
            playerRanks.push(alive);
            gameState = State.FINISHED;
        }

        if (alivePlayers == 0) {
            gameState = State.FINISHED;
        }
    }

    @JsonbTransient
    public boolean isStarted() {
        return gameState != State.OPEN && gameState != State.FULL;
    }

    @JsonbTransient
    public boolean isOpen() {
        return gameState == State.OPEN;
    }

    public void startGame() {
        if (isStarted())
            return;

        while (isOpen()) {
            addAI();
        }

        // Issue a countdown to all of the clients
        gameState = State.STARTING;

        sendToClients(clients.keySet(), new OutboundMessage.StartingCountdown(STARTING_COUNTDOWN));
        delay(TimeUnit.SECONDS.toMillis(STARTING_COUNTDOWN));

        paused.set(false);
        for (Player p : getPlayers())
            if (STATUS.Connected == p.getStatus())
                p.setStatus(STATUS.Alive);
        broadcastPlayerList();
        if (!gameRunning.get()) {
            try {
                ManagedScheduledExecutorService exec = InitialContext.doLookup("java:comp/DefaultManagedScheduledExecutorService");
                exec.submit(this);
            } catch (NamingException e) {
                log("Unable to start game due to: " + e);
                e.printStackTrace();
            }
        }
        gameState = State.RUNNING;
    }

    private void endGame() {
        runningGames.decrementAndGet();
        log("<<< Finished round");
        broadcastPlayerList();

        try {
            ManagedScheduledExecutorService exec = InitialContext.doLookup("java:comp/DefaultManagedScheduledExecutorService");
            exec.submit(() -> {
                updatePlayerStats();
            });
            exec.submit(() -> {
                lifecycleCallbacks.forEach(c -> c.gameEnding());
            });
        } catch (NamingException e) {
            log("Unable to perform post game processing");
            e.printStackTrace();
        }

        // Tell each client that the game is done and close the websockets
        for (Session s : clients.keySet())
            sendToClient(s, new OutboundMessage.GameStatus(State.FINISHED));
        for (Session s : clients.keySet())
            removeClient(s);
    }

    private void log(String msg) {
        System.out.println("[GameRound-" + id + "]  " + msg);
    }

    public interface LifecycleCallback {
        public void gameEnding();
    }

    private class LobbyCountdown implements Runnable {

        public int roundStartCountdown = MAX_TIME_BETWEEN_ROUNDS;

        public void gameFull() {
            roundStartCountdown = roundStartCountdown > 5 ? FULL_GAME_TIME_BETWEEN_ROUNDS : roundStartCountdown;
            broadcastTimeUntilGameStarts(roundStartCountdown);
        }

        @Override
        public void run() {
            while (isOpen() || gameState == State.FULL) {
                delay(1000);
                roundStartCountdown--;
                if (roundStartCountdown < 1) {
                    if (clients.size() == 0) {
                        log("No clients remaining.  Cancelling LobbyCountdown.");
                        // Ensure that game state is closed off so that no other players can quick join while a round is marked for deletion
                        gameState = State.FINISHED;
                    } else {
                        startGame();
                    }
                }
            }
        }
    }

    private class HeartbeatTrigger implements Trigger {

        private static final int HEARTBEAT_INTERVAL_SEC = 100;

        @Override
        public Date getNextRunTime(LastExecution lastExecutionInfo, Date taskScheduledTime) {
            // If there are any clients still connected to this game, keep sending heartbeats
            if (clients.size() == 0) {
                log("No clients remaining.  Cancelling heartbeat.");
                // Ensure that game state is closed off so that no other players can quick join while a round is marked for deletion
                gameState = State.FINISHED;
                return null;
            }
            return Date.from(Instant.now().plusSeconds(HEARTBEAT_INTERVAL_SEC));
        }

        @Override
        public boolean skipRun(LastExecution lastExecutionInfo, Date scheduledRunTime) {
            return clients.size() == 0;
        }

    }
}
