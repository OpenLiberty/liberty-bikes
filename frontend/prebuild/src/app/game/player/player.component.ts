import { Component, OnInit, Input } from '@angular/core';
import { Player } from '../../entity/player';
import { Constants } from '../constants';
import { Assets } from '../assets';

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
    if (!this.player || !this.player.status) {
      return '';
    }

    let status = this.player.status.toLowerCase();
    if (status === 'none') {
      return '';
    }

    if (status === 'connected') {
      status = 'alive';
    }

    if (status === 'alive') {
      let filename: string;
      switch (this.player.color.toLowerCase()) {
        case Constants.GREEN_COLOR:
          filename = Constants.GREEN_FILENAME;
          break;
        case Constants.BLUE_COLOR:
          filename = Constants.BLUE_FILENAME;
          break;
        case Constants.ORANGE_COLOR:
          filename = Constants.ORANGE_FILENAME;
          break;
        case Constants.PURPLE_COLOR:
          filename = Constants.PURPLE_FILENAME;
          break;
        default:
          console.warn('Player color did not match available images. Defaulting to green.');
          filename = Constants.GREEN_FILENAME;
          break;
      }
      return `/assets/images/${filename}`;
    } else {
      return `/assets/images/status_${status}.png`;
    }
  }

  backgroundColor(color: string): string {
    return `${color}30`;
  }

}
