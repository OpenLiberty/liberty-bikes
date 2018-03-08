/**
 *
 */
package org.libertybikes.game.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GameBoard {

    public static final int BOARD_SIZE = 121;
    public static final short SPOT_AVAILABLE = 0, PLAYER_SPOT_TAKEN = 1, OBJECT_SPOT_TAKEN = -1;

    // @JsonbTransient // TODO use annotation here once OpenLiberty upgrades to yasson 1.0.1 (contains bug fix)
    private final short[][] board = new short[BOARD_SIZE][BOARD_SIZE];

    public final Set<Obstacle> obstacles = new HashSet<>();
    public final Set<MovingObstacle> movingObstacles = new HashSet<>();

    public final Set<Player> players = new HashSet<>();

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

    public boolean addPlayer(Player p) {
        if (p.x > BOARD_SIZE || p.y > BOARD_SIZE)
            throw new IllegalArgumentException("Player does not fit on board: " + p);

        board[p.x][p.y] = PLAYER_SPOT_TAKEN;

        return players.add(p);
    }

    // TODO: once OpenLiberty moves up to yasson 1.0.1 this method can be removed
    public short[][] board() {
        return board;
    }

    // For debugging
    public void dumpBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            StringBuilder row = new StringBuilder();
            for (int j = 0; j < BOARD_SIZE; j++) {
                switch (board[i][j]) {
                    case (PLAYER_SPOT_TAKEN): {
                        row.append("X");
                        break;
                    }
                    case (OBJECT_SPOT_TAKEN): {
                        row.append("O");
                        break;
                    }
                    default: {
                        row.append("_");
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
            obstacle.move(board);
        }

        return true;
    }

}
