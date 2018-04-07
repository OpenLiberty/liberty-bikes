import { Component, OnInit, NgZone } from '@angular/core';
import { Ranking } from './ranking/ranking';
import { HttpClient } from '@angular/common/http';
import { environment } from './../../../environments/environment';

@Component({
  selector: 'rank-list',
  templateUrl: './leaderboard.component.html',
  styleUrls: ['./leaderboard.component.scss']
})
export class LeaderboardComponent implements OnInit {
  rankings: Ranking[] = new Array();

  constructor(private ngZone: NgZone, private http: HttpClient) {
  }

  ngOnInit() {
    this.getLeaders();
  }
  
  async getLeaders() {
    try {
      console.log("Calling rank service");
      let data = await this.http.get(`${environment.API_URL_RANKS}/top/10`).toPromise();
      console.log(`Got leaders: ${JSON.stringify(data)}`);
      const json = data as any;
      const rankingsArr = new Array();
      let i = 1;
      for (let ranking of json) {
        console.log(`Got rank ${JSON.stringify(ranking)}`);
        rankingsArr.push(new Ranking(i++, ranking.name, ranking.stats.numWins, ranking.stats.totalGames, ranking.stats.rating));
      }
      this.ngZone.run(() => {
        this.rankings = rankingsArr;
      });
      
    } catch (error) {
      console.log(error);
    }

  }
}
