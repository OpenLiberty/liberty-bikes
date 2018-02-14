/**
 *
 */
package org.libertybikes.game.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GameBoard {

    public static final int BOARD_SIZE = 121;
    public static final boolean SPOT_AVAILABLE = true, SPOT_TAKEN = false;

    // @JsonbTransient // TODO use annotation here once OpenLiberty upgrades to yasson 1.0.1 (contains bug fix)
    private final boolean[][] board = new boolean[BOARD_SIZE][BOARD_SIZE];

    public final Set<Obstacle> obstacles = new HashSet<>();
    public final Set<Player> players = new HashSet<>();

    public GameBoard() {
        for (int i = 0; i < BOARD_SIZE; i++)
            Arrays.fill(board[i], SPOT_AVAILABLE);
    }

    public boolean addObstacle(Obstacle o) {
        if (o.x + o.width > BOARD_SIZE || o.y + o.height > BOARD_SIZE)
            throw new IllegalArgumentException("Obstacle does not fit on board: " + o);

        // First make sure all spaces are available
        for (int x = 0; x < o.width; x++)
            for (int y = 0; y < o.height; y++)
                if (!board[o.x + x][o.y + y]) {
                    System.out.println("Obstacle cannot be added to board because spot [" + o.x + x + "][" + o.y + y + "] is taken.");
                    return false;
                }

        // If all spaces are available, claim them
        for (int x = 0; x < o.width; x++)
            for (int y = 0; y < o.height; y++)
                board[o.x + x][o.y + y] = SPOT_TAKEN;

        return obstacles.add(o);
    }

    public boolean addPlayer(Player p) {
        if (p.x > BOARD_SIZE || p.y > BOARD_SIZE)
            throw new IllegalArgumentException("Player does not fit on board: " + p);

        board[p.x][p.y] = SPOT_TAKEN;

        return players.add(p);
    }

    // TODO: once OpenLiberty moves up to yasson 1.0.1 this method can be removed
    public boolean[][] board() {
        return board;
    }

    // For debugging
    public void dumpBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            StringBuilder row = new StringBuilder();
            for (int j = 0; j < BOARD_SIZE; j++)
                row.append(board[i][j] == SPOT_TAKEN ? "X" : "_");
            System.out.println(String.format("%03d %s", i, row.toString()));
        }
    }

}
