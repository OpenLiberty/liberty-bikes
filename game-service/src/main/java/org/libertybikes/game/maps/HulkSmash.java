package org.libertybikes.game.maps;

import static org.libertybikes.game.core.GameBoard.BOARD_SIZE;

import org.libertybikes.game.core.DIRECTION;
import org.libertybikes.game.core.GameBoard.Point;
import org.libertybikes.game.core.MovingObstacle;
import org.libertybikes.game.core.Obstacle;

public class HulkSmash extends GameMap {
    public HulkSmash() {
        createPair(10, 10);
        createPair(30, BOARD_SIZE / 2 - 5);
        createPair(10, BOARD_SIZE - 20);

        startingPoints = new Point[] {
                                       new Point(BOARD_SIZE / 2 - 10, 10),
                                       new Point(BOARD_SIZE / 2 + 10, 10),
                                       new Point(BOARD_SIZE / 2 - 10, BOARD_SIZE - 10),
                                       new Point(BOARD_SIZE / 2 + 10, BOARD_SIZE - 10)
        };

        startingDirections = new DIRECTION[] {
                                               DIRECTION.DOWN, DIRECTION.DOWN, DIRECTION.UP, DIRECTION.UP
        };
    }

    private void createPair(int x, int y) {
        movingObstacles.add(new MovingObstacle(2, 10, x, y, -1, 0));
        movingObstacles.add(new MovingObstacle(2, 10, BOARD_SIZE - (x + 3), y, 1, 0));

        // [
        obstacles.add(new Obstacle(2, 14, 0, y - 2));
        obstacles.add(new Obstacle(2, 2, 2, y - 2));
        obstacles.add(new Obstacle(2, 2, 2, y + 10));

        // ]
        obstacles.add(new Obstacle(2, 14, BOARD_SIZE - 3, y - 2));
        obstacles.add(new Obstacle(2, 2, BOARD_SIZE - 5, y - 2));
        obstacles.add(new Obstacle(2, 2, BOARD_SIZE - 5, y + 10));
    }
}