package org.libertybikes.game.maps;

import org.libertybikes.game.core.GameBoard;
import org.libertybikes.game.core.MovingObstacle;
import org.libertybikes.game.core.Obstacle;

public class FakeBlock extends GameMap {

    public FakeBlock() {
        int size = 4;
        int offset = 2;
        int speed = 2;
        movingObstacles.add(new MovingObstacle(size, size, GameBoard.BOARD_SIZE / 2 - size - offset, GameBoard.BOARD_SIZE / 2 - size - offset, -1, -1, speed));
        movingObstacles.add(new MovingObstacle(size, size, GameBoard.BOARD_SIZE / 2 - offset, GameBoard.BOARD_SIZE / 2 - size - offset, 0, -1, speed));
        movingObstacles.add(new MovingObstacle(size, size, GameBoard.BOARD_SIZE / 2 + size - offset, GameBoard.BOARD_SIZE / 2 - size - offset, 1, -1, speed));
        movingObstacles.add(new MovingObstacle(size, size, GameBoard.BOARD_SIZE / 2 - size - offset, GameBoard.BOARD_SIZE / 2 - offset, -1, 0, speed));
        obstacles.add(new Obstacle(size, size, GameBoard.BOARD_SIZE / 2 - offset, GameBoard.BOARD_SIZE / 2 - offset));
        movingObstacles.add(new MovingObstacle(size, size, GameBoard.BOARD_SIZE / 2 + size - offset, GameBoard.BOARD_SIZE / 2 - offset, 1, 0, speed));
        movingObstacles.add(new MovingObstacle(size, size, GameBoard.BOARD_SIZE / 2 - size - offset, GameBoard.BOARD_SIZE / 2 + size - offset, -1, 1, speed));
        movingObstacles.add(new MovingObstacle(size, size, GameBoard.BOARD_SIZE / 2 - offset, GameBoard.BOARD_SIZE / 2 + size - offset, 0, 1, speed));
        movingObstacles.add(new MovingObstacle(size, size, GameBoard.BOARD_SIZE / 2 + size - offset, GameBoard.BOARD_SIZE / 2 + size - offset, 1, 1, speed));
    }

}