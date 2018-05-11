package org.libertybikes.game.maps;

import org.libertybikes.game.core.GameBoard;
import org.libertybikes.game.core.MovingObstacle;
import org.libertybikes.game.core.Obstacle;

public class Smile extends GameMap {

    public Smile() {
        obstacles.add(new Obstacle(12, 1, (GameBoard.BOARD_SIZE / 10) * 3, GameBoard.BOARD_SIZE / 3));
        obstacles.add(new Obstacle(12, 1, (GameBoard.BOARD_SIZE / 10) * 3, GameBoard.BOARD_SIZE / 3 + 12));
        obstacles.add(new Obstacle(1, 11, (GameBoard.BOARD_SIZE / 10) * 3, GameBoard.BOARD_SIZE / 3 + 1));
        obstacles.add(new Obstacle(1, 11, (GameBoard.BOARD_SIZE / 10) * 3 + 11, GameBoard.BOARD_SIZE / 3 + 1));
        movingObstacles.add(new MovingObstacle(2, 2, (GameBoard.BOARD_SIZE / 10) * 3 + 8, GameBoard.BOARD_SIZE / 3 + 8, 1, 1));

        obstacles.add(new Obstacle(12, 1, (GameBoard.BOARD_SIZE / 5) * 3, GameBoard.BOARD_SIZE / 3));
        obstacles.add(new Obstacle(12, 1, (GameBoard.BOARD_SIZE / 5) * 3, GameBoard.BOARD_SIZE / 3 + 12));
        obstacles.add(new Obstacle(1, 11, (GameBoard.BOARD_SIZE / 5) * 3, GameBoard.BOARD_SIZE / 3 + 1));
        obstacles.add(new Obstacle(1, 11, (GameBoard.BOARD_SIZE / 5) * 3 + 11, GameBoard.BOARD_SIZE / 3 + 1));
        movingObstacles.add(new MovingObstacle(2, 2, (GameBoard.BOARD_SIZE / 5) * 3 + 3, GameBoard.BOARD_SIZE / 3 + 8, -1, -1));

        movingObstacles.add(new MovingObstacle(6, 6, (GameBoard.BOARD_SIZE / 2 - 3), GameBoard.BOARD_SIZE / 2, 1, 1));
        obstacles.add(new Obstacle((GameBoard.BOARD_SIZE / 2), 1, GameBoard.BOARD_SIZE / 4, (GameBoard.BOARD_SIZE / 4) * 3 - 10));
        for (int i = 0; i < 10; i++) {
            obstacles.add(new Obstacle(1, 1, GameBoard.BOARD_SIZE / 4 - i, (GameBoard.BOARD_SIZE / 4) * 3 - i - 10));
            obstacles.add(new Obstacle(1, 1, GameBoard.BOARD_SIZE / 4 + i + (GameBoard.BOARD_SIZE / 4) * 2, (GameBoard.BOARD_SIZE / 4) * 3 - i - 10));
        }
    }
}