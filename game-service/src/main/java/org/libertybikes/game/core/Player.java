/**
 *
 */
package org.libertybikes.game.core;

import java.util.LinkedList;
import java.util.Queue;

import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.json.bind.annotation.JsonbTransient;

@JsonbPropertyOrder({ "id", "name", "color", "status", "isAlive", "x", "y", "width", "height", "oldX", "oldY", "trailPosX", "trailPosY", "trailPosX2", "trailPosY2" })
public class Player {

    public static enum STATUS {
        Connected,
        Alive,
        Dead,
        Winner,
        Disconnected
    }

    private static enum PlayerStartingData {
        START_1("#DF740C", 9, 9, DIRECTION.RIGHT),
        START_2("#FF0000", 9, GameBoard.BOARD_SIZE - 11, DIRECTION.UP),
        START_3("#6FC3DF", GameBoard.BOARD_SIZE - 11, 9, DIRECTION.DOWN),
        START_4("#FFE64D", GameBoard.BOARD_SIZE - 11, GameBoard.BOARD_SIZE - 11, DIRECTION.LEFT);

        public final String color;
        public final int x;
        public final int y;
        public final DIRECTION dir;

        PlayerStartingData(String color, int x, int y, DIRECTION dir) {
            this.color = color;
            this.x = x;
            this.y = y;
            this.dir = dir;
        }
    }

    public static final int MAX_PLAYERS = PlayerStartingData.values().length;
    private static final PlayerStartingData[] startingData = new PlayerStartingData[] { PlayerStartingData.START_1, PlayerStartingData.START_2,
                                                                                        PlayerStartingData.START_3, PlayerStartingData.START_4 };

    // Properties exposed by JSON-B
    public final String name;
    public final String id;
    public final String color;
    public int oldX, x;
    public int oldY, y;
    public final int width = 3;
    public final int height = 3;
    public boolean isAlive = true;
    public int trailPosX, trailPosY, trailPosX2, trailPosY2;
    private STATUS playerStatus = STATUS.Connected;

    private final short playerNum;
    private DIRECTION direction;
    private DIRECTION lastDirection = null;
    private DIRECTION desiredNextDirection = null;

    //FIFO Stack size 3
    private Queue<TrailPosition> trail = new LinkedList<TrailPosition>();

    private class TrailPosition {
        public int x, y;

        public TrailPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public Player(String id, String name, short playerNum) {
        this.id = id;
        this.name = name;
        this.playerNum = playerNum;

        // Initialize starting data
        PlayerStartingData data = startingData[playerNum];
        color = data.color;
        direction = data.dir;
        oldX = x = data.x;
        oldY = y = data.y;
        trailPosX = trailPosX2 = x + 1;
        trailPosY = trailPosY2 = y + 1;
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
    public boolean movePlayer(short[][] s) {

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
                    return isAlive;
                }
                moveUp(s);
                break;
            case DOWN:
                if (y + height + 1 >= GameBoard.BOARD_SIZE || checkCollision(s, x, y + 1)) {
                    setStatus(STATUS.Dead);
                    return isAlive;
                }
                moveDown(s);
                break;
            case RIGHT:
                if (x + width + 1 >= GameBoard.BOARD_SIZE || checkCollision(s, x + 1, y)) {
                    setStatus(STATUS.Dead);
                    return isAlive;
                }
                moveRight(s);
                break;
            case LEFT:
                if (x - 1 < 0 || checkCollision(s, x - 1, y)) {
                    setStatus(STATUS.Dead);
                    return isAlive;
                }
                moveLeft(s);
                break;
        }

        trail.add(new TrailPosition(x + 1, y + 1));

        if (trail.size() > 2) {
            TrailPosition trailP = trail.remove();
            trailPosX = trailP.x;
            trailPosY = trailP.y;
            s[trailPosX][trailPosY] = GameBoard.TRAIL_SPOT_TAKEN;
            if (lastDirection != direction) {
                TrailPosition trailP2 = trail.remove();
                trailPosX2 = trailP2.x;
                trailPosY2 = trailP2.y;
                s[trailPosX2][trailPosY2] = GameBoard.TRAIL_SPOT_TAKEN;
            } else {
                trailPosX2 = trailPosX;
                trailPosY2 = trailPosY;
            }
        }

        lastDirection = direction;

        return isAlive;
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

    public void setStatus(STATUS newState) {
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
}
