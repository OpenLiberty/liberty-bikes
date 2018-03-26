/**
 *
 */
package org.libertybikes.game.bot;

import java.util.Random;

import org.libertybikes.game.core.AIPlayer;
import org.libertybikes.game.core.DIRECTION;
import org.libertybikes.game.core.GameBoard;

public class Hal extends AIPlayer {

    private int ticksTillRandomMove, ticksTillMove, numOfRandomMoves;
    private final static int BOARD_SIZE = 121;
    static Random ran = new Random();
    private final static int CD = 2;
    private final static int BD = 6;

    public Hal(String id, String name, short playerNum) {
        super(id, name, playerNum);
        ticksTillRandomMove = 20;
        ticksTillMove = 4;
    }

    @Override
    public void broadCastBoard(short[][] board) {
        if (--ticksTillRandomMove < 1 && numOfRandomMoves < 10) {
            setDirection(DIRECTION.values()[ran.nextInt(4)]);
            ticksTillRandomMove = 35;
            numOfRandomMoves++;

        } else {

            if (--ticksTillMove > 1) {
                return;
            }

            ticksTillMove = 2;

            switch (direction) {
                case DOWN:
                    if (ran.nextBoolean()) {
                        if (y + BD > BOARD_SIZE || checkCollision(board, x, y + CD)) {
                            if (checkCollision(board, x + CD, y + CD)) {
                                direction = DIRECTION.LEFT;
                            }
                            direction = DIRECTION.RIGHT;
                        }
                    } else {
                        if (y + BD > BOARD_SIZE || checkCollision(board, x, y + CD)) {
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
                        if (x + BD > BOARD_SIZE || checkCollision(board, x + CD, y)) {
                            if (checkCollision(board, x + CD, y - CD)) {
                                direction = DIRECTION.DOWN;
                            }
                            direction = DIRECTION.UP;
                        }
                    } else {
                        if (x + BD > BOARD_SIZE || checkCollision(board, x + CD, y)) {
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
                if (y + BD > BOARD_SIZE || checkCollision(board, x, y + CD)) {
                    setDirection(DIRECTION.UP);
                }
                break;
            case LEFT:
                if (x - BD < 0 || checkCollision(board, x - CD, y)) {
                    setDirection(DIRECTION.RIGHT);
                }
                break;
            case RIGHT:
                if (x + BD > BOARD_SIZE || checkCollision(board, x + CD, y)) {
                    setDirection(DIRECTION.LEFT);
                }
                break;
            case UP:
                if (y - BD < 0 || checkCollision(board, x, y - CD)) {
                    setDirection(DIRECTION.DOWN);
                }
                break;
        }
    }

    private boolean checkCollision(short[][] board, int x, int y) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (board[x + i][y + j] == GameBoard.PLAYER_SPOT_TAKEN + playerNum || board[x + i][y + j] == GameBoard.SPOT_AVAILABLE) {
                } else {
                    return true;
                }
            }
        }
        return false;
    }

}
