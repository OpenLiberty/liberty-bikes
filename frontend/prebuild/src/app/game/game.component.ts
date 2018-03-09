import * as $ from 'jquery';

import { Component, OnInit } from '@angular/core';
import { Meta } from '@angular/platform-browser';
import { GameWebsocket } from '../net/websocket';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.scss']
})
export class GameComponent implements OnInit {
  static readonly BOX_SIZE = 5;

  gameSocket: GameWebsocket;

  roundId: string;
  serverHost: string;
  serverPort: string;

  output: HTMLElement;
  idDisplay: HTMLElement;

  canvas: any;
  context: CanvasRenderingContext2D;

  constructor(private meta: Meta) {  }

  ngOnInit() {
    this.meta.addTag({name: 'viewport', content: 'width=1600'}, true);
    this.roundId = sessionStorage.getItem('roundId');
    console.log(`Round ID: ${this.roundId}`);
    this.serverHost = document.location.hostname;
    this.serverPort = '8080';
    this.gameSocket = new GameWebsocket(this.serverHost, this.serverPort, this.roundId);

    this.output = document.getElementById('output');
    this.idDisplay = document.getElementById('gameIdDisplay');

    this.canvas = document.getElementById('gameCanvas');
    this.context = this.canvas.getContext('2d');

    this.gameSocket.messageCallback = (evt: MessageEvent): any => {
      this.onMessage(evt);
    };

    this.gameSocket.errorCallback = (evt: MessageEvent): any => {
      this.onError(evt);
    };

    this.gameSocket.openCallback = (evt: MessageEvent): any => {
      this.onConnect(evt);
    };

    window.onkeydown = (e: KeyboardEvent): any => {
      const key = e.keyCode ? e.keyCode : e.which;

      if (key === 38) {
        this.moveUp();
      } else if (key === 40) {
        this.moveDown();
      } else if (key === 37) {
        this.moveLeft();
      } else if (key === 39) {
        this.moveRight();
      }
    };
  }

  // Handlers
  onMessage(evt: MessageEvent) {
    console.log(`received: ${evt.data}`);
    if (typeof evt.data === 'string') {
      const json = JSON.parse(evt.data);
      if (json.playerlist) {
        this.updatePlayerList(json);
      }
      if (json.requeue) {
        this.roundId = json.requeue;
        sessionStorage.setItem('roundId', this.roundId);
        location.reload();
      }
      if (json.obstacles) {
        for (let obstacle of json.obstacles) {
          this.drawObstacle(obstacle);
        }
      }
      if (json.movingObstacles) {
        for (let obstacle of json.movingObstacles) {
          this.drawMovingObstacle(obstacle);
        }
      }
      if (json.players) {
        for (let player of json.players) {
          if (player.isAlive) {
            this.drawPlayer(player);
          }
        }
      }
    }
  }

  onError(evt: MessageEvent) {
    this.writeToScreen(`<span style="color: red;">ERROR:</span> ${evt.data}`);
  }

  onConnect(evt: MessageEvent) {
    if (sessionStorage.getItem('isSpectator') === 'true') {
      console.log('is a spectator... showing game id');
      // Set the Round ID and make visible
      $('#game-code').html(this.roundId);
      const gameId = $('#game-code-display');
      gameId.removeClass('d-none');
      gameId.addClass('d-inline-block');
      this.gameSocket.sendText(JSON.stringify({'spectatorjoined': true}));
    } else {
      this.gameSocket.sendText(JSON.stringify({'playerjoined': sessionStorage.getItem('userId')}));
    }
  }


  // Game actions
  startGame() {
    this.gameSocket.sendText(JSON.stringify({ message: 'GAME_START' }));
  }

  requeue() {
    this.gameSocket.sendText(JSON.stringify({ message: 'GAME_REQUEUE' }));
  }

  moveUp() {
    this.gameSocket.sendText(JSON.stringify({ direction: 'UP' }));
  }

  moveDown() {
    this.gameSocket.sendText(JSON.stringify({ direction: 'DOWN' }));
  }

  moveLeft() {
    this.gameSocket.sendText(JSON.stringify({ direction: 'LEFT' }));
  }

  moveRight() {
    this.gameSocket.sendText(JSON.stringify({ direction: 'RIGHT' }));
  }

  // Update display
  drawPlayer(player) {
    this.context.fillStyle = player.color;
    this.context.fillRect(GameComponent.BOX_SIZE * player.x, GameComponent.BOX_SIZE * player.y,
                          GameComponent.BOX_SIZE, GameComponent.BOX_SIZE);
  }

  drawObstacle(obstacle) {
    this.context.fillStyle = '#808080'; // obstacles always grey
    this.context.fillRect(GameComponent.BOX_SIZE * obstacle.x, GameComponent.BOX_SIZE * obstacle.y,
                          GameComponent.BOX_SIZE * obstacle.height, GameComponent.BOX_SIZE * obstacle.width);
  }
  
  drawMovingObstacle(obstacle) {
    this.context.fillStyle = '#808080'; // obstacles always grey
    if (obstacle.hasMoved) {
      this.context.clearRect(GameComponent.BOX_SIZE * obstacle.oldX, GameComponent.BOX_SIZE * obstacle.oldY,
                          GameComponent.BOX_SIZE * obstacle.height, GameComponent.BOX_SIZE * obstacle.width);
    }
    this.context.fillRect(GameComponent.BOX_SIZE * obstacle.x, GameComponent.BOX_SIZE * obstacle.y,
                          GameComponent.BOX_SIZE * obstacle.height, GameComponent.BOX_SIZE * obstacle.width);
  }

  writeToScreen(message: string) {
    const pre = document.createElement('p');
    pre.style.wordWrap = 'break-word';
    pre.innerHTML = message;
    this.output.appendChild(pre);
  }

  updatePlayerList(json) {
    let list = '<li class="list-group-item active">Players</li>';

    for (const player of json.playerlist) {
      list += `
<li class="list-group-item">
  <span style="color: ${player.color};">${player.name}</span>: ${this.getStatus(player.status)}
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

}
