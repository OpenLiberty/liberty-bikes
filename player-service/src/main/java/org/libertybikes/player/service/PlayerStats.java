package org.libertybikes.player.service;

public class PlayerStats {

    public int totalGames;

    public int numWins;

    public double winLossRatio() {
        return numWins / totalGames;
    }

}
