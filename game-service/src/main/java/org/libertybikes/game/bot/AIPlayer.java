package org.libertybikes.game.bot;

import org.libertybikes.game.core.AI;
import org.libertybikes.game.core.DIRECTION;
import org.libertybikes.game.core.Player;
import org.libertybikes.game.maps.GameMap;

public abstract class AIPlayer implements AI {

    protected final int startX, startY, height = Player.PLAYER_SIZE, width = Player.PLAYER_SIZE;
    protected final short takenSpotNumber;
    public DIRECTION startDirection;

    public AIPlayer(GameMap map, short playerNum) {
        this(map.startingPosition(playerNum).x, map.startingPosition(playerNum).y, map.startingDirection(playerNum), playerNum);
    }

    public AIPlayer(int startX, int startY, DIRECTION startDirection, short playerNum) {
        this.startX = startX;
        this.startY = startY;
        this.startDirection = startDirection;
        this.takenSpotNumber = playerNum;
    }

    @Override
    public abstract DIRECTION processGameTick(short[][] board);

    public Player asPlayer() {
        String name = getClass().getSimpleName() + '-' + takenSpotNumber;
        Player p = new Player(name, name, takenSpotNumber);
        p.setAI(this);
        return p;
    }

}
