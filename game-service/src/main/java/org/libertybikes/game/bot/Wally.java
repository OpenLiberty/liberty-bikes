package org.libertybikes.game.bot;

import java.util.Random;

import org.libertybikes.game.core.DIRECTION;
import org.libertybikes.game.core.GameBoard;
import org.libertybikes.game.maps.GameMap;

public class Wally extends AIPlayer {

    static final Random ran = new Random();

    private DIRECTION direction;

    private int locX, locY;

    public Wally(GameMap map, short playerNum) {
        super(map, playerNum);
        direction = startDirection;
        locX = startX;
        locY = startY;
    }

    @Override
    public DIRECTION processGameTick(short[][] board) {
        direction = straightLine(board, locX, locY);
        if (direction == DIRECTION.LEFT) {
            locX--;
        } else if (direction == DIRECTION.UP) {
            locY--;
        } else if (direction == DIRECTION.RIGHT) {
            locX++;
        } else {
            locY++;
        }

        return direction;
    }

    private boolean movable(short[][] b, int x, int y, DIRECTION d) {
        try {
            if (d == DIRECTION.LEFT) {
                if (b[x - 1][y] == GameBoard.SPOT_AVAILABLE && b[x - 1][y + 1] == GameBoard.SPOT_AVAILABLE && b[x - 1][y + 2] == GameBoard.SPOT_AVAILABLE) {
                    return true;
                }
            } else if (d == DIRECTION.RIGHT) {
                if (b[x + 3][y] == GameBoard.SPOT_AVAILABLE && b[x + 3][y + 1] == GameBoard.SPOT_AVAILABLE && b[x + 3][y + 2] == GameBoard.SPOT_AVAILABLE) {
                    return true;
                }
            } else if (d == DIRECTION.UP) {
                if (b[x][y - 1] == GameBoard.SPOT_AVAILABLE && b[x + 1][y - 1] == GameBoard.SPOT_AVAILABLE && b[x + 2][y - 1] == GameBoard.SPOT_AVAILABLE) {
                    return true;
                }
            } else if (d == DIRECTION.DOWN) {
                if (b[x][y + 3] == GameBoard.SPOT_AVAILABLE && b[x + 1][y + 3] == GameBoard.SPOT_AVAILABLE && b[x + 2][y + 3] == GameBoard.SPOT_AVAILABLE) {
                    return true;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {

        }

        return false;
    }

    private DIRECTION straightLine(short[][] b, int x, int y) {
        int left = goUntilWall(b, x, y, DIRECTION.LEFT, 0);
        int right = goUntilWall(b, x, y, DIRECTION.RIGHT, 0);
        int up = goUntilWall(b, x, y, DIRECTION.UP, 0);
        int down = goUntilWall(b, x, y, DIRECTION.DOWN, 0);

        if (x + 5 < GameBoard.BOARD_SIZE && y + 5 < GameBoard.BOARD_SIZE && b[x + 5][y + 5] == GameBoard.OBJECT_SPOT_TAKEN) {
            right = right / 10;
            down = down / 10;
        }
        if (x - 5 >= 0 && y + 5 < GameBoard.BOARD_SIZE && b[x - 5][y + 5] == GameBoard.OBJECT_SPOT_TAKEN) {
            left = left / 10;
            down = down / 10;
        }
        if (x + 5 < GameBoard.BOARD_SIZE && y - 5 >= 0 && b[x + 5][y - 5] == GameBoard.OBJECT_SPOT_TAKEN) {
            right = right / 10;
            up = up / 10;
        }
        if (x - 5 >= 0 && y - 5 >= 0 && b[x - 5][y - 5] == GameBoard.OBJECT_SPOT_TAKEN) {
            left = left / 10;
            up = up / 10;
        }

        if (direction == DIRECTION.LEFT) {
            if (left >= up) {
                if (left >= down) {
                    return DIRECTION.LEFT;
                } else {
                    return DIRECTION.DOWN;
                }
            } else if (down >= up) {
                return DIRECTION.DOWN;
            } else {
                return DIRECTION.UP;
            }
        } else if (direction == DIRECTION.RIGHT) {
            if (right >= up) {
                if (right >= down) {
                    return DIRECTION.RIGHT;
                } else {
                    return DIRECTION.DOWN;
                }
            } else if (down >= up) {
                return DIRECTION.DOWN;
            } else {
                return DIRECTION.UP;
            }
        } else if (direction == DIRECTION.UP) {
            if (left >= up) {
                if (left >= right) {
                    return DIRECTION.LEFT;
                } else {
                    return DIRECTION.RIGHT;
                }
            } else if (right >= up) {
                return DIRECTION.RIGHT;
            } else {
                return DIRECTION.UP;
            }
        } else if (direction == DIRECTION.DOWN) {
            if (left >= right) {
                if (left >= down) {
                    return DIRECTION.LEFT;
                } else {
                    return DIRECTION.DOWN;
                }
            } else if (down >= right) {
                return DIRECTION.DOWN;
            } else {
                return DIRECTION.RIGHT;
            }
        }
        return DIRECTION.LEFT;
    }

    private int goUntilWall(short[][] b, int x, int y, DIRECTION d, int total) {
        if (d == DIRECTION.LEFT) {
            if (movable(b, x, y, d)) {
                return goUntilWall(b, x - 1, y, d, total + 1);
            } else {
                return total;
            }
        } else if (d == DIRECTION.RIGHT) {
            if (movable(b, x, y, d)) {
                return goUntilWall(b, x + 1, y, d, total + 1);
            } else {
                return total;
            }
        } else if (d == DIRECTION.UP) {
            if (movable(b, x, y, d)) {
                return goUntilWall(b, x, y - 1, d, total + 1);
            } else {
                return total;
            }
        } else if (d == DIRECTION.DOWN) {
            if (movable(b, x, y, d)) {
                return goUntilWall(b, x, y + 1, d, total + 1);
            } else {
                return total;
            }
        }
        return 0;

    }

}
