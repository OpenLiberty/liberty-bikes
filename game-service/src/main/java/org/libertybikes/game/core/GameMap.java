package org.libertybikes.game.core;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GameMap {

    int[][] playerPositions = new int[4][2];
    ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    ArrayList<MovingObstacle> movingObstacles = new ArrayList<MovingObstacle>();

    // -1 for random
    // 0 for empty
    // > 0 for specific map
    public GameMap(int map) {
        //set default playerPositions
        //these are overridden in some maps
        if (map == 0) {
            setDefaultPlayerPositions();
            return;
        }

        if (map == -1) {
            int mapNumTotal = 4;
            map = ThreadLocalRandom.current().nextInt(1, mapNumTotal + 1);
        }

        if (map == 1) {
            map1();
        } else if (map == 2) {
            map2();
        } else if (map == 3) {
            map3();
        } else if (map == 4) {
            map4();
        }
    }

    public GameMap(boolean test) {
        //don't use this, it is for testing an empty map
    }

    private void map1() {
        setDefaultPlayerPositions();
        //Standard
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

    private void map2() {

        //Cross Slice

        obstacles.add(new Obstacle(50, 1, GameBoard.BOARD_SIZE / 2 - 25, GameBoard.BOARD_SIZE / 2));
        obstacles.add(new Obstacle(1, 24, GameBoard.BOARD_SIZE / 2, GameBoard.BOARD_SIZE / 2 - 24));
        obstacles.add(new Obstacle(1, 24, GameBoard.BOARD_SIZE / 2, GameBoard.BOARD_SIZE / 2 + 1));

        movingObstacles.add(new MovingObstacle(1, 5, GameBoard.BOARD_SIZE / 2, GameBoard.BOARD_SIZE / 8, 0, 1));
        movingObstacles.add(new MovingObstacle(1, 5, GameBoard.BOARD_SIZE / 2, (GameBoard.BOARD_SIZE / 8) * 7, 0, -1));
        movingObstacles.add(new MovingObstacle(5, 1, GameBoard.BOARD_SIZE / 8, GameBoard.BOARD_SIZE / 2, 1, 0));
        movingObstacles.add(new MovingObstacle(5, 1, (GameBoard.BOARD_SIZE / 8) * 6, GameBoard.BOARD_SIZE / 2, -1, 0));

        playerPositions = new int[4][2];
        playerPositions[0][0] = GameBoard.BOARD_SIZE / 2 - 15;
        playerPositions[0][1] = GameBoard.BOARD_SIZE / 2 - 15;
        playerPositions[1][0] = GameBoard.BOARD_SIZE / 2 + 15;
        playerPositions[1][1] = GameBoard.BOARD_SIZE / 2 - 15;
        playerPositions[2][0] = GameBoard.BOARD_SIZE / 2 - 15;
        playerPositions[2][1] = GameBoard.BOARD_SIZE / 2 + 15;
        playerPositions[3][0] = GameBoard.BOARD_SIZE / 2 + 15;
        playerPositions[3][1] = GameBoard.BOARD_SIZE / 2 + 15;
    }

    private void map3() {
        setDefaultPlayerPositions();
        //Fake Block
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

    private void map4() {
        setDefaultPlayerPositions();
        //Smile
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

    private void setDefaultPlayerPositions() {
        playerPositions[0][0] = 9;
        playerPositions[0][1] = 9;
        playerPositions[1][0] = GameBoard.BOARD_SIZE - 11;
        playerPositions[1][1] = 9;
        playerPositions[2][0] = 9;
        playerPositions[2][1] = GameBoard.BOARD_SIZE - 11;
        playerPositions[3][0] = GameBoard.BOARD_SIZE - 11;
        playerPositions[3][1] = GameBoard.BOARD_SIZE - 11;
    }

    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }

    public ArrayList<MovingObstacle> getMovingObstacles() {
        return movingObstacles;
    }

    public int[][] getPlayerPositions() {
        return playerPositions;
    }

    public int getPlayerPositionX(short playerNum) {
        return playerPositions[playerNum][0];
    }

    public int getPlayerPositionY(short playerNum) {
        return playerPositions[playerNum][1];
    }
}
