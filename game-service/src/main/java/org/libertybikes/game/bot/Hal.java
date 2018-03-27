/**
 *
 */
package org.libertybikes.game.bot;

import java.util.Random;

import org.libertybikes.game.core.DIRECTION;
import org.libertybikes.game.core.GameBoard;

public class Hal extends AIPlayer {

    private int ticksTillRandomMove = 20;
    private int ticksTillMove = 4;
    private int numOfRandomMoves = 0;
    static Random ran = new Random();
    // Collision detection distance
    private final static int CD = 1;
    // Border detection distance
    private final static int BD = 5;

    private DIRECTION direction;
    private DIRECTION lastDirection = null;

    private int x, y;
    private boolean hasMoved = false;

    public Hal(int startX, int startY, int width, int height, DIRECTION startDirection, short takenSpotNumber) {
        super(startX, startY, width, height, startDirection, takenSpotNumber);
        direction = startDirection;
        x = startX;
        y = startY;
    }

    @Override
    public DIRECTION processGameTick(short[][] board) {
        if (hasMoved) {
            switch (direction) {
                case DOWN:
                    y++;
                    break;
                case LEFT:
                    x--;
                    break;
                case RIGHT:
                    x++;
                    break;
                case UP:
                    y--;
                    break;
            }
        }

        hasMoved = true;

        if (--ticksTillRandomMove < 1 && numOfRandomMoves < 10) {
            direction = setDirection(DIRECTION.values()[ran.nextInt(4)]);
            ticksTillRandomMove = 35;
            numOfRandomMoves++;

        } else {

            if (--ticksTillMove > 1) {
                return direction;
            }

            ticksTillMove = 2;

            switch (direction) {
                case DOWN:
                    if (ran.nextBoolean()) {
                        if (y + BD > GameBoard.BOARD_SIZE || checkCollision(board, x, y + CD)) {
                            if (checkCollision(board, x + CD, y + CD)) {
                                direction = DIRECTION.LEFT;
                            }
                            direction = DIRECTION.RIGHT;
                        }
                    } else {
                        if (y + BD > GameBoard.BOARD_SIZE || checkCollision(board, x, y + CD)) {
                            if (checkCollision(board, x - CD, y + CD)) {
                                direction = DIRECTION.RIGHT;
                            }
                            direction = DIRECTION.LEFT;
                        }
                    }
                    break;
                case LEFT:
                    if (ran.nextBoolean()) {
                        if (x - BD < 0 || checkCollision(board, x - CD, y)) {
                            if (checkCollision(board, x - CD, y - CD)) {
                                direction = DIRECTION.DOWN;
                            }
                            direction = DIRECTION.UP;
                        }
                    } else {
                        if (x - BD < 0 || checkCollision(board, x - CD, y)) {
                            if (checkCollision(board, x - CD, y + CD)) {
                                direction = DIRECTION.UP;
                            }
                            direction = DIRECTION.DOWN;
                        }
                    }
                    break;
                case RIGHT:
                    if (ran.nextBoolean()) {
                        if (x + BD > GameBoard.BOARD_SIZE || checkCollision(board, x + CD, y)) {
                            if (checkCollision(board, x + CD, y - CD)) {
                                direction = DIRECTION.DOWN;
                            }
                            direction = DIRECTION.UP;
                        }
                    } else {
                        if (x + BD > GameBoard.BOARD_SIZE || checkCollision(board, x + CD, y)) {
                            if (checkCollision(board, x + CD, y + CD)) {
                                direction = DIRECTION.UP;
                            }
                            direction = DIRECTION.DOWN;
                        }
                    }
                    break;
                case UP:
                    if (ran.nextBoolean()) {
                        if (y - BD < 0 || checkCollision(board, x, y - CD)) {
                            if (checkCollision(board, x + CD, y - CD)) {
                                direction = DIRECTION.LEFT;
                            }
                            direction = DIRECTION.RIGHT;
                        }
                    } else {
                        if (y - BD < 0 || checkCollision(board, x, y - CD)) {
                            if (checkCollision(board, x - CD, y - CD)) {
                                direction = DIRECTION.RIGHT;
                            }
                            direction = DIRECTION.LEFT;
                        }
                    }
                    break;
            }
        }

        switch (direction) {
            case DOWN:
                if (y + BD > GameBoard.BOARD_SIZE || checkCollision(board, x, y + CD)) {
                    setDirection(DIRECTION.UP);
                }
                break;
            case LEFT:
                if (x - BD < 0 || checkCollision(board, x - CD, y)) {
                    setDirection(DIRECTION.RIGHT);
                }
                break;
            case RIGHT:
                if (x + BD > GameBoard.BOARD_SIZE || checkCollision(board, x + CD, y)) {
                    setDirection(DIRECTION.LEFT);
                }
                break;
            case UP:
                if (y - BD < 0 || checkCollision(board, x, y - CD)) {
                    setDirection(DIRECTION.DOWN);
                }
                break;
        }

        lastDirection = direction;
        return direction;
    }

    private boolean checkCollision(short[][] board, int x, int y) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (board[x + i][y + j] == GameBoard.PLAYER_SPOT_TAKEN + takenSpotNumber || board[x + i][y + j] == GameBoard.SPOT_AVAILABLE) {
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public DIRECTION setDirection(DIRECTION newDirection) {

        if (lastDirection != null) {
            if ((newDirection == DIRECTION.UP && lastDirection == DIRECTION.DOWN) ||
                (newDirection == DIRECTION.DOWN && lastDirection == DIRECTION.UP) ||
                (newDirection == DIRECTION.LEFT && lastDirection == DIRECTION.RIGHT) ||
                (newDirection == DIRECTION.RIGHT && lastDirection == DIRECTION.LEFT)) {
                return lastDirection;
            }
        }

        return newDirection;
    }

}
