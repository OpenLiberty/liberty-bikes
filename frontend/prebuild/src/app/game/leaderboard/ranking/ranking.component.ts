import { Component, OnInit, Input } from '@angular/core';
import { Ranking } from './ranking';

@Component({
  selector: 'ranking',
  templateUrl: './ranking.component.html',
  styleUrls: ['./ranking.component.scss']
})
export class RankingComponent implements OnInit {
  @Input() ranking: Ranking;
  constructor() {
  }

  ngOnInit() {
  }

}
