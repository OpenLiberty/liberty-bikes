export class Ranking {
  public rank: number;
  public name: string;
  public numWins: number;
  public winLossRatio: number;
  public totalGames: number;
  public rating: number;

  constructor(rank: number, name: string, numWins: number, totalGames: number, rating: number) {
    this.rank = rank;
    this.name = name;
    this.numWins = numWins;
    this.totalGames = totalGames;
    this.winLossRatio = totalGames === 0 ? 0 : (numWins / totalGames) * 100;
    this.rating = rating;
  }
}
