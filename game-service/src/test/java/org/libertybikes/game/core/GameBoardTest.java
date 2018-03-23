/**
 *
 */
package org.libertybikes.game.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.libertybikes.game.core.GameBoard.BOARD_SIZE;

import org.junit.Before;
import org.junit.Test;

public class GameBoardTest {

    GameBoard board = null;

    @Before
    public void initBoard() {
        board = new GameBoard();
    }

    @Test
    public void testAddObstacle() {
        assertTrue(board.addObstacle(new Obstacle(5, 10, 0, 0)));
        verifyTaken(0, 0);
        verifyTaken(4, 9);
    }

    @Test
    public void testAdd2Obstacles() {
        assertTrue(board.addObstacle(new Obstacle(5, 10, 0, 0)));
        assertTrue(board.addObstacle(new Obstacle(25, 25, BOARD_SIZE - 25, BOARD_SIZE - 25)));

        verifyTaken(0, 0);
        verifyTaken(4, 9);

        verifyTaken(BOARD_SIZE - 25, BOARD_SIZE - 25);
        verifyTaken(BOARD_SIZE - 1, BOARD_SIZE - 1);
    }

    @Test
    public void testOverlappingObstacles() {
        assertTrue(board.addObstacle(new Obstacle(5, 10, 0, 0)));
        assertFalse(board.addObstacle(new Obstacle(5, 10, 4, 9)));
        verifyTaken(0, 0);
        verifyTaken(4, 9);
        verifyAvailable(5, 9);
        verifyAvailable(4, 10);
        verifyAvailable(5, 10);
    }

    @Test
    public void testIllegalObstacle() {
        try {
            board.addObstacle(new Obstacle(2, 1, -3, 4));
            fail("Should not be able to add obstacle off of board");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            board.addObstacle(new Obstacle(BOARD_SIZE + 1, 1, 3, 4));
            fail("Should not be able to add obstacle off of board");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    private void verifyTaken(int x, int y) {
        if (board.board[x][y] != GameBoard.OBJECT_SPOT_TAKEN) {
            board.dumpBoard();
            fail("Spot should be taken but it was available: [" + x + "][" + y + "]");
        }
    }

    private void verifyAvailable(int x, int y) {
        if (board.board[x][y] != GameBoard.SPOT_AVAILABLE) {
            board.dumpBoard();
            fail("Spot should be availble but it was taken: [" + x + "][" + y + "]");
        }
    }

}
