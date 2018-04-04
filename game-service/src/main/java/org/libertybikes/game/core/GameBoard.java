/**
 *
 */
package org.libertybikes.game.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.json.bind.annotation.JsonbTransient;

import org.libertybikes.game.bot.Hal;
import org.libertybikes.game.bot.Walle;

public class GameBoard {

    public static final int BOARD_SIZE = 121;
    public static final short SPOT_AVAILABLE = 0, TRAIL_SPOT_TAKEN = -10, OBJECT_SPOT_TAKEN = -8, PLAYER_SPOT_TAKEN = 1;
    private static final Map<String, Short> preferredPlayerSlots = new ConcurrentHashMap<>();

    @JsonbTransient
    public final short[][] board = new short[BOARD_SIZE][BOARD_SIZE];

    public final Set<Obstacle> obstacles = new HashSet<>();
    public final Set<MovingObstacle> movingObstacles = new HashSet<>();
    public final Set<Player> players = new HashSet<>();
    private final boolean[] takenPlayerSlots = new boolean[Player.MAX_PLAYERS];

    public GameBoard() {
        for (int i = 0; i < BOARD_SIZE; i++)
            Arrays.fill(board[i], SPOT_AVAILABLE);
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
        System.out.println("Player slot " + playerNum + " taken");

        // Don't let the preferred player slot map take up too much memory
        if (preferredPlayerSlots.size() > 1000)
            preferredPlayerSlots.clear();

        // Initialize Player
        Player p = new Player(playerId, playerName, playerNum);

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
            if (p.isAlive()) {
                short[][] boardCopy = board.clone();
                p.processAIMove(boardCopy);
            }
        }
    }

    /**
     *
     */
    public void addAI() {
        // Find first open player slot to fill, which determines position
        short playerNum = -1;
        for (short i = 0; i < takenPlayerSlots.length; i++) {
            if (!takenPlayerSlots[i]) {
                playerNum = i;
                takenPlayerSlots[i] = true;
                System.out.println("Player slot " + i + " taken");
                break;
            }
        }

        // Initialize Player
        Player p;
        AI ai;

        if (Math.random() < .5) {
            p = new Player("Hal-" + playerNum, "Hal-" + playerNum, playerNum);
            ai = new Hal(p.x, p.y, p.width, p.height, p.direction, playerNum);
        } else {
            p = new Player("WALLE-" + playerNum, "WALLE-" + playerNum, playerNum);
            ai = new Walle(p.x, p.y, p.width, p.height, p.direction, playerNum);
        }

        if (p.x + p.width > BOARD_SIZE || p.y + p.height > BOARD_SIZE)
            throw new IllegalArgumentException("Player does not fit on board: " + p);

        for (int i = 0; i < p.width; i++) {
            for (int j = 0; j < p.height; j++) {
                board[p.x + i][p.y + j] = (short) (PLAYER_SPOT_TAKEN + playerNum);
            }
        }

        players.add(p);
        p.addAI(ai);
    }

    public boolean removeAI(Player p) {
        takenPlayerSlots[p.getPlayerNum()] = false;
        return players.remove(p);
    }

}
