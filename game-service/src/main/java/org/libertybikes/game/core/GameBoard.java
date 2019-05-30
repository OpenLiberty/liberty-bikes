package org.libertybikes.game.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.json.bind.annotation.JsonbTransient;

import org.libertybikes.game.bot.Hal;
import org.libertybikes.game.bot.Wally;
import org.libertybikes.game.maps.GameMap;

public class GameBoard {

    public static final int BOARD_SIZE = 121;
    public static final short SPOT_AVAILABLE = 0, TRAIL_SPOT_TAKEN = -10, OBJECT_SPOT_TAKEN = -8, PLAYER_SPOT_TAKEN = 1;
    private static final Map<String, Short> preferredPlayerSlots = new ConcurrentHashMap<>();

    public static class Point {
        public final int x;
        public final int y;

        public Point(int x, int y) {
            if (x < 0 || y < 0 || x >= BOARD_SIZE || y >= BOARD_SIZE)
                throw new IllegalArgumentException("Invalid point location x=" + x + " y=" + y);
            this.x = x;
            this.y = y;
        }
    }

    @JsonbTransient
    public final short[][] board = new short[BOARD_SIZE][BOARD_SIZE];

    public final Set<Obstacle> obstacles = new HashSet<>();
    public final Set<MovingObstacle> movingObstacles = new LinkedHashSet<>();
    public final Set<Player> players = new LinkedHashSet<>();
    private final boolean[] takenPlayerSlots = new boolean[Player.MAX_PLAYERS];
    private GameMap gameMap;

    public GameBoard() {
        this(-1);
    }

    public GameBoard(int map) {
        for (int i = 0; i < BOARD_SIZE; i++)
            Arrays.fill(board[i], SPOT_AVAILABLE);
        initializeGameMap(map);
    }

    private void initializeGameMap(int map) {
        gameMap = GameMap.create(map);
        for (Obstacle o : gameMap.getObstacles()) {
            addObstacle(o);
        }
        for (MovingObstacle o : gameMap.getMovingObstacles()) {
            addObstacle(o);
        }
    }

    public boolean verifyObstacle(Obstacle o) {
        if (o.x < 0 || o.y < 0 || o.x + o.width > BOARD_SIZE || o.y + o.height > BOARD_SIZE)
            throw new IllegalArgumentException("Obstacle does not fit on board: " + o);

        // First make sure all spaces are available
        for (int x = 0; x < o.width; x++)
            for (int y = 0; y < o.height; y++)
                if (board[o.x + x][o.y + y] != SPOT_AVAILABLE) {
                    System.out.println("Obstacle cannot be added to board because spot [" + o.x + x + "][" + o.y + y + "] is taken.");
                    return false;
                }

        // If all spaces are available, claim them
        for (int x = 0; x < o.width; x++)
            for (int y = 0; y < o.height; y++)
                board[o.x + x][o.y + y] = OBJECT_SPOT_TAKEN;
        return true;
    }

    public boolean addObstacle(Obstacle o) {
        return verifyObstacle(o) ? obstacles.add(o) : false;
    }

    public boolean addObstacle(MovingObstacle o) {
        return verifyObstacle(o) ? movingObstacles.add(o) : false;
    }

    public Player addPlayer(String playerId, String playerName) {

        short playerNum = -1;

        // Try to keep players in the same slots across rounds if possible
        Short preferredSlot = preferredPlayerSlots.get(playerId);
        if (preferredSlot != null && !takenPlayerSlots[preferredSlot]) {
            playerNum = preferredSlot;
        } else {
            // Find first open player slot to fill, which determines position
            for (short i = 0; i < takenPlayerSlots.length; i++) {
                if (!takenPlayerSlots[i]) {
                    playerNum = i;
                    break;
                }
            }
            preferredPlayerSlots.put(playerId, playerNum);
        }
        takenPlayerSlots[playerNum] = true;

        // Don't let the preferred player slot map take up too much memory
        if (preferredPlayerSlots.size() > 1000)
            preferredPlayerSlots.clear();

        // Initialize Player
        Player p = new Player(playerId, playerName, playerNum).addTo(gameMap);

        if (p.x + p.width > BOARD_SIZE || p.y + p.height > BOARD_SIZE)
            throw new IllegalArgumentException("Player does not fit on board: " + p);

        for (int i = 0; i < p.width; i++) {
            for (int j = 0; j < p.height; j++) {
                board[p.x + i][p.y + j] = (short) (PLAYER_SPOT_TAKEN + playerNum);
            }
        }

        return players.add(p) ? p : null;
    }

    public boolean removePlayer(Player p) {
        takenPlayerSlots[p.getPlayerNum()] = false;

        // Right now we don't clear their dead body while drawing the canvas
//        for (int i = 0; i < p.width; i++) {
//            for (int j = 0; j < p.height; j++) {
//                board[p.x + i][p.y + j] = SPOT_AVAILABLE;
//            }
//        }

        return players.remove(p);
    }

    // For debugging
    public void dumpBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            StringBuilder row = new StringBuilder();
            for (int j = 0; j < BOARD_SIZE; j++) {
                switch (board[j][i]) {

                    case (SPOT_AVAILABLE): {
                        row.append("-");
                        break;
                    }
                    case (OBJECT_SPOT_TAKEN): {
                        row.append("O");
                        break;
                    }
                    case (TRAIL_SPOT_TAKEN): {
                        row.append("X");
                        break;
                    }
                    default: {
                        row.append(board[j][i]);
                        break;
                    }
                }
            }
            System.out.println(String.format("%03d %s", i, row.toString()));
        }
    }

    public boolean moveObjects() {
        if (movingObstacles.isEmpty()) {
            return false;
        }

        for (MovingObstacle obstacle : movingObstacles) {
            obstacle.checkCollision(board);
        }

        for (MovingObstacle obstacle : movingObstacles) {
            obstacle.move(board);
        }

        return true;
    }

    public void broadcastToAI() {
        for (Player p : players) {
            if (p.isAlive() && !p.isRealPlayer()) {
                short[][] boardCopy = board.clone();
                p.processAIMove(boardCopy);
            }
        }
    }

    public void addAI() {
        // Find first open player slot to fill, which determines position
        short playerNum = -1;
        for (short i = 0; i < takenPlayerSlots.length; i++) {
            if (!takenPlayerSlots[i]) {
                playerNum = i;
                takenPlayerSlots[i] = true;
                break;
            }
        }

        // Initialize Player
        Player p = Math.random() < 0.5 ? //
                        new Hal(gameMap, playerNum).asPlayer() : //
                        new Wally(gameMap, playerNum).asPlayer();
        p.addTo(gameMap);

        if (p.x + p.width > BOARD_SIZE || p.y + p.height > BOARD_SIZE)
            throw new IllegalArgumentException("Player does not fit on board: " + p);

        for (int i = 0; i < p.width; i++) {
            for (int j = 0; j < p.height; j++) {
                board[p.x + i][p.y + j] = (short) (PLAYER_SPOT_TAKEN + playerNum);
            }
        }

        players.add(p);
    }

    public boolean removeAI(Player p) {
        takenPlayerSlots[p.getPlayerNum()] = false;
        return players.remove(p);
    }

}
