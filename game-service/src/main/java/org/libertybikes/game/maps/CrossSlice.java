package org.libertybikes.game.maps;

import org.libertybikes.game.core.DIRECTION;
import org.libertybikes.game.core.GameBoard;
import org.libertybikes.game.core.GameBoard.Point;
import org.libertybikes.game.core.MovingObstacle;
import org.libertybikes.game.core.Obstacle;

public class CrossSlice extends GameMap {

    public CrossSlice() {
        obstacles.add(new Obstacle(50, 1, GameBoard.BOARD_SIZE / 2 - 25, GameBoard.BOARD_SIZE / 2));
        obstacles.add(new Obstacle(1, 24, GameBoard.BOARD_SIZE / 2, GameBoard.BOARD_SIZE / 2 - 24));
        obstacles.add(new Obstacle(1, 24, GameBoard.BOARD_SIZE / 2, GameBoard.BOARD_SIZE / 2 + 1));

        movingObstacles.add(new MovingObstacle(1, 5, GameBoard.BOARD_SIZE / 2, GameBoard.BOARD_SIZE / 8, 0, 1));
        movingObstacles.add(new MovingObstacle(1, 5, GameBoard.BOARD_SIZE / 2, (GameBoard.BOARD_SIZE / 8) * 7, 0, -1));
        movingObstacles.add(new MovingObstacle(5, 1, GameBoard.BOARD_SIZE / 8, GameBoard.BOARD_SIZE / 2, 1, 0));
        movingObstacles.add(new MovingObstacle(5, 1, (GameBoard.BOARD_SIZE / 8) * 6, GameBoard.BOARD_SIZE / 2, -1, 0));

        startingPoints = new Point[] {
                                       new Point(GameBoard.BOARD_SIZE / 2 - 15, GameBoard.BOARD_SIZE / 2 - 15),
                                       new Point(GameBoard.BOARD_SIZE / 2 + 15, GameBoard.BOARD_SIZE / 2 - 15),
                                       new Point(GameBoard.BOARD_SIZE / 2 - 15, GameBoard.BOARD_SIZE / 2 + 15),
                                       new Point(GameBoard.BOARD_SIZE / 2 + 15, GameBoard.BOARD_SIZE / 2 + 15)
        };
        startingDirections = new DIRECTION[] { DIRECTION.UP, DIRECTION.UP, DIRECTION.DOWN, DIRECTION.DOWN };
    }
}