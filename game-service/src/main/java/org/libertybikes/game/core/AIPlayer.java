/**
 *
 */
package org.libertybikes.game.core;

public abstract class AIPlayer extends Player {

    public AIPlayer(String id, String name, short playerNum) {
        super(id, name, playerNum);
    }

    public void broadCastBoard(short[][] board) {}

}
