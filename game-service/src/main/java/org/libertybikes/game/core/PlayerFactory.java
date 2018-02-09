/**
 *
 */
package org.libertybikes.game.core;

import javax.websocket.Session;

import org.libertybikes.game.core.Player.DIRECTION;

/**
 * @author Andrew
 *
 */
public class PlayerFactory {

    public static int MAX_PLAYERS = PlayerData.values().length;

    private static enum PlayerData {
        START_1("#DF740C", 50, 50, DIRECTION.RIGHT),
        START_2("#FF0000", 50, GameRound.GAME_SIZE - 50, DIRECTION.UP),
        START_3("#6FC3DF", GameRound.GAME_SIZE - 50, 50, DIRECTION.DOWN),
        START_4("#FFE64D", GameRound.GAME_SIZE - 50, GameRound.GAME_SIZE - 50, DIRECTION.LEFT);

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

    public static Player initNextPlayer(GameRound g, Session client, String name) {
        PlayerData data = startingData[g.players.size()];
        Player p = new Player(client, data.color, data.x, data.y);
        p.direction = data.dir;
        p.playerName = name;
        return p;
    }
}
