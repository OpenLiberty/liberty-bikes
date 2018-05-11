package org.libertybikes.game.core;

public enum DIRECTION {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    public static DIRECTION opposite(DIRECTION dir) {
        if (dir == UP)
            return DOWN;
        if (dir == DOWN)
            return UP;
        if (dir == LEFT)
            return RIGHT;
        return LEFT;
    }

    public boolean isOppositeOf(DIRECTION dir) {
        return this == DIRECTION.opposite(dir);
    }
}
