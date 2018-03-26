package org.libertybikes.game.core;

import static org.libertybikes.game.round.service.GameRoundWebsocket.sendTextToClient;
import static org.libertybikes.game.round.service.GameRoundWebsocket.sendTextToClients;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.inject.spi.CDI;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.json.bind.annotation.JsonbTransient;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.websocket.Session;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.libertybikes.game.core.Player.STATUS;
import org.libertybikes.game.round.service.GameRoundService;
import org.libertybikes.game.round.service.GameRoundWebsocket;
import org.libertybikes.restclient.PlayerService;

@JsonbPropertyOrder({ "id", "gameState", "board", "nextRoundId" })
public class GameRound implements Runnable {

    public static enum State {
        OPEN, FULL, STARTING, RUNNING, FINISHED
    }

    public static final Jsonb jsonb = JsonbBuilder.create();
    public static final int GAME_TICK_SPEED = 50; // ms
    private static final int DELAY_BETWEEN_ROUNDS = 5; //ticks
    private static final int STARTING_COUNTDOWN = 3; // seconds
    private static final Random r = new Random();
    private static final AtomicInteger runningGames = new AtomicInteger();

    // Properties exposed in JSON representation of object
    public final String id;
    public final String nextRoundId;
    public volatile State gameState = State.OPEN;
    private final GameBoard board = new GameBoard();

    private final AtomicBoolean gameRunning = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final Map<Session, Client> clients = new HashMap<>();

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
//        board.addObstacle(new MovingObstacle(10, 5, 60, 60, 0, -1, 5));
//        board.addObstacle(new MovingObstacle(10, 5, 60, 65, 0, 1));
        board.addObstacle(new MovingObstacle(5, 5, GameBoard.BOARD_SIZE / 2 - 10, GameBoard.BOARD_SIZE / 3, -1, -1));
        board.addObstacle(new MovingObstacle(5, 5, GameBoard.BOARD_SIZE / 2 + 10, (GameBoard.BOARD_SIZE / 3 * 2) - 5, 1, 1));

        // TL
        board.addObstacle(new Obstacle(15, 1, GameBoard.BOARD_SIZE / 8, GameBoard.BOARD_SIZE / 8));
        board.addObstacle(new Obstacle(1, 14, GameBoard.BOARD_SIZE / 8, GameBoard.BOARD_SIZE / 8 + 1));
        // TR
        board.addObstacle(new Obstacle(15, 1, ((GameBoard.BOARD_SIZE / 8) * 7) - 14, GameBoard.BOARD_SIZE / 8));
        board.addObstacle(new Obstacle(1, 14, (GameBoard.BOARD_SIZE / 8) * 7, GameBoard.BOARD_SIZE / 8 + 1));
        // BL
        board.addObstacle(new Obstacle(15, 1, GameBoard.BOARD_SIZE / 8, (GameBoard.BOARD_SIZE / 8) * 7));
        board.addObstacle(new Obstacle(1, 14, GameBoard.BOARD_SIZE / 8, ((GameBoard.BOARD_SIZE / 8) * 7) - 14));
        // BR
        board.addObstacle(new Obstacle(15, 1, ((GameBoard.BOARD_SIZE / 8) * 7) - 14, (GameBoard.BOARD_SIZE / 8) * 7));
        board.addObstacle(new Obstacle(1, 14, (GameBoard.BOARD_SIZE / 8) * 7, ((GameBoard.BOARD_SIZE / 8) * 7) - 14));
    }

    public GameBoard getBoard() {
        return board;
    }

    public void updatePlayerDirection(Session playerSession, InboundMessage msg) {
        Client c = clients.get(playerSession);
        if (c.isPlayer())
            c.player.setDirection(msg.direction);
    }

    public void addPlayer(Session s, String playerId, String playerName, Boolean hasGameBoard) {
        // Front end should be preventing a player joining a full game but
        // defensive programming
        if (gameState != State.OPEN) {
            System.out.println("Cannot add player " + playerId + " to game " + id + " because game has already started.");
            return;
        }

        if (getPlayers().size() + 1 >= Player.MAX_PLAYERS) {
            gameState = State.FULL;
        }

        Player p = board.addPlayer(playerId, playerName);
        if (p != null) {
            Client c = new Client(s, p);
            c.isPhone = hasGameBoard ? false : true;
            clients.put(s, c);
            System.out.println("Player " + playerId + " has joined.");
        } else {
            System.out.println("Player " + playerId + " already exists.");
        }
        broadcastPlayerList();
        broadcastGameBoard();
    }

    public void addAI() {
        if (gameState != State.OPEN) {
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
        System.out.println("A spectator has joined.");
        clients.put(s, new Client(s));
        sendTextToClient(s, jsonb.toJson(new OutboundMessage.PlayerList(getPlayers())));
        sendTextToClient(s, jsonb.toJson(board));
    }

    private void removePlayer(Player p) {
        p.disconnect();
        System.out.println(p.name + " disconnected.");

        // Open player slot for new joiners
        if (gameState == State.FULL && getPlayers().size() - 1 < Player.MAX_PLAYERS) {
            gameState = State.OPEN;
        }

        if (gameState == State.OPEN) {
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
        System.out.println("Starting round: " + id);
        ticksFromGameEnd = 0;
        int numGames = runningGames.incrementAndGet();
        if (numGames > 3)
            System.out.println("WARNING: There are currently " + numGames + " game instances running.");
        while (gameRunning.get()) {
            delay(GAME_TICK_SPEED);
            gameTick();
            if (ticksFromGameEnd > DELAY_BETWEEN_ROUNDS)
                gameRunning.set(false); // end the game if nobody can move anymore
        }
        runningGames.decrementAndGet();
        System.out.println("Finished round: " + id);
        broadcastPlayerList();

        long start = System.nanoTime();
        updatePlayerStats();

        // Wait for 5 seconds, but subtract the amount of time it took to update player stats
        long nanoWait = TimeUnit.SECONDS.toNanos(5) - (System.nanoTime() - start);
        delay(TimeUnit.NANOSECONDS.toMillis(nanoWait));
        System.out.println("Clients flagged for auto-requeue will be redirected to the next round now");
        GameRoundService gameSvc = CDI.current().select(GameRoundService.class).get();
        for (Client c : clients.values())
            if (c.autoRequeue)
                GameRoundWebsocket.requeueClient(gameSvc, this, c.session);
    }

    private void updatePlayerStats() {
        if (gameState != State.FINISHED)
            throw new IllegalStateException("Canot update player stats while game is still running.");

        Set<Player> players = getPlayers();
        if (players.size() < 2)
            return; // Don't update player stats for single-player games

        PlayerService playerSvc = CDI.current().select(PlayerService.class, RestClient.LITERAL).get();
        for (Player p : players) {
            if (p.getStatus() == STATUS.Winner) {
                System.out.println("Player " + p.name + " has won round " + id);
                playerSvc.addWin(p.id);
            } else {
                System.out.println("Player " + p.name + " has participated in round " + id);
                playerSvc.addLoss(p.id);
            }
        }
    }

    private void gameTick() {
        if (gameState != State.RUNNING) {
            ticksFromGameEnd++;
            return;
        }

        board.broadcastToAI();

        boolean boardUpdated = board.moveObjects();

        boolean death = false;
        // Move all living players forward 1
        boolean playerStatusChange = false;
        boolean playersMoved = false;
        for (Player p : getPlayers()) {
            if (p.isAlive()) {
                if (p.movePlayer(board.board)) {
                    playersMoved = true;
                } else {
                    death = true;
                }
            }
        }

        if (death) {
            checkForWinner();
            playerStatusChange = true;
        }

        if (playersMoved || boardUpdated)
            broadcastGameBoard();

        if (playerStatusChange)
            broadcastPlayerList();
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

    private void broadcastGameBoard() {
        sendTextToClients(getNonMobileSessions(), jsonb.toJson(board));
    }

    private void broadcastPlayerList() {
        sendTextToClients(getNonMobileSessions(), jsonb.toJson(new OutboundMessage.PlayerList(getPlayers())));
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
            gameState = State.FINISHED;
        }

        if (alivePlayers == 0) {
            gameState = State.FINISHED;
        }
    }

    public void startGame() {
        if (gameState != State.OPEN && gameState != State.FULL)
            return;

        while (gameState == State.OPEN) {
            addAI();
        }

        // Issue a countdown to all of the clients
        gameState = State.STARTING;

        sendTextToClients(clients.keySet(), jsonb.toJson(new OutboundMessage.StartingCountdown(STARTING_COUNTDOWN)));
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
                System.out.println("Unable to start game due to: " + e);
                e.printStackTrace();
            }
        }
        gameState = State.RUNNING;
    }
}
