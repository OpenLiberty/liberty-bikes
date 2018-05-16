package org.libertybikes.game.maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.naming.InitialContext;

import org.libertybikes.game.core.DIRECTION;
import org.libertybikes.game.core.GameBoard;
import org.libertybikes.game.core.GameBoard.Point;
import org.libertybikes.game.core.MovingObstacle;
import org.libertybikes.game.core.Obstacle;

public class GameMap {

    public static final int NUM_MAPS = 4; // Not including empty map
    private static final Random r = new Random();

    /**
     * @param map -1=random, 0=empty, >0=specific map
     */
    public static GameMap create(int map) {
        try {
            int mapOverride = InitialContext.doLookup("round/map");
            if (mapOverride >= 0 && mapOverride <= NUM_MAPS) {
                map = mapOverride;
                System.out.println("Overriding map selection to map #" + map);
            }
        } catch (Exception ignore) {
        }

        switch (map) {
            case -1:
                return create(r.nextInt(NUM_MAPS) + 1);
            case 0:
                return new EmptyMap();
            case 1:
                return new OriginalMap();
            case 2:
                return new CrossSlice();
            case 3:
                return new FakeBlock();
            case 4:
                return new Smile();
            case 5:
                return new HulkSmash();
            default:
                throw new IllegalArgumentException("Illegal map number: " + map);
        }
    }

    protected final ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    protected final ArrayList<MovingObstacle> movingObstacles = new ArrayList<MovingObstacle>();
    protected DIRECTION[] startingDirections = new DIRECTION[] { DIRECTION.RIGHT, DIRECTION.DOWN, DIRECTION.UP, DIRECTION.LEFT };
    protected Point[] startingPoints = new Point[] {
                                                     new Point(9, 9),
                                                     new Point(GameBoard.BOARD_SIZE - 11, 9),
                                                     new Point(9, GameBoard.BOARD_SIZE - 11),
                                                     new Point(GameBoard.BOARD_SIZE - 11, GameBoard.BOARD_SIZE - 11)
    };

    public final Point startingPosition(int playerNum) {
        return startingPoints[playerNum];
    }

    public final DIRECTION startingDirection(int playerNum) {
        return startingDirections[playerNum];
    }

    public final List<Obstacle> getObstacles() {
        return obstacles;
    }

    public final List<MovingObstacle> getMovingObstacles() {
        return movingObstacles;
    }
}
