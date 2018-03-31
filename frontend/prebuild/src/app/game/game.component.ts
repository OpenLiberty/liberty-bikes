import * as $ from 'jquery';

import { Component, OnInit } from '@angular/core';
import { Meta } from '@angular/platform-browser';
import { GameService } from './game.service';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.scss']
})
export class GameComponent implements OnInit {
  static readonly BOX_SIZE = 5;

  roundId: string;
  serverHost: string;
  serverPort: string;

  output: HTMLElement;
  idDisplay: HTMLElement;

  canvas: any;
  context: CanvasRenderingContext2D;

  constructor(private meta: Meta, private gameService: GameService) {
    gameService.messages.subscribe((msg) => {
      const json = msg as any;
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
          if (player.alive) {
            this.drawPlayer(player);
          }
        }
      }
      if (json.countdown) {
        this.startingCountdown(json.countdown);
      }
      if (json.keepAlive) {
        this.gameService.send({ keepAlive: true });
      }
    }, (err) => {
      console.log(`Error occurred: ${err}`);
    });
  }

  ngOnInit() {
    this.roundId = sessionStorage.getItem('roundId');

    if (sessionStorage.getItem('isSpectator') === 'true') {
      console.log('is a spectator... showing game id');
      // Set the Round ID and make visible
      $('#game-code').html(this.roundId);
      const gameId = $('#game-code-display');
      gameId.removeClass('d-none');
      gameId.addClass('d-inline-block');
      this.gameService.send({'spectatorjoined': true});
    } else {
      this.gameService.send({'playerjoined': sessionStorage.getItem('userId'), 'hasGameBoard' : 'true'});
    }


    this.meta.addTag({name: 'viewport', content: 'width=1600'}, true);

    this.output = document.getElementById('output');
    this.idDisplay = document.getElementById('gameIdDisplay');

    this.canvas = document.getElementById('gameCanvas');
    this.context = this.canvas.getContext('2d');

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

  // Game actions
  startGame() {
    this.gameService.send({ message: 'GAME_START' });
  }

  requeue() {
    this.gameService.send({ message: 'GAME_REQUEUE' });
  }
  
  moveUp() {
    this.gameService.send({ direction: 'UP' });
  }

  moveDown() {
    this.gameService.send({ direction: 'DOWN' });
  }

  moveLeft() {
    this.gameService.send({ direction: 'LEFT' });
  }

  moveRight() {
    this.gameService.send({ direction: 'RIGHT' });
  }

  // Update display
  drawPlayer(player) {
    this.context.fillStyle = player.color;
    this.context.clearRect(GameComponent.BOX_SIZE * player.oldX, GameComponent.BOX_SIZE * player.oldY,
                          GameComponent.BOX_SIZE * player.width, GameComponent.BOX_SIZE * player.height);
    this.context.fillRect(GameComponent.BOX_SIZE * player.x, GameComponent.BOX_SIZE * player.y,
                          GameComponent.BOX_SIZE * player.width, GameComponent.BOX_SIZE * player.height);
    this.context.fillRect(GameComponent.BOX_SIZE * player.trailPosX, GameComponent.BOX_SIZE * player.trailPosY,
                          GameComponent.BOX_SIZE, GameComponent.BOX_SIZE);
    this.context.fillRect(GameComponent.BOX_SIZE * player.trailPosX2, GameComponent.BOX_SIZE * player.trailPosY2,
                          GameComponent.BOX_SIZE, GameComponent.BOX_SIZE);
    this.context.fillStyle = '#e8e5e5';
    this.context.fillRect(GameComponent.BOX_SIZE * player.x + player.width / 4 * GameComponent.BOX_SIZE, 
                          GameComponent.BOX_SIZE * player.y + player.height / 4 * GameComponent.BOX_SIZE,
                          GameComponent.BOX_SIZE * (player.width / 2), GameComponent.BOX_SIZE * (player.height / 2));  
  }

  drawObstacle(obstacle) {
    this.context.fillStyle = '#808080'; // obstacles always grey
    this.context.fillRect(GameComponent.BOX_SIZE * obstacle.x, GameComponent.BOX_SIZE * obstacle.y,
                          GameComponent.BOX_SIZE * obstacle.width, GameComponent.BOX_SIZE * obstacle.height);
  }

  drawMovingObstacle(obstacle) {
    this.context.fillStyle = '#808080'; // obstacles always grey
    if (obstacle.hasMoved) {
      this.context.clearRect(GameComponent.BOX_SIZE * obstacle.oldX, GameComponent.BOX_SIZE * obstacle.oldY,
                          GameComponent.BOX_SIZE * obstacle.width, GameComponent.BOX_SIZE * obstacle.height);
    }
    this.context.fillRect(GameComponent.BOX_SIZE * obstacle.x, GameComponent.BOX_SIZE * obstacle.y,
                          GameComponent.BOX_SIZE * obstacle.width, GameComponent.BOX_SIZE * obstacle.height);
  }

  writeToScreen(message: string) {
    const pre = document.createElement('p');
    pre.style.wordWrap = 'break-word';
    pre.innerHTML = message;
    this.output.appendChild(pre);
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
  
  startingCountdown(seconds) {
    const loader = $('#loader-overlay');
    loader.removeClass('d-none');
    setTimeout(function() {
      loader.addClass('d-none');
    }, (1000 * seconds));
  }

}
