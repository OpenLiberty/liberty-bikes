package org.libertybikes.game.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.json.bind.annotation.JsonbTransient;

import org.libertybikes.game.maps.GameMap;

@JsonbPropertyOrder({ "id", "name", "color", "status", "alive", "x", "y", "width", "height", "direction" })
public class Player {

    public static enum STATUS {
        Connected,
        Alive,
        Dead,
        Winner,
        Disconnected
    }

    public static final int PLAYER_SIZE = 3; // squares
    public static final int MAX_PLAYERS = 4;
    public static final String[] PLAYER_COLORS = {};

    // Properties exposed by JSON-B
    public final String name;
    public final String id;
    public final String color;
    public int x = 0, y = 0;
    public final int width = PLAYER_SIZE, height = PLAYER_SIZE;
    private boolean isAlive = true;
    private STATUS playerStatus = STATUS.Connected;

    protected final short playerNum;
    protected DIRECTION direction;
    private DIRECTION[] directionLastTick = { null, null };
    private Optional<DIRECTION> desiredNextDirection = Optional.empty();

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

    public Player(String id, String name, short playerNum) {
        this.id = id;
        this.name = name;
        this.playerNum = playerNum;

        // Initialize starting data
        if (playerNum >= MAX_PLAYERS)
            throw new IllegalArgumentException("Cannot create player number " + playerNum + " because MAX_PLAYERS=" + MAX_PLAYERS);
        if (playerNum == 0) {
            color = "#f28415"; // orange
        } else if (playerNum == 1) {
            color = "#ABD155"; // bright green
        } else if (playerNum == 2) {
            color = "#6FC3DF"; // light blue
        } else {
            color = "#c178c9"; // light purple
        }
    }

    public DIRECTION getDirection() {
        return desiredNextDirection.orElse(direction);
    }

    public void setDirection(DIRECTION newDirection) {
        if (newDirection == direction)
            return;

        // Make sure the player doesn't move backwards on themselves
        if (directionLastTick[0] != null) {
            if (newDirection.isOppositeOf(directionLastTick[0]) || newDirection.isOppositeOf(directionLastTick[1])) {
                desiredNextDirection = Optional.of(newDirection);
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
    public final boolean movePlayer(short[][] board) {
        // If a player issues two moves in the same game tick and the second direction would kill themselves,
        // spread out the moves across 2-3 ticks rather than ignoring the second move entirely
        if (desiredNextDirection.isPresent() && directionLastTick[0] == direction && directionLastTick[1] == direction) {
            setDirection(desiredNextDirection.get());
            desiredNextDirection = Optional.empty();
        }

        switch (direction) {
            case UP:
                if (y - 1 < 0 || checkCollision(board, x, y - 1)) {
                    setStatus(STATUS.Dead);
                    return isAlive();
                }
                moveUp(board);
                break;
            case DOWN:
                if (y + height + 1 >= GameBoard.BOARD_SIZE || checkCollision(board, x, y + 1)) {
                    setStatus(STATUS.Dead);
                    return isAlive();
                }
                moveDown(board);
                break;
            case RIGHT:
                if (x + width + 1 >= GameBoard.BOARD_SIZE || checkCollision(board, x + 1, y)) {
                    setStatus(STATUS.Dead);
                    return isAlive();
                }
                moveRight(board);
                break;
            case LEFT:
                if (x - 1 < 0 || checkCollision(board, x - 1, y)) {
                    setStatus(STATUS.Dead);
                    return isAlive();
                }
                moveLeft(board);
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
                        board[tp.x][tp.y] = GameBoard.TRAIL_SPOT_TAKEN;
                        it.remove();
                        first = false;
                    } else {
                        board[tp.x][tp.y] = GameBoard.TRAIL_SPOT_TAKEN;
                        it.remove();
                        break;
                    }
                }
            }
        }

        directionLastTick[1] = directionLastTick[0];
        directionLastTick[0] = direction;

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
    public short getPlayerNum() {
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

    public void setAI(AI ai) {
        this.ai = ai;
    }

    public Player addTo(GameMap map) {
        this.x = map.startingPosition(playerNum).x;
        this.y = map.startingPosition(playerNum).y;
        this.direction = map.startingDirection(playerNum);
        return this;
    }
}
