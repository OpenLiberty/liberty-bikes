/**
 *
 */
package org.libertybikes.game.core;

import org.libertybikes.game.core.Player.DIRECTION;

public class PlayerFactory {

    public static int MAX_PLAYERS = PlayerData.values().length;

    private static enum PlayerData {
        START_1("#DF740C", 10, 10, DIRECTION.RIGHT),
        START_2("#FF0000", 10, GameRound.BOARD_SIZE - 10, DIRECTION.UP),
        START_3("#6FC3DF", GameRound.BOARD_SIZE - 10, 10, DIRECTION.DOWN),
        START_4("#FFE64D", GameRound.BOARD_SIZE - 10, GameRound.BOARD_SIZE - 10, DIRECTION.LEFT);

        public final String color;
        public final int x;
        public final int y;
        public final DIRECTION dir;

        PlayerData(String color, int x, int y, DIRECTION dir) {
            this.color = color;
            this.x = x;
            this.y = y;
            this.dir = dir;
        }
    }

    private static final PlayerData[] startingData = new PlayerData[] { PlayerData.START_1, PlayerData.START_2, PlayerData.START_3, PlayerData.START_4 };

    public static Player initNextPlayer(GameRound g, String name) {
        PlayerData data = startingData[g.getPlayers().size()];
        Player p = new Player(data.color, data.x, data.y);
        p.direction = data.dir;
        p.playerName = name;
        return p;
    }
}
