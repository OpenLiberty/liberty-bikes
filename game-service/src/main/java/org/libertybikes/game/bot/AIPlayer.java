/**
 *
 */
package org.libertybikes.game.bot;

import org.libertybikes.game.core.AI;
import org.libertybikes.game.core.DIRECTION;

public abstract class AIPlayer implements AI {

    protected final int startX, startY, height, width;
    protected final short takenSpotNumber;
    public DIRECTION startDirection;

    public AIPlayer(int startX, int startY, int width, int height, DIRECTION startDirection, short takenSpotNumber) {
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.startDirection = startDirection;
        this.takenSpotNumber = takenSpotNumber;
    }

    @Override
    public abstract DIRECTION processGameTick(short[][] board);

}
