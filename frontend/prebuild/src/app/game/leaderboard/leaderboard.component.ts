import { Component, OnInit, NgZone } from '@angular/core';
import { Ranking } from './ranking/ranking';
import { HttpClient } from '@angular/common/http';
import { environment } from './../../../environments/environment';
import { timer } from 'rxjs/observable/timer';
import { trigger, transition, style, animate } from '@angular/animations';

@Component({
  selector: 'rank-list',
  templateUrl: './leaderboard.component.html',
  styleUrls: ['./leaderboard.component.scss'],
  animations: [
    trigger('swap', [
      transition(':enter', [
        style({transform: 'translateY(100%)', opacity: 0}),
        animate('1000ms', style({transform: 'translateY(0)', opacity: 1}))
      ]),
      transition(':leave', [
        style({transform: 'translateY(0)', opacity: 1}),
        animate('1000ms', style({transform: 'translateY(-100%)', opacity: 0}))
      ])
    ])
  ]
})
export class LeaderboardComponent implements OnInit {
  rankings: Ranking[] = new Array();
  board: LeaderboardType = 'wins';

  get currentBoard(): LeaderboardType {
    return this.board;
  }

  set currentBoard(newBoard: LeaderboardType) {
    this.board = newBoard;
  }



  constructor(private ngZone: NgZone, private http: HttpClient) {

  }

  ngOnInit() {
    this.getLeaders();
    let leaderboardTimer = timer(0, 12* 1000);
    leaderboardTimer.subscribe((t) => this.nextLeaderboard(t));
  }

  async getLeaders() {
    try {
      let data = await this.http.get(`${environment.API_URL_RANKS}/top/10`).toPromise();
      console.log(`Got leaders: ${JSON.stringify(data)}`);
      const json = data as any;
      const rankingsArr = new Array();
      let i = 1;
      for (let ranking of json) {
        rankingsArr.push(new Ranking(i++, ranking.name, ranking.stats.numWins, ranking.stats.totalGames, ranking.stats.rating));
      }
      this.ngZone.run(() => {
        this.rankings = rankingsArr;
      });

    } catch (error) {
      console.log(error);
    }
  }

  get winsRankings(): Ranking[] {
    return this.rankings.sort((a, b) => b.numWins - a.numWins);
  }

  get gamesRankings(): Ranking[] {
    return this.rankings.sort((a, b) => b.totalGames - a.totalGames);
  }

  get ratioRankings(): Ranking[] {
    return this.rankings.sort((a, b) => b.winLossRatio - a.winLossRatio);
  }

  get ratingRankings(): Ranking[] {
    return this.rankings.sort((a, b) => b.rating - a.rating);
  }

  nextLeaderboard(board: number) {
    switch (board % 4) {
      case 0:
        this.currentBoard = 'wins';
        break;
      case 1:
        this.currentBoard = 'totalGames';
        break;
      case 2:
        this.currentBoard = 'winLossRatio';
        break;
      default:
        this.currentBoard = 'rating';
        break;
    }
  }
}

export type LeaderboardType = 'wins' | 'totalGames' | 'winLossRatio' | 'rating' | 'none';

