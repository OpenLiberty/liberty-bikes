/**
 *
 */
package org.libertybikes.game.core;

import javax.json.bind.annotation.JsonbCreator;

public class Obstacle {

    public final int height;

    public final int width;

    public int x;

    public int y;

    @JsonbCreator
    public Obstacle(int w, int h, int x, int y) {
        this.height = h;
        this.width = w;
        this.x = x;
        this.y = y;
    }

}
