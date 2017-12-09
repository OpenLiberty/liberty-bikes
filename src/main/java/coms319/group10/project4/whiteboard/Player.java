/**
 *
 */
package coms319.group10.project4.whiteboard;

import java.io.IOException;

import javax.websocket.Session;

/**
 * @author Andrew
 */
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

    public static final int PLAYER_SIZE = 5;
    public final Game game;
    public Session client;
    public final String color;
    public DIRECTION direction = DIRECTION.RIGHT;
    public int x;
    public int y;
    public String playerName;
    public boolean isAlive = true;
    private STATUS playerStatus = STATUS.Connected;

    public Player(Game g, Session client, String color) {
        this.game = g;
        this.color = color;
        this.client = client;
    }

    public Player(Game g, Session client, String color, int xstart, int ystart) {
        this.game = g;
        this.color = color;
        this.client = client;
        x = xstart;
        y = ystart;
    }

    public boolean movePlayer() {
        game.board[x / PLAYER_SIZE][y / PLAYER_SIZE] = false;
        switch (direction) {
            case UP:
                if (y - PLAYER_SIZE >= 0)
                    y -= PLAYER_SIZE;
                break;
            case DOWN:
                if (y + PLAYER_SIZE < Game.GAME_SIZE)
                    y += PLAYER_SIZE;
                break;
            case RIGHT:
                if (x + PLAYER_SIZE < Game.GAME_SIZE)
                    x += PLAYER_SIZE;
                break;
            case LEFT:
                if (x - PLAYER_SIZE >= 0)
                    x -= PLAYER_SIZE;
                break;
        }
        boolean checkResult = checkPosition(x, y);
        if (!checkResult) {
            setStatus(STATUS.Dead);
        }
        return checkResult;
    }

    public String toJson() {
        // {"shape":"square","color":"#FF0000","coords":{"x":251,"y":89}}
        StringBuffer sb = new StringBuffer("{\"shape\":\"square\",\"color\":\"");
        sb.append(this.color);
        sb.append("\",\"coords\":{\"x\":");
        sb.append(this.x);
        sb.append(",\"y\":");
        sb.append(this.y);
        sb.append("}}");
        return sb.toString();
    }

    public void setDirection(String dir) {
        direction = DIRECTION.valueOf(dir);
    }

    public DIRECTION getDrirection() {
        return direction;
    }

    private boolean checkPosition(int x, int y) {
        int realXPosition = x / PLAYER_SIZE;
        int realYPosition = y / PLAYER_SIZE;
        return game.board[realXPosition][realYPosition];
    }

    public void sendTextToClient(String message) {
        if (client != null) {
            try {
                client.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        this.client = null;
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
}
