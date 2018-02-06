import * as $ from 'jquery';
import { GameWebsocket } from './websocket';

export class Whiteboard {
  canvas: any;
  context: any;
  gamesocket: GameWebsocket;

  constructor() {
    this.canvas = document.getElementById('myCanvas');
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

  getCurrentPos(evt) {
    const rect = this.canvas.getBoundingClientRect();
    return {
      x: evt.clientX - rect.left,
      y: evt.clientY - rect.top
    };
  }

  drawImageText(image) {
    const json = JSON.parse(image);
    this.context.fillStyle = json.color;
    switch (json.shape) {
      case 'circle':
        this.context.beginPath();
        this.context.arc(
          json.coords.x,
          json.coords.y,
          5,
          0,
          2 * Math.PI,
          false
        );
        this.context.fill();
        break;
      case 'square':
      default:
        this.context.fillRect(json.coords.x, json.coords.y, 5, 5);
        break;
    }
  }

  drawImageBinary(blob) {
    const bytes = new Uint8Array(blob);

    const imageData = this.context.createImageData(
      this.canvas.width,
      this.canvas.height
    );

    for (let i = 8; i < imageData.data.length; i++) {
      imageData.data[i] = bytes[i];
    }
    this.context.putImageData(imageData, 0, 0);
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
      return '<span class=\'label label-primary\'>Connected</span>';
    }
    if (status === 'Alive' || status === 'Winner') {
      return `<span class='label label-success'>${status}</span>`;
    }
    if (status === 'Dead') {
      return '<span class=\'label label-danger\'>Dead</span>';
    }
    if (status === 'Disconnected') {
      return '<span class=\'label label-default\'>Disconnected</span>';
    }
  }

  startGame() {
    this.gamesocket.sendText(JSON.stringify({ message: 'GAME_START' }));
  }

  pauseGame() {
    this.gamesocket.sendText(JSON.stringify({ message: 'GAME_PAUSE' }));
  }

  requeue() {
    this.gamesocket.sendText(JSON.stringify({ message: 'GAME_REQUEUE' }));
  }
}
