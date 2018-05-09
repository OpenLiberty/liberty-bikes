/**
 *
 */
package org.libertybikes.game.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.json.bind.annotation.JsonbTransient;

@JsonbPropertyOrder({ "id", "name", "color", "status", "alive", "x", "y", "width", "height", "oldX", "oldY", "trailPosX", "trailPosY", "trailPosX2", "trailPosY2", "direction" })
public class Player {

    public static enum STATUS {
        Connected,
        Alive,
        Dead,
        Winner,
        Disconnected
    }

    public static final int MAX_PLAYERS = 4;

    // Properties exposed by JSON-B
    public final String name;
    public final String id;
    public final String color;
    public int oldX, x;
    public int oldY, y;
    public final int width = 3;
    public final int height = 3;
    private boolean isAlive = true;
    public int trailPosX, trailPosY, trailPosX2, trailPosY2;
    private STATUS playerStatus = STATUS.Connected;

    protected final short playerNum;
    protected DIRECTION direction;
    private DIRECTION lastDirection = null;
    private DIRECTION desiredNextDirection = null;

    private AI ai = null;

    //FIFO Stack size 3
    private Queue<TrailPosition> trail = new LinkedList<TrailPosition>();

    private class TrailPosition {
        public int x, y;

        public TrailPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public Player(String id, String name, short playerNum, int x, int y) {
        this.id = id;
        this.name = name;
        this.playerNum = playerNum;

        // Initialize starting data
        if (playerNum == 0) {
            color = "#DF740C";
            direction = DIRECTION.RIGHT;
        } else if (playerNum == 1) {
            color = "#FF0000";
            direction = DIRECTION.DOWN;
        } else if (playerNum == 2) {
            color = "#6FC3DF";
            direction = DIRECTION.UP;
        } else if (playerNum == 3) {
            color = "#FFE64D";
            direction = DIRECTION.LEFT;
        } else {
            //error
            color = "#FFFFFF";
            direction = DIRECTION.DOWN;
        }
        oldX = this.x = x;
        oldY = this.y = y;
        trailPosX = trailPosX2 = x + 1;
        trailPosY = trailPosY2 = y + 1;
    }

    public DIRECTION getDirection() {
        return desiredNextDirection != null ? desiredNextDirection : direction;
    }

    public void setDirection(DIRECTION newDirection) {
        if (newDirection == direction)
            return;

        // Make sure the player doesn't move backwards on themselves
        if (lastDirection != null) {
            if ((newDirection == DIRECTION.UP && lastDirection == DIRECTION.DOWN) ||
                (newDirection == DIRECTION.DOWN && lastDirection == DIRECTION.UP) ||
                (newDirection == DIRECTION.LEFT && lastDirection == DIRECTION.RIGHT) ||
                (newDirection == DIRECTION.RIGHT && lastDirection == DIRECTION.LEFT)) {
                desiredNextDirection = newDirection;
                return;
            }
        }

        direction = newDirection;
    }

    /**
     * Move a player forward one space in whatever direction they are facing currently.
     *
     * @return True if the player is still alive after moving forward one space. False otherwise.
     */
    public final boolean movePlayer(short[][] s) {

        oldX = x;
        oldY = y;

        // If a player issues two moves in the same game tick and the second direction is illegal,
        // spread out the moves across two ticks rather than ignoring the second move entirely
        if (desiredNextDirection != null && lastDirection == direction) {
            setDirection(desiredNextDirection);
            desiredNextDirection = null;
        }

        switch (direction) {
            case UP:
                if (y - 1 < 0 || checkCollision(s, x, y - 1)) {
                    setStatus(STATUS.Dead);
                    return isAlive();
                }
                moveUp(s);
                break;
            case DOWN:
                if (y + height + 1 >= GameBoard.BOARD_SIZE || checkCollision(s, x, y + 1)) {
                    setStatus(STATUS.Dead);
                    return isAlive();
                }
                moveDown(s);
                break;
            case RIGHT:
                if (x + width + 1 >= GameBoard.BOARD_SIZE || checkCollision(s, x + 1, y)) {
                    setStatus(STATUS.Dead);
                    return isAlive();
                }
                moveRight(s);
                break;
            case LEFT:
                if (x - 1 < 0 || checkCollision(s, x - 1, y)) {
                    setStatus(STATUS.Dead);
                    return isAlive();
                }
                moveLeft(s);
                break;
        }

        trail.add(new TrailPosition(x + 1, y + 1));
        boolean first = true;
        if (trail.size() > 2) {
            Iterator<TrailPosition> it = trail.iterator();
            while (it.hasNext()) {
                TrailPosition tp = it.next();
                if (!withinOneSquare(tp)) {
                    if (first) {
                        s[tp.x][tp.y] = GameBoard.TRAIL_SPOT_TAKEN;
                        trailPosX = tp.x;
                        trailPosY = tp.y;
                        it.remove();
                        first = false;
                    } else {
                        s[tp.x][tp.y] = GameBoard.TRAIL_SPOT_TAKEN;
                        trailPosX2 = tp.x;
                        trailPosY2 = tp.y;
                        it.remove();
                        break;
                    }
                }
            }
        }

        lastDirection = direction;

        return isAlive();
    }

    private boolean withinOneSquare(TrailPosition trail) {
        if (Math.abs(trail.x - (x + 1)) <= 1 && Math.abs(trail.y - (y + 1)) <= 1) {
            return true;
        }
        return false;
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

    private void moveRight(short[][] board) {
        for (int i = 0; i < height; i++) {
            // clear previous position
            board[x][y + i] = GameBoard.SPOT_AVAILABLE;
            board[x + width][y + i] += GameBoard.PLAYER_SPOT_TAKEN + playerNum;
        }
        x++;
    }

    private void moveLeft(short[][] board) {
        for (int i = 0; i < height; i++) {
            board[x - 1][y + i] += GameBoard.PLAYER_SPOT_TAKEN + playerNum;
            board[x + width - 1][y + i] = GameBoard.SPOT_AVAILABLE;
        }
        x--;
    }

    private void moveUp(short[][] board) {
        for (int i = 0; i < width; i++) {
            board[x + i][y - 1] += GameBoard.PLAYER_SPOT_TAKEN + playerNum;
            board[x + i][y + height - 1] = GameBoard.SPOT_AVAILABLE;
        }
        y--;
    }

    private void moveDown(short[][] board) {
        for (int i = 0; i < width; i++) {
            board[x + i][y] = GameBoard.SPOT_AVAILABLE;
            board[x + i][y + height] += GameBoard.PLAYER_SPOT_TAKEN + playerNum;
        }
        y++;
    }

    public void disconnect() {
        setStatus(STATUS.Disconnected);
    }

    public final void setStatus(STATUS newState) {
        if (newState == STATUS.Dead || newState == STATUS.Disconnected)
            this.isAlive = false;
        if (newState == STATUS.Dead && this.playerStatus == STATUS.Winner)
            return; // Winning player can't die (game is over)
        this.playerStatus = newState;
    }

    public STATUS getStatus() {
        return this.playerStatus;
    }

    @JsonbTransient
    public int getPlayerNum() {
        return this.playerNum;
    }

    public boolean isAlive() {
        return isAlive;
    }

    @JsonbTransient
    public boolean isRealPlayer() {
        return ai == null;
    }

    public void processAIMove(short[][] board) {
        if (isRealPlayer())
            return;
        try {
            direction = ai.processGameTick(board);
        } catch (Exception e) {
            System.out.println("Bot Exception: " + e.toString());
            e.printStackTrace(System.out);
        }
    }

    public void addAI(AI ai) {
        this.ai = ai;
    }
}
