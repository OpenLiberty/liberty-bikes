export class Ranking {
  public rank: number;
  public name: string;
  public numWins: number;
  public winLossRatio: string;
  public totalGames: number;
  public rating: number;

  constructor(rank: number, name: string, numWins: number, totalGames: number, rating: number) {
    this.rank = rank;
    this.name = name;
    this.numWins = numWins;
    this.totalGames = totalGames;
    this.winLossRatio = totalGames === 0 ? '--' : Number((numWins / totalGames) * 100).toFixed();
    this.rating = rating;
  }
}
