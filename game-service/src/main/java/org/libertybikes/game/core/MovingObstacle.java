/**
 *
 */
package org.libertybikes.game.core;

public class MovingObstacle extends Obstacle {

    public int xDir, yDir, oldX, oldY, moveDelay, currentDelay = 0;
    public boolean hasMoved = false;

    public MovingObstacle(int w, int h, int x, int y) {
        super(w, h, x, y);
    }

    public MovingObstacle(int w, int h, int x, int y, int xDir, int yDir) {
        super(w, h, x, y);
        moveDelay = 1;
        this.xDir = xDir;
        this.yDir = yDir;
    }

    public MovingObstacle(int w, int h, int x, int y, int xDir, int yDir, int moveDelay) {
        this(w, h, x, y, xDir, yDir);
        this.moveDelay = moveDelay;
    }

    public void checkCollision(short[][] board) {
        boolean checkCorner = true;

        if (xDir != 0) {
            if (xDir > 0) {
                if (x + width + 1 >= GameBoard.BOARD_SIZE || hasCollision(board, DIRECTION.RIGHT)) {
                    xDir = xDir * -1;
                    checkCorner = false;
                }
            } else {
                if (x - 1 < 0 || hasCollision(board, DIRECTION.LEFT)) {
                    xDir = xDir * -1;
                    checkCorner = false;
                }
            }
        }
        if (yDir != 0) {
            if (yDir > 0) {
                if (y + height + 1 >= GameBoard.BOARD_SIZE || hasCollision(board, DIRECTION.DOWN)) {
                    yDir = yDir * -1;
                    checkCorner = false;
                }
            } else {
                if (y - 1 < 0 || hasCollision(board, DIRECTION.UP)) {
                    yDir = yDir * -1;
                    checkCorner = false;
                }
            }
        }
        if (checkCorner) {
            checkCornerCollision(board);
        }
    }

    public void move(short[][] board) {

        if (++currentDelay < moveDelay) {
            // don't move yet
            hasMoved = false;
            return;
        }

        hasMoved = true;
        currentDelay = 0;
        oldX = x;
        oldY = y;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                board[x + i][y + j] = GameBoard.SPOT_AVAILABLE;
            }
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                board[x + i + xDir][y + j + yDir] = GameBoard.OBJECT_SPOT_TAKEN;
            }
        }
        x += xDir;
        y += yDir;

    }

    private void checkCornerCollision(short[][] board) {
        if (xDir == 0 || yDir == 0) {
            return;
        }
        if (xDir > 0) {
            if (yDir > 0 && board[x + width][y + height] == GameBoard.OBJECT_SPOT_TAKEN) {
                xDir = xDir * -1;
                yDir = yDir * -1;
            } else if (yDir < 0 && board[x + width][y - 1] == GameBoard.OBJECT_SPOT_TAKEN) {
                xDir = xDir * -1;
                yDir = yDir * -1;
            }

        } else {
            if (yDir > 0 && board[x - 1][y + height] == GameBoard.OBJECT_SPOT_TAKEN) {
                xDir = xDir * -1;
                yDir = yDir * -1;
            } else if (yDir < 0 && board[x - 1][y - 1] == GameBoard.OBJECT_SPOT_TAKEN) {
                xDir = xDir * -1;
                yDir = yDir * -1;
            }

        }
    }

    // loops through the spots we want to move to and see if they are already taken by
    // only another object, we will move through players and their lines
    private boolean hasCollision(short[][] board, DIRECTION dir) {
        switch (dir) {
            case UP:
                for (int i = 0; i < width; i++) {
                    if (board[x + i][y - 1] == GameBoard.OBJECT_SPOT_TAKEN)
                        return true;
                }
                return false;
            case DOWN:
                for (int i = 0; i < width; i++) {
                    if (board[x + i][y + height] == GameBoard.OBJECT_SPOT_TAKEN)
                        return true;
                }
                return false;
            case LEFT:
                for (int i = 0; i < height; i++) {
                    if (board[x - 1][y + i] == GameBoard.OBJECT_SPOT_TAKEN)
                        return true;
                }
                return false;
            default:
                for (int i = 0; i < height; i++) {
                    if (board[x + width][y + i] == GameBoard.OBJECT_SPOT_TAKEN)
                        return true;
                }
                return false;
        }
    }

}
