import { Component, OnInit, Input } from '@angular/core';
import { Player } from './player';

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

  public get style(): any {
    const style = {
      'border-left': `5px solid ${this.player.color}`,
      'background-color': `${this.backgroundColor(this.player.color)}`
    };

    return style;
  }

  public get color(): string {
    if (this.player.status === 'Dead' || this.player.status === 'Disconnected') {
      return '#BBBBBB';
    }

    return this.player.color;
  }

  get statusImage() {
    var status = this.player.status.toLowerCase();
    if (status === 'none') {
      return '';
    }

	  if (status === 'connected') {
      status = 'alive';
    }

	  return `/assets/images/status_${status}.png`;
  }

  backgroundColor(color: string): string {
    return `${color}30`;
  }

}
