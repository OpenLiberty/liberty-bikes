import { Component, OnInit, OnDestroy, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { Meta } from '@angular/platform-browser';
import { GameService } from './game.service';
import { LoginComponent } from '../login/login.component';
import * as EventSource from 'eventsource';
import { environment } from './../../environments/environment';
import { Player } from '../entity/player';
import { Obstacle } from '../entity/obstacle';
import { Shape, Stage } from 'createjs-module';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.scss'],
  providers: [ GameService ],
})
export class GameComponent implements OnInit, OnDestroy {
  static readonly BOX_SIZE = 5;
  static readonly OBSTACLE_COLOR = '#808080';

  roundId: string;
  serverHost: string;
  serverPort: string;

  partyId: string;
  showPartyId = false;
  showLoader = false;

  output: HTMLElement;
  idDisplay: HTMLElement;

  canvas: any;
  context: CanvasRenderingContext2D;
  stage: Stage;

  players: Player[];
  obstacles: Obstacle[];
  trailsShape: Shape;
  obstaclesShape: Shape;

  constructor(private meta: Meta,
		      private router: Router,
		      private ngZone: NgZone,
		      private gameService: GameService,
  ) {
    gameService.messages.subscribe((msg) => {
      const json = msg as any;
      if (json.requeue) {
    	    this.processRequeue(json.requeue);
      }
      if (json.obstacles) {
        for (let obstacle of json.obstacles) {
          this.obstaclesShape.graphics.beginFill(GameComponent.OBSTACLE_COLOR).rect(
            obstacle.x * GameComponent.BOX_SIZE,
            obstacle.y * GameComponent.BOX_SIZE,
            obstacle.width * GameComponent.BOX_SIZE,
            obstacle.height * GameComponent.BOX_SIZE
          );
        }
      }
      if (json.movingObstacles) {
        if (this.obstacles == null || this.obstacles.length < json.movingObstacles.length) {
          if (this.obstacles != null) {
            this.obstacles.forEach(obstacle => {
              if (obstacle.shape != null) {
                this.stage.removeChild(obstacle.shape);
              }
            });
          }

          this.obstacles = new Array<Obstacle>();
          json.movingObstacles.forEach((obstacle, i) => {
            const newObstacle = new Obstacle();
            newObstacle.width = obstacle.width;
            newObstacle.height = obstacle.height;

            const obShape = new Shape();
            obShape.graphics.beginFill(GameComponent.OBSTACLE_COLOR).rect(0, 0, newObstacle.width * GameComponent.BOX_SIZE, newObstacle.height * GameComponent.BOX_SIZE);
            obShape.x = obstacle.x * GameComponent.BOX_SIZE;
            obShape.y = obstacle.y * GameComponent.BOX_SIZE;

            newObstacle.shape = obShape;
            this.obstacles.push(newObstacle);
            this.stage.addChild(newObstacle.shape);

            // this.stage.update();

          });
        } else {
          json.movingObstacles.forEach((obstacle, i) => {
            console.log(obstacle);
            console.log(this.obstacles[i]);
            this.obstacles[i].shape.x = obstacle.x * GameComponent.BOX_SIZE;
            this.obstacles[i].shape.y = obstacle.y * GameComponent.BOX_SIZE;
          });
        }

        // this.stage.update();
      }
      if (json.playerlist) {
        console.log(json.playerlist);

        if (this.players != null) {
          this.players.forEach(player => {
            if (player.shape != null) {
              this.stage.removeChild(player.shape);
            }
          });
        }

        this.players = new Array<Player>();
        for (let playerInfo of json.playerlist) {
          const newPlayer = new Player();
          newPlayer.name = playerInfo.name;
          newPlayer.color = playerInfo.color;
          newPlayer.status = playerInfo.status;

          const playerShape = new Shape();
          playerShape.graphics.beginFill(newPlayer.color).rect(0, 0, playerInfo.width * GameComponent.BOX_SIZE, playerInfo.height * GameComponent.BOX_SIZE);
          playerShape.graphics.beginFill('#e8e5e5').rect(playerInfo.width / 4 * GameComponent.BOX_SIZE,
                                playerInfo.height / 4 * GameComponent.BOX_SIZE,
                                GameComponent.BOX_SIZE * (playerInfo.width / 2), GameComponent.BOX_SIZE * (playerInfo.height / 2));

          playerShape.x = playerInfo.x * GameComponent.BOX_SIZE;
          playerShape.y = playerInfo.y * GameComponent.BOX_SIZE;

          if (playerInfo.id === "") {
            playerShape.visible = false;
          }
          newPlayer.shape = playerShape;
          this.stage.addChild(newPlayer.shape);

          this.players.push(newPlayer);
        }

        // this.stage.update();

      }
      if (json.players) {
        json.players.forEach((player, i) => {
          if (player.alive) {
            const shape = this.players[i].shape;

            if (!shape.visible) {
              shape.visible = true;
            }
            shape.x = player.x * GameComponent.BOX_SIZE;
            shape.y = player.y * GameComponent.BOX_SIZE;

            this.trailsShape.graphics.beginFill(this.players[i].color).rect(GameComponent.BOX_SIZE * player.x + player.width / 2 * GameComponent.BOX_SIZE - GameComponent.BOX_SIZE / 2,
              GameComponent.BOX_SIZE * player.y + player.height / 2 * GameComponent.BOX_SIZE - GameComponent.BOX_SIZE / 2,
              GameComponent.BOX_SIZE, GameComponent.BOX_SIZE);
          }
        });

        // this.stage.update();
      }
      if (json.countdown) {
        this.startingCountdown(json.countdown);
      }
      if (json.keepAlive) {
        this.gameService.send({ keepAlive: true });
      }

      this.stage.update();
    }, (err) => {
      console.log(`Error occurred: ${err}`);
    });
  }

  ngOnInit() {
    this.roundId = sessionStorage.getItem('roundId');

    if (sessionStorage.getItem('isSpectator') === 'true') {
      console.log('is a spectator... showing game id');
      // Set the Party ID and make visible
      this.partyId = sessionStorage.getItem('partyId');
      this.showPartyId = true;
      this.gameService.send({'spectatorjoined': true});
    } else {
      this.gameService.send({'playerjoined': sessionStorage.getItem('userId'), 'hasGameBoard' : 'true'});
    }


    this.meta.addTag({name: 'viewport', content: 'width=1600'}, true);

    this.output = document.getElementById('output');
    this.idDisplay = document.getElementById('gameIdDisplay');

    this.canvas = document.getElementById('gameCanvas');
    this.context = this.canvas.getContext('2d');
    this.stage = new Stage(this.canvas);

    this.trailsShape = new Shape();
    this.trailsShape.x = 0;
    this.trailsShape.y = 0;

    this.stage.addChild(this.trailsShape);

    this.obstaclesShape = new Shape();
    this.obstaclesShape.x = 0;
    this.obstaclesShape.y = 0;

    this.stage.addChild(this.obstaclesShape);

    this.stage.update();

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

  ngOnDestroy() {
    sessionStorage.removeItem('roundId');
  }

  // Game actions
  startGame() {
    this.gameService.send({ message: 'GAME_START' });
  }

  requeue() {
    let partyId = sessionStorage.getItem('partyId');
    if (sessionStorage.getItem('isSpectator') === 'true' || partyId === null) {
      this.gameService.send({ message: 'GAME_REQUEUE' });
    } else {
      let queueCallback = new EventSource(`${environment.API_URL_PARTY}/${partyId}/queue`);
      queueCallback.onmessage = msg => {
        let queueMsg = JSON.parse(msg.data);
        if (queueMsg.queuePosition) {
          // go to login page, reuse the same EventSource
          LoginComponent.queueCallback = queueCallback;
          sessionStorage.setItem('queuePosition', queueMsg.queuePosition);
          this.ngZone.run(() => {
            this.router.navigate(['/login']);
          });
        } else if (queueMsg.requeue) {
          console.log(`ready to join game! Joining round ${queueMsg.requeue}`);
          queueCallback.close();
          this.processRequeue(queueMsg.requeue);
        } else {
          console.log('Error: unrecognized message ' + msg.data);
        }
      }
      queueCallback.onerror = msg => {
        console.log('Error showing queue position: ' + JSON.stringify(msg.data));
      }
    }
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

  processRequeue(newRoundId) {
    this.roundId = newRoundId;
    sessionStorage.setItem('roundId', this.roundId);
    location.reload();
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
    this.showLoader = true;
    setTimeout(() => {
      this.showLoader = false;
    }, (1000 * seconds));
  }

}
