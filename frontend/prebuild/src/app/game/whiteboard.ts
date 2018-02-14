import * as $ from 'jquery';
import { GameWebsocket } from './websocket';

export class Whiteboard {
  static readonly BOX_SIZE = 5;
  canvas: any;
  context: any;
  gamesocket: GameWebsocket;

  constructor() {
    this.canvas = document.getElementById('gameCanvas');
    this.context = this.canvas.getContext('2d');
    this.gamesocket = new GameWebsocket(this);

    window.onkeydown = (e: KeyboardEvent): any => {
      const key = e.keyCode ? e.keyCode : e.which;

      if (key === 38) {
        this.gamesocket.sendText(JSON.stringify({ direction: 'UP' }));
      } else if (key === 40) {
        this.gamesocket.sendText(JSON.stringify({ direction: 'DOWN' }));
      } else if (key === 37) {
        this.gamesocket.sendText(JSON.stringify({ direction: 'LEFT' }));
      } else if (key === 39) {
        this.gamesocket.sendText(JSON.stringify({ direction: 'RIGHT' }));
      }
    };
  }

  drawPlayer(player) {
    this.context.fillStyle = player.color;
    this.context.fillRect(Whiteboard.BOX_SIZE * player.x, Whiteboard.BOX_SIZE * player.y,
                          Whiteboard.BOX_SIZE, Whiteboard.BOX_SIZE);
  }
  
  drawObstacle(obstacle) {
    this.context.fillStyle = '#808080'; // obstacles always grey
    this.context.fillRect(Whiteboard.BOX_SIZE * obstacle.x, Whiteboard.BOX_SIZE * obstacle.y,
                          Whiteboard.BOX_SIZE * obstacle.height, Whiteboard.BOX_SIZE * obstacle.width);
  }

  updatePlayerList(json) {
    let list = '<li class="list-group-item active">Players</li>';

    for (const player of json.playerlist) {
      list += `
<li class="list-group-item">
  <span style="color: ${player.color}; font-size: 5;">${player.name}</span>: ${this.getStatus(player.status)}
</li>
`;
    }

    $('#playerList').html(list);
  }

  getStatus(status) {
    if (status === 'Connected') {
      return '<span class=\'badge badge-pill badge-primary\'>Connected</span>';
    }
    if (status === 'Alive' || status === 'Winner') {
      return `<span class='badge badge-pill badge-success'>${status}</span>`;
    }
    if (status === 'Dead') {
      return '<span class=\'badge badge-pill badge-danger\'>Dead</span>';
    }
    if (status === 'Disconnected') {
      return '<span class=\'badge badge-pill badge-secondary\'>Disconnected</span>';
    }
  }

  startGame() {
    this.gamesocket.sendText(JSON.stringify({ message: 'GAME_START' }));
  }

  requeue() {
    this.gamesocket.sendText(JSON.stringify({ message: 'GAME_REQUEUE' }));
  }
}
