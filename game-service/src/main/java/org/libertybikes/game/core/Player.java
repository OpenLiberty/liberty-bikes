/**
 *
 */
package org.libertybikes.game.core;

public class Player {

    public static enum DIRECTION {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public static enum STATUS {
        Connected,
        Alive,
        Dead,
        Winner,
        Disconnected
    }

    public final String color;
    public DIRECTION direction = DIRECTION.RIGHT;
    private DIRECTION lastDirection = null;
    private DIRECTION desiredNextDirection = null;
    public int x;
    public int y;
    public int playerNum;
    public String playerName;
    public boolean isAlive = true;
    private STATUS playerStatus = STATUS.Connected;

    public Player(String color) {
        this.color = color;
    }

    public Player(String color, int xstart, int ystart, int playerNum) {
        this.color = color;
        x = xstart;
        y = ystart;
        this.playerNum = playerNum;
    }

    public String toJson() {
        // {"color":"#FF0000","coords":{"x":251,"y":89}}
        StringBuffer sb = new StringBuffer("{\"color\":\"");
        sb.append(this.color);
        sb.append("\",\"coords\":{\"x\":");
        sb.append(this.x);
        sb.append(",\"y\":");
        sb.append(this.y);
        sb.append("}}");
        return sb.toString();
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

    public DIRECTION getDrirection() {
        return direction;
    }

    /**
     * Move a player forward one space in whatever direction they are facing currently.
     *
     * @return True if the player is still alive after moving forward one space. False otherwise.
     */
    public boolean movePlayer(boolean[][] board) {
        // Consume the space the player was in before the move
        board[x][y] = false;

        // If a player issues two moves in the same game tick and the second direction is illegal,
        // spread out the moves across two ticks rather than ignoring the second move entirely
        if (desiredNextDirection != null && lastDirection == direction) {
            setDirection(desiredNextDirection);
            desiredNextDirection = null;
        }

        switch (direction) {
            case UP:
                if (y - 1 >= 0)
                    y--;
                break;
            case DOWN:
                if (y + 1 < GameRound.BOARD_SIZE)
                    y++;
                break;
            case RIGHT:
                if (x + 1 < GameRound.BOARD_SIZE)
                    x++;
                break;
            case LEFT:
                if (x - 1 >= 0)
                    x--;
                break;
        }

        // Check if the player is now dead after moving
        boolean spaceAvailable = board[x][y];
        if (!spaceAvailable) {
            setStatus(STATUS.Dead);
        }
        lastDirection = direction;
        return isAlive;
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

    public int getPlayerNum() {
        return this.playerNum;
    }
}
