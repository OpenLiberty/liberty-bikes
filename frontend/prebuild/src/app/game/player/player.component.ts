import { Component, OnInit, Input } from '@angular/core';
import { Player } from '../../entity/player';

@Component({
  selector: 'app-player',
  templateUrl: './player.component.html',
  styleUrls: ['./player.component.scss']
})
export class PlayerComponent implements OnInit {
  @Input() player: Player;
  constructor() {
  }

  ngOnInit() {
  }

  get style(): any {
    const style = {
      'border-left': `5px solid ${this.color}`,
      'background-color': `${this.backgroundColor(this.color)}`
    };

    return style;
  }

  get color(): string {
    if (this.player.status === 'Dead' || this.player.status === 'Disconnected') {
      return '#BBBBBB';
    }

    return this.player.color;
  }

  get statusImage() {
    let status = this.player.status.toLowerCase();
    if (status === 'none') {
      return '';
    }

    if (status === 'connected') {
      status = 'alive';
    }

    if (status === 'alive') {
      return `/assets/images/bike_full.png`;
    } else {
      return `/assets/images/status_${status}.png`;
    }
  }

  backgroundColor(color: string): string {
    return `${color}30`;
  }

}
