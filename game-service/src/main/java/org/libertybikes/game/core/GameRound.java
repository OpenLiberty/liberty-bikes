package org.libertybikes.game.core;

import static org.libertybikes.game.round.service.GameRoundWebsocket.sendToClient;
import static org.libertybikes.game.round.service.GameRoundWebsocket.sendToClients;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Calendar;
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

import org.eclipse.microprofile.metrics.Timer.Context;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.libertybikes.game.core.Player.STATUS;
import org.libertybikes.game.metric.GameMetrics;
import org.libertybikes.restclient.PlayerService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

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
    private static final int STARTING_COUNTDOWN = 4; // seconds
    private static final int MAX_TIME_BETWEEN_ROUNDS_DEFAULT = 20; // seconds
    private static final int FULL_GAME_TIME_BETWEEN_ROUNDS = 5; //seconds
    private static final Random r = new Random();
    private static final AtomicInteger runningGames = new AtomicInteger();

    // Properties exposed in JSON representation of object
    public final String id;
    public final String nextRoundId;
    private volatile State gameState = State.OPEN;
    private final GameBoard board = new GameBoard();

    private final AtomicBoolean gameRunning = new AtomicBoolean();
    private final AtomicBoolean didRun = new AtomicBoolean();
    private final AtomicBoolean paused = new AtomicBoolean();
    private final AtomicBoolean heartbeatStarted = new AtomicBoolean();
    private final AtomicBoolean gameClosed = new AtomicBoolean();
    private final Map<Session, Client> clients = new HashMap<>();
    private final Deque<Player> playerRanks = new ArrayDeque<>();
    private final Set<LifecycleCallback> lifecycleCallbacks = new HashSet<>();
    private final int GAME_TICK_SPEED, MAX_TIME_BETWEEN_ROUNDS;
    private LobbyCountdown lobbyCountdown;
    private AtomicBoolean lobbyCountdownStarted = new AtomicBoolean();

    private String jwt = null;

    private int ticksFromGameEnd = 0;

    protected static Key signingKey = null;

    protected String keyStore;

    String keyStorePW;

    String keyStoreAlias;

    private Context timerContext;

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

        // Increment round counter metrics
        GameMetrics.counterInc(GameMetrics.totalRoundsCounter);
        GameMetrics.counterInc(GameMetrics.currentRoundsCounter);
    }

    public GameBoard getBoard() {
        return board;
    }

    private void beginLobbyCountdown(Session s, boolean isPhone) {
        if (!lobbyCountdownStarted.get()) {
            lobbyCountdownStarted.set(true);
            lobbyCountdown = new LobbyCountdown();
            executor().submit(lobbyCountdown);
        }
        if (!isPhone)
            sendToClient(s, new OutboundMessage.AwaitPlayersCountdown(lobbyCountdown.roundStartCountdown));
    }

    public boolean updatePlayerDirection(Session playerSession, InboundMessage msg) {
        Client c = clients.get(playerSession);
        if (c == null)
            return false;
        c.player.ifPresent((p) -> p.setDirection(msg.direction));
        return true;
    }

    public boolean addPlayer(Session s, String playerId, String playerName, Boolean hasGameBoard) {
        // Front end should be preventing a player joining a full game but
        // defensive programming
        if (!isOpen()) {
            log("Cannot add player " + playerId + " to game because game has already started.");
            return false;
        }

        if (playerId == null || playerId.isEmpty()) {
            log("Player must have a valid ID to join a round, but was null/empty.");
            return false;
        }

        Client replaceClient = null;
        for (Client c : clients.values()) {
            if (c.player.isPresent() && playerId.equals(c.player.get().id)) {
                // If we find a player trying to join a game with the same ID as a player who is already
                // in the game, assume it was from an AI Bot player who's developer made a hot code update
                // TODO: once private IDs are implemented, could filter this on playerId.startsWith("BOT:")
                replaceClient = c;
                break;
            }
        }
        if (replaceClient != null) {
            log("Replacing client with id: " + playerId);
            removeClient(replaceClient.session);
        }

        int numPlayers = getPlayers().size() + 1;
        if (numPlayers == Player.MAX_PLAYERS) {
            gameState = State.FULL;
            lobbyCountdown.gameFull();
        } else if (numPlayers > Player.MAX_PLAYERS) {
            log("Cannot add player " + playerId + " to game because the current round is full.");
            return false;
        }

        Player p = board.addPlayer(playerId, playerName);
        boolean isPhone = false;
        if (p != null) {
            Client c = new Client(s, p);
            isPhone = c.isPhone = hasGameBoard ? false : true;
            clients.put(s, c);
            log("Player " + playerId + " has joined.");

            // Increment player counter metrics
            GameMetrics.counterInc(GameMetrics.currentPlayersCounter);
            GameMetrics.counterInc(GameMetrics.totalPlayersCounter);
            if (isPhone) {
                GameMetrics.counterInc(GameMetrics.totalMobilePlayersCounter);
            }

        } else {
            log("Player " + playerId + " already exists.");
        }
        broadcastPlayerList();
        broadcastGameBoard();
        beginHeartbeat();
        beginLobbyCountdown(s, isPhone);
        return true;
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
            log("Initiating heartbeat to clients");
            executor().schedule(() -> {
                log("Sending heartbeat to " + clients.size() + " clients");
                sendToClients(clients.keySet(), new OutboundMessage.Heartbeat());
            }, new HeartbeatTrigger());

        }
    }

    @JsonbTransient
    public boolean isPlayer(Session s) {
        Client c = clients.get(s);
        return c != null && c.player.isPresent();
    }

    private void removePlayer(Player p, boolean isMobile) {
        p.disconnect();
        log(p.name + " disconnected.");

        // Open player slot for new joiners
        if (gameState == State.FULL && getPlayers().size() - 1 < Player.MAX_PLAYERS) {
            gameState = State.OPEN;
        }

        if (isOpen()) {
            board.removePlayer(p);

            // Decrement player counters because they didn't play
            GameMetrics.counterDec(GameMetrics.totalPlayersCounter);
            if (isMobile) {
                GameMetrics.counterDec(GameMetrics.totalMobilePlayersCounter);
            }

        } else if (gameState == State.RUNNING) {
            checkForWinner();
        }

        if (gameState != State.FINISHED)
            broadcastPlayerList();

        // Decrement current players counter
        GameMetrics.counterDec(GameMetrics.currentPlayersCounter);
    }

    /**
     * Removes a client from the game round
     *
     * @param client The client to remove
     * @return The number of remaining clients in the game round
     */
    public int removeClient(Session client) {
        Client c = clients.remove(client);
        if (c != null && c.player.isPresent()) {
            removePlayer(c.player.get(), c.isPhone);
        }
        return clients.size();
    }

    @JsonbTransient
    public Set<Player> getPlayers() {
        return board.players;
    }

    @Override
    public void run() {
        gameRunning.set(true);
        didRun.set(true);
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
            throw new IllegalStateException("Cannot update player stats while game is still running.");

        PlayerService playerSvc = CDI.current().select(PlayerService.class, RestClient.LITERAL).get();
        int rank = 1;
        for (Player p : playerRanks) {
            log("Player " + p.name + " came in place " + rank);
            if (p.isRealPlayer()) {
                String jwt = createJWT();
                String authHeader = "Bearer " + jwt;
                playerSvc.recordGame(p.id, rank, authHeader);
            }
            rank++;
        }
    }

    private String createJWT() {
        if (jwt != null)
            return jwt;
        if (signingKey == null) {
            try {
                getKeyStoreInfo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Claims onwardsClaims = Jwts.claims();

            onwardsClaims.put("upn", "game-service");
            onwardsClaims.put("groups", "admin");
            // Set the subject using the "id" field from our claims map.
            onwardsClaims.setSubject(id);

            onwardsClaims.setId(id);

            // We'll use this claim to know this is a user token
            onwardsClaims.setAudience("client");

            onwardsClaims.setIssuer("https://libertybikes.mybluemix.net");
            // we set creation time to 24hrs ago, to avoid timezone issues in the
            // browser verification of the jwt.
            Calendar calendar1 = Calendar.getInstance();
            calendar1.add(Calendar.HOUR, -24);
            onwardsClaims.setIssuedAt(calendar1.getTime());

            // client JWT has 24 hrs validity from now.
            Calendar calendar2 = Calendar.getInstance();
            calendar2.add(Calendar.HOUR, 48);
            onwardsClaims.setExpiration(calendar2.getTime());

            // finally build the new jwt, using the claims we just built, signing it
            // with our signing key, and adding a key hint as kid to the encryption header,
            // which is optional, but can be used by the receivers of the jwt to know which
            // key they should verify it with.
            jwt = Jwts.builder()
                            .setHeaderParam("kid", "bike")
                            .setHeaderParam("alg", "RS256")
                            .setClaims(onwardsClaims)
                            .signWith(SignatureAlgorithm.RS256, signingKey)
                            .compact();
            return jwt;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "bad";
    }

    private synchronized void getKeyStoreInfo() throws IOException {
        if (signingKey != null)
            return;
        try {
            // load up the keystore

            keyStore = InitialContext.doLookup("jwtKeyStore");
            keyStorePW = InitialContext.doLookup("jwtKeyStorePassword");
            keyStoreAlias = InitialContext.doLookup("jwtKeyStoreAlias");

            FileInputStream is = new FileInputStream(keyStore);

            KeyStore signingKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
            signingKeystore.load(is, keyStorePW.toCharArray());
            signingKey = signingKeystore.getKey(keyStoreAlias, keyStorePW.toCharArray());
        } catch (Exception e) {
            throw new IOException(e);
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

        if (playerDied)
            checkForWinner();
        if (playersMoved || boardUpdated)
            broadcastGameBoard();
        if (playerDied)
            broadcastPlayerList();
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

    public State getGameState() {
        return gameState;
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

        executor().submit(new Starter());
    }

    public void endGame() {
        if (gameClosed.getAndSet(true))
            return;

        gameState = State.FINISHED;
        if (didRun.get())
            runningGames.decrementAndGet();
        log("<<< Finished round");

        // Decrement current rounds counter and close round timer
        GameMetrics.counterDec(GameMetrics.currentRoundsCounter);
        if (timerContext != null)
            timerContext.close();

        broadcastPlayerList();

        ManagedScheduledExecutorService exec = executor();
        exec.submit(() -> {
            updatePlayerStats();
        });
        exec.submit(() -> {
            lifecycleCallbacks.forEach(c -> c.gameEnding());
        });

        // Tell each client that the game is done and close the websockets
        for (Session s : clients.keySet())
            sendToClient(s, new OutboundMessage.GameStatus(State.FINISHED));

        // Give players a 10s grace period before they are removed from a finished game
        if (exec != null)
            exec.schedule(() -> {
                for (Session s : new HashSet<Session>(clients.keySet()))
                    removeClient(s);
            }, 10, TimeUnit.SECONDS);
    }

    private ManagedScheduledExecutorService executor() {
        try {
            return InitialContext.doLookup("java:comp/DefaultManagedScheduledExecutorService");
        } catch (NamingException e) {
            log("Unable to obtain ManagedScheduledExecutorService");
            e.printStackTrace();
            return null;
        }
    }

    private void log(String msg) {
        System.out.println("[GameRound-" + id + "]  " + msg);
    }

    public interface LifecycleCallback {
        public void gameEnding();
    }

    private class Starter implements Runnable {

        @Override
        public void run() {

            for (int i = 0; i < (STARTING_COUNTDOWN * 2); i++) {
                delay(500);
                broadcastPlayerList();
            }

            paused.set(false);
            for (Player p : getPlayers())
                if (STATUS.Connected == p.getStatus())
                    p.setStatus(STATUS.Alive);
            broadcastPlayerList();
            if (!gameRunning.get()) {
                executor().submit(GameRound.this);
            }
            gameState = State.RUNNING;

            // Start round timer metric
            timerContext = GameMetrics.timerStart(GameMetrics.gameRoundTimerMetadata);
        }
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
                for (int i = 0; i < 2; i++) {
                    delay(500);
                    broadcastPlayerList();
                }
                roundStartCountdown--;
                if (roundStartCountdown < 1) {
                    if (clients.size() == 0) {
                        // Ensure that game state is closed off so that no other players
                        // can quick join while a round is marked for deletion
                        log("No clients remaining.  Cancelling LobbyCountdown.");
                        endGame();
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
                // Ensure that game state is closed off so that no other players
                // can quick join while a round is marked for deletion
                log("No clients remaining.  Cancelling heartbeat.");
                endGame();
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
