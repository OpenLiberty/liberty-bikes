/**
 *
 */
package org.libertybikes.game.core;

/**
 * @author OLENCOOK
 *
 */
public class MovingObstacle extends Obstacle {

    private static enum DIRECTION {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

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

        if (xDir != 0) {
            if (xDir > 0) {
                if (x + width + 1 < GameBoard.BOARD_SIZE && !hasCollision(board, DIRECTION.RIGHT)) {
                    moveRight(board);
                } else if (!hasCollision(board, DIRECTION.LEFT)) {
                    moveLeft(board);
                    xDir = xDir * -1;
                }
            } else {
                if (x - 1 >= 0 && !hasCollision(board, DIRECTION.LEFT)) {
                    moveLeft(board);
                } else if (!hasCollision(board, DIRECTION.RIGHT)) {
                    moveRight(board);
                    xDir = xDir * -1;
                }
            }
        }

        if (yDir != 0) {
            if (yDir > 0) {
                if (y + height + 1 < GameBoard.BOARD_SIZE && !hasCollision(board, DIRECTION.DOWN)) {
                    moveDown(board);
                } else if (!hasCollision(board, DIRECTION.UP)) {
                    moveUp(board);
                    yDir = yDir * -1;
                }
            } else {
                if (y - 1 >= 0 && !hasCollision(board, DIRECTION.UP)) {
                    moveUp(board);
                } else if (!hasCollision(board, DIRECTION.DOWN)) {
                    moveDown(board);
                    yDir = yDir * -1;
                }
            }
        }
    }

    private void moveRight(short[][] board) {
        for (int i = 0; i <= height; i++) {
            board[x][y + i] = GameBoard.SPOT_AVAILABLE;
            board[x + width + 1][y + i] = GameBoard.OBJECT_SPOT_TAKEN;
        }
        x++;
    }

    private void moveLeft(short[][] board) {
        for (int i = 0; i <= height; i++) {
            board[x - 1][y + i] = GameBoard.OBJECT_SPOT_TAKEN;
            board[x + width][y + i] = GameBoard.SPOT_AVAILABLE;
        }
        x--;
    }

    private void moveUp(short[][] board) {
        for (int i = 0; i <= width; i++) {
            board[x + i][y - 1] = GameBoard.OBJECT_SPOT_TAKEN;
            board[x + i][y + height] = GameBoard.SPOT_AVAILABLE;
        }
        y--;
    }

    private void moveDown(short[][] board) {
        for (int i = 0; i <= width; i++) {
            board[x + i][y] = GameBoard.SPOT_AVAILABLE;
            board[x + i][y + height + 1] = GameBoard.OBJECT_SPOT_TAKEN;
        }
        y++;
    }

    private boolean hasCollision(short[][] board, DIRECTION dir) {
        switch (dir) {
            case UP:
                for (int i = 0; i <= width; i++) {
                    if (board[x + i][y - 1] == GameBoard.OBJECT_SPOT_TAKEN)
                        return true;
                }
                return false;
            case DOWN:
                for (int i = 0; i <= width; i++) {
                    if (board[x + i][y + height + 1] == GameBoard.OBJECT_SPOT_TAKEN)
                        return true;
                }
                return false;
            case LEFT:
                for (int i = 0; i <= height; i++) {
                    if (board[x - 1][y + i] == GameBoard.OBJECT_SPOT_TAKEN)
                        return true;
                }
                return false;
            default:
                for (int i = 0; i <= height; i++) {
                    if (board[x + width + 1][y + i] == GameBoard.OBJECT_SPOT_TAKEN)
                        return true;
                }
                return false;
        }
    }

}
