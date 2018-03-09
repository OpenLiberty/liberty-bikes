/**
 *
 */
package org.libertybikes.game.core;

import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.json.bind.annotation.JsonbTransient;

@JsonbPropertyOrder({ "id", "name", "color", "status", "x", "y", "isAlive" })
public class Player {

    public static enum STATUS {
        Connected,
        Alive,
        Dead,
        Winner,
        Disconnected
    }

    private static enum PlayerStartingData {
        START_1("#DF740C", 10, 10, DIRECTION.RIGHT),
        START_2("#FF0000", 10, GameBoard.BOARD_SIZE - 10, DIRECTION.UP),
        START_3("#6FC3DF", GameBoard.BOARD_SIZE - 10, 10, DIRECTION.DOWN),
        START_4("#FFE64D", GameBoard.BOARD_SIZE - 10, GameBoard.BOARD_SIZE - 10, DIRECTION.LEFT);

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
    public int x;
    public int y;
    public boolean isAlive = true;
    private STATUS playerStatus = STATUS.Connected;

    private final int playerNum;
    private DIRECTION direction;
    private DIRECTION lastDirection = null;
    private DIRECTION desiredNextDirection = null;

    public Player(String id, String name, int playerNum) {
        this.id = id;
        this.name = name;
        this.playerNum = playerNum;

        // Initialize starting data
        PlayerStartingData data = startingData[playerNum];
        color = data.color;
        direction = data.dir;
        x = data.x;
        y = data.y;
    }

    public String toJson() {
        // TODO: Use JSON-B to eliminate the need for this method
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

    /**
     * Move a player forward one space in whatever direction they are facing currently.
     *
     * @return True if the player is still alive after moving forward one space. False otherwise.
     */
    public boolean movePlayer(short[][] s) {
        // Consume the space the player was in before the move
        s[x][y] = GameBoard.PLAYER_SPOT_TAKEN;

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
                if (y + 1 < GameBoard.BOARD_SIZE)
                    y++;
                break;
            case RIGHT:
                if (x + 1 < GameBoard.BOARD_SIZE)
                    x++;
                break;
            case LEFT:
                if (x - 1 >= 0)
                    x--;
                break;
        }

        // Check if the player is now dead after moving
        if (s[x][y] != GameBoard.SPOT_AVAILABLE) {
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

    @JsonbTransient
    public int getPlayerNum() {
        return this.playerNum;
    }
}
