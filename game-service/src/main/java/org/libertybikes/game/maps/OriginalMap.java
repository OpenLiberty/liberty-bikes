package org.libertybikes.game.maps;

import org.libertybikes.game.core.GameBoard;
import org.libertybikes.game.core.MovingObstacle;
import org.libertybikes.game.core.Obstacle;

public class OriginalMap extends GameMap {
    public OriginalMap() {
        movingObstacles.add(new MovingObstacle(5, 5, GameBoard.BOARD_SIZE / 2 - 10, GameBoard.BOARD_SIZE / 3, -1, -1));
        movingObstacles.add(new MovingObstacle(5, 5, GameBoard.BOARD_SIZE / 2 + 10, (GameBoard.BOARD_SIZE / 3 * 2) - 5, 1, 1));

        // Creating some walls
        // TopLeft
        obstacles.add(new Obstacle(15, 1, GameBoard.BOARD_SIZE / 8, GameBoard.BOARD_SIZE / 8));
        obstacles.add(new Obstacle(1, 14, GameBoard.BOARD_SIZE / 8, GameBoard.BOARD_SIZE / 8 + 1));
        // TopRight
        obstacles.add(new Obstacle(15, 1, ((GameBoard.BOARD_SIZE / 8) * 7) - 14, GameBoard.BOARD_SIZE / 8));
        obstacles.add(new Obstacle(1, 14, (GameBoard.BOARD_SIZE / 8) * 7, GameBoard.BOARD_SIZE / 8 + 1));
        // BottomLeft
        obstacles.add(new Obstacle(15, 1, GameBoard.BOARD_SIZE / 8, (GameBoard.BOARD_SIZE / 8) * 7));
        obstacles.add(new Obstacle(1, 14, GameBoard.BOARD_SIZE / 8, ((GameBoard.BOARD_SIZE / 8) * 7) - 14));
        // BottomRight
        obstacles.add(new Obstacle(15, 1, ((GameBoard.BOARD_SIZE / 8) * 7) - 14, (GameBoard.BOARD_SIZE / 8) * 7));
        obstacles.add(new Obstacle(1, 14, (GameBoard.BOARD_SIZE / 8) * 7, ((GameBoard.BOARD_SIZE / 8) * 7) - 14));
    }
}